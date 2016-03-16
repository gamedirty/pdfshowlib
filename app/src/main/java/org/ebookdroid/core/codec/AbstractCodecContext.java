package org.ebookdroid.core.codec;

import java.util.concurrent.atomic.AtomicLong;

import android.graphics.Bitmap;

public abstract class AbstractCodecContext implements CodecContext {

	private static final AtomicLong SEQ = new AtomicLong();

	private static Integer densityDPI;

	final int supportedFeatures;

	private long contextHandle;

	/**
	 * Constructor.
	 * 
	 * @param supportedFeatures
	 *            supported features list
	 */
	protected AbstractCodecContext(final int supportedFeatures) {
		this(SEQ.incrementAndGet(), supportedFeatures);
	}

	/**
	 * Constructor.
	 * 
	 * @param contextHandle
	 *            contect handler
	 */
	protected AbstractCodecContext(final long contextHandle, final int supportedFeatures) {
		this.contextHandle = contextHandle;
		this.supportedFeatures = supportedFeatures;
	}

	@Override
	protected final void finalize() throws Throwable {
		recycle();
		super.finalize();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.codec.CodecContext#recycle()
	 */
	@Override
	public final void recycle() {
		if (!isRecycled()) {
			freeContext();
			contextHandle = 0;
		}
	}

	protected void freeContext() {
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.codec.CodecContext#isRecycled()
	 */
	@Override
	public final boolean isRecycled() {
		return contextHandle == 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.codec.CodecContext#getContextHandle()
	 */
	@Override
	public final long getContextHandle() {
		return contextHandle;
	}

	@Override
	public boolean isFeatureSupported(final int feature) {
		return (supportedFeatures & feature) != 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.codec.CodecContext#getBitmapConfig()
	 */
	@Override
	public Bitmap.Config getBitmapConfig() {
		return Bitmap.Config.ARGB_8888;
	}
}