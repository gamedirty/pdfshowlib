package org.ebookdroid.common.bitmaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.emdev.utils.LengthUtils;
import org.emdev.utils.collections.ArrayDeque;
import org.emdev.utils.collections.SparseArrayEx;
import org.emdev.utils.collections.TLIterator;

public class ByteBufferManager {

	private final static long BITMAP_MEMORY_LIMIT = Runtime.getRuntime().maxMemory() / 2;

	private static final int GENERATION_THRESHOLD = 10;

	private static SparseArrayEx<ByteBufferBitmap> used = new SparseArrayEx<ByteBufferBitmap>();

	private static ArrayDeque<ByteBufferBitmap> pool = new ArrayDeque<ByteBufferBitmap>();

	private static Queue<Object> releasing = new ConcurrentLinkedQueue<Object>();

	private static final AtomicLong created = new AtomicLong();
	private static final AtomicLong reused = new AtomicLong();

	private static final AtomicLong memoryUsed = new AtomicLong();
	private static final AtomicLong memoryPooled = new AtomicLong();

	private static AtomicLong generation = new AtomicLong();

	private static ReentrantLock lock = new ReentrantLock();

	static int partSize = 1 << 7;

	public static ByteBufferBitmap getBitmap(final int width, final int height) {
		lock.lock();
		try {

			final TLIterator<ByteBufferBitmap> it = pool.iterator();
			try {
				while (it.hasNext()) {
					final ByteBufferBitmap ref = it.next();

					if (ref.size >= 4 * width * height) {
						if (ref.used.compareAndSet(false, true)) {
							it.remove();

							ref.pixels.rewind();
							ref.gen = generation.get();
							ref.width = width;
							ref.height = height;
							used.append(ref.id, ref);

							reused.incrementAndGet();
							memoryPooled.addAndGet(-ref.size);
							memoryUsed.addAndGet(ref.size);

							return ref;
						}
					}
				}
			} finally {
				it.release();
			}

			final ByteBufferBitmap ref = new ByteBufferBitmap(width, height);
			used.put(ref.id, ref);

			created.incrementAndGet();
			memoryUsed.addAndGet(ref.size);

			shrinkPool(BITMAP_MEMORY_LIMIT);

			return ref;
		} finally {
			lock.unlock();
		}
	}

	public static ByteBufferBitmap[] getParts(final int partSize, final int rows, final int columns) {
		lock.lock();
		try {

			int filled = 0;
			final int length = rows * columns;
			final ByteBufferBitmap[] arr = new ByteBufferBitmap[length];

			final int size = 4 * partSize * partSize;
			final TLIterator<ByteBufferBitmap> it = pool.iterator();
			try {
				while (filled < length && it.hasNext()) {
					final ByteBufferBitmap ref = it.next();

					if (ref.size == size) {
						if (ref.used.compareAndSet(false, true)) {
							it.remove();

							ref.pixels.rewind();
							ref.gen = generation.get();
							ref.width = partSize;
							ref.height = partSize;
							used.append(ref.id, ref);

							reused.incrementAndGet();
							memoryPooled.addAndGet(-ref.size);
							memoryUsed.addAndGet(ref.size);

							arr[filled++] = ref;
						} else {
						}
					}
				}
			} finally {
				it.release();
			}

			final int reused = filled;
			final int additional = length - filled;
			if (additional > 0) {
				for (int i = 0; i < additional; i++) {
					final ByteBufferBitmap ref = new ByteBufferBitmap(partSize, partSize);
					arr[filled++] = ref;

					used.append(ref.id, ref);
					created.incrementAndGet();
					memoryUsed.addAndGet(ref.size);
				}
			}

			if (additional > 0) {
				shrinkPool(BITMAP_MEMORY_LIMIT);
			}
			return arr;
		} finally {
			lock.unlock();
		}
	}

	public static void clear(final String msg) {
		lock.lock();
		try {
			generation.addAndGet(GENERATION_THRESHOLD * 2);
			removeOldRefs();
			release();
			shrinkPool(0);
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public static void release() {
		lock.lock();
		try {
			generation.incrementAndGet();
			removeOldRefs();

			int count = 0;
			while (!releasing.isEmpty()) {
				final Object ref = releasing.poll();
				if (ref instanceof ByteBufferBitmap) {
					releaseImpl((ByteBufferBitmap) ref);
					count++;
				} else if (ref instanceof GLBitmaps) {
					final GLBitmaps bmp = (GLBitmaps) ref;
					final ByteBufferBitmap[] bitmaps = bmp.clear();
					if (bitmaps != null) {
						for (final ByteBufferBitmap b : bitmaps) {
							releaseImpl(b);
							count++;
						}
					}
				} else if (ref instanceof List) {
					final List<GLBitmaps> list = (List<GLBitmaps>) ref;
					for (final GLBitmaps bmp : list) {
						final ByteBufferBitmap[] bitmaps = bmp.clear();
						if (bitmaps != null) {
							for (final ByteBufferBitmap b : bitmaps) {
								releaseImpl(b);
								count++;
							}
						}
					}
				} else if (ref instanceof ByteBufferBitmap[]) {
					final ByteBufferBitmap[] bitmaps = (ByteBufferBitmap[]) ref;
					for (final ByteBufferBitmap bitmap : bitmaps) {
						if (bitmap != null) {
							releaseImpl(bitmap);
							count++;
						}
					}
				}
			}

			shrinkPool(BITMAP_MEMORY_LIMIT);
		} finally {
			lock.unlock();
		}
	}

	public static void release(final ByteBufferBitmap ref) {
		if (ref != null) {
			releasing.add(ref);
		}
	}

	public static void release(final ByteBufferBitmap[] refs) {
		if (refs != null) {
			releasing.add(refs);
		}
	}

	public static void release(final GLBitmaps ref) {
		if (ref != null) {
			releasing.add(ref);
		}
	}

	public static void release(final List<GLBitmaps> bitmapsToRecycle) {
		if (LengthUtils.isNotEmpty(bitmapsToRecycle)) {
			releasing.add(new ArrayList<GLBitmaps>(bitmapsToRecycle));
		}
	}

	static void releaseImpl(final ByteBufferBitmap ref) {
		assert ref != null;
		if (ref.used.compareAndSet(true, false)) {
			if (used.get(ref.id, null) == ref) {
				used.remove(ref.id);
				memoryUsed.addAndGet(-ref.size);
			}
		}

		pool.add(ref);
		memoryPooled.addAndGet(ref.size);
	}

	private static void removeOldRefs() {
		final long gen = generation.get();
		int recycled = 0;
		final Iterator<ByteBufferBitmap> it = pool.iterator();
		while (it.hasNext()) {
			final ByteBufferBitmap ref = it.next();
			if (gen - ref.gen > GENERATION_THRESHOLD) {
				it.remove();
				ref.recycle();
				recycled++;
				memoryPooled.addAndGet(-ref.size);
			}
		}
	}

	private static void shrinkPool(final long limit) {
		int recycled = 0;
		while (memoryPooled.get() + memoryUsed.get() > limit && !pool.isEmpty()) {
			final ByteBufferBitmap ref = pool.poll();
			if (ref != null) {
				ref.recycle();
				memoryPooled.addAndGet(-ref.size);
				recycled++;
			}
		}
	}

	public static int getPartSize() {
		return partSize;
	}

	public static void setPartSize(final int partSize) {
		ByteBufferManager.partSize = partSize;
	}
}
