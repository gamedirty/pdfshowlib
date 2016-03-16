package org.ebookdroid.core.models;

import java.util.concurrent.atomic.AtomicInteger;

import org.ebookdroid.core.events.DecodingProgressListener;
import org.emdev.utils.listeners.ListenerProxy;

public class DecodingProgressModel extends ListenerProxy {

	private AtomicInteger currentlyDecoding = new AtomicInteger();

	public DecodingProgressModel() {
		super(DecodingProgressListener.class);
	}

	public void increase(int increment) {
		this.<DecodingProgressListener> getListener().decodingProgressChanged(currentlyDecoding.addAndGet(increment));
	}

	public void decrease() {
		this.<DecodingProgressListener> getListener().decodingProgressChanged(currentlyDecoding.decrementAndGet());
	}
}
