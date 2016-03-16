package org.ebookdroid.common.bitmaps;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.emdev.BaseDroidApp;
import org.emdev.utils.collections.ArrayDeque;
import org.emdev.utils.collections.SparseArrayEx;
import org.emdev.utils.collections.TLIterator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

public class BitmapManager {

	private final static long BITMAP_MEMORY_LIMIT = Runtime.getRuntime().maxMemory() / 2;

	private static final int GENERATION_THRESHOLD = 10;

	private static SparseArrayEx<AbstractBitmapRef> used = new SparseArrayEx<AbstractBitmapRef>();

	private static ArrayDeque<AbstractBitmapRef> pool = new ArrayDeque<AbstractBitmapRef>();

	private static SparseArray<Bitmap> resources = new SparseArray<Bitmap>();

	private static Queue<Object> releasing = new ConcurrentLinkedQueue<Object>();

	private static final AtomicLong created = new AtomicLong();
	private static final AtomicLong reused = new AtomicLong();

	private static final AtomicLong memoryUsed = new AtomicLong();
	private static final AtomicLong memoryPooled = new AtomicLong();

	private static AtomicLong generation = new AtomicLong();

	private static ReentrantLock lock = new ReentrantLock();

	public static Bitmap getResource(final int resourceId) {
		synchronized (resources) {
			Bitmap bitmap = resources.get(resourceId);
			if (bitmap == null || bitmap.isRecycled()) {
				final Resources resources = BaseDroidApp.context.getResources();
				bitmap = BitmapFactory.decodeResource(resources, resourceId);
			}
			return bitmap;
		}
	}

	public static IBitmapRef addBitmap(final String name, final Bitmap bitmap) {
		lock.lock();
		try {
			final BitmapRef ref = new BitmapRef(bitmap, generation.get());
			used.append(ref.id, ref);

			created.incrementAndGet();
			memoryUsed.addAndGet(ref.size);

			ref.name = name;
			return ref;
		} finally {
			lock.unlock();
		}
	}

	public static IBitmapRef getBitmap(final String name, final int width, final int height, final Bitmap.Config config) {
		lock.lock();
		try {

			final TLIterator<AbstractBitmapRef> it = pool.iterator();
			try {
				while (it.hasNext()) {
					final AbstractBitmapRef ref = it.next();

					if (!ref.isRecycled() && ref.config == config && ref.width == width && ref.height >= height) {
						if (ref.used.compareAndSet(false, true)) {
							it.remove();

							ref.gen = generation.get();
							used.append(ref.id, ref);

							reused.incrementAndGet();
							memoryPooled.addAndGet(-ref.size);
							memoryUsed.addAndGet(ref.size);

							ref.name = name;
							return ref;
						} else {
						}
					}
				}
			} finally {
				it.release();
			}

			final BitmapRef ref = new BitmapRef(Bitmap.createBitmap(width, height, config), generation.get());
			used.put(ref.id, ref);

			created.incrementAndGet();
			memoryUsed.addAndGet(ref.size);

			shrinkPool(BITMAP_MEMORY_LIMIT);

			ref.name = name;
			return ref;
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

	public static void release() {
		lock.lock();
		try {
			generation.incrementAndGet();
			removeOldRefs();

			int count = 0;
			while (!releasing.isEmpty()) {
				final Object ref = releasing.poll();
				if (ref instanceof AbstractBitmapRef) {
					releaseImpl((AbstractBitmapRef) ref);
					count++;
				} else {
				}
			}

			shrinkPool(BITMAP_MEMORY_LIMIT);
		} finally {
			lock.unlock();
		}
	}

	public static void release(final IBitmapRef ref) {
		if (ref != null) {
			releasing.add(ref);
		}
	}

	static void releaseImpl(final AbstractBitmapRef ref) {
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
		final Iterator<AbstractBitmapRef> it = pool.iterator();
		while (it.hasNext()) {
			final AbstractBitmapRef ref = it.next();
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
			final AbstractBitmapRef ref = pool.poll();
			if (ref != null) {
				ref.recycle();
				memoryPooled.addAndGet(-ref.size);
				recycled++;
			}
		}
	}

	public static int getBitmapBufferSize(final int width, final int height, final Bitmap.Config config) {
		return getPixelSizeInBytes(config) * width * height;
	}

	public static int getPixelSizeInBytes(final Bitmap.Config config) {
		switch (config) {
		case ALPHA_8:
			return 1;
		case ARGB_4444:
		case RGB_565:
			return 2;
		case ARGB_8888:
		default:
			return 4;
		}
	}
}
