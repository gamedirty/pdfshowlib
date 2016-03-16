package org.ebookdroid.core.models;

import org.ebookdroid.core.events.ZoomListener;
import org.emdev.utils.MathUtils;
import org.emdev.utils.listeners.ListenerProxy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

public class ZoomModel extends ListenerProxy {

	public static final float MIN_ZOOM = 1.0f;
	public static final float MAX_ZOOM = 9.0f;

	private static int ZOOM_ROUND_FACTOR = 0;

	private float initialZoom = MIN_ZOOM;
	private float currentZoom = MIN_ZOOM;

	private boolean isCommited;

	public ZoomModel() {
		super(ZoomListener.class);
	}

	public void initZoom(final float zoom) {
		this.initialZoom = this.currentZoom = adjust(zoom);
		isCommited = true;
	}

	public void setZoom(final float zoom) {
		setZoom(zoom, false);
		final float newZoom = adjust(zoom);
		final float oldZoom = this.currentZoom;
		if (newZoom != oldZoom) {
			isCommited = false;
			this.currentZoom = newZoom;
			this.<ZoomListener> getListener().zoomChanged(oldZoom, newZoom,
					false);
		}
	}

	public void setZoom(final float zoom, final boolean commitImmediately) {
		final float newZoom = adjust(zoom);
		final float oldZoom = this.currentZoom;
		if (newZoom != oldZoom || commitImmediately) {
			isCommited = commitImmediately;
			this.currentZoom = newZoom;
			this.<ZoomListener> getListener().zoomChanged(oldZoom, newZoom,
					commitImmediately);
			if (commitImmediately) {
				this.initialZoom = this.currentZoom;
			}
		}
	}

	public void smoothZoomTo(final float factor) {
		ValueAnimator va = ValueAnimator.ofFloat(currentZoom, factor);

		va.setDuration(500);
		va.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = Float.parseFloat(arg0.getAnimatedValue()
						.toString());
				setZoom(value, false);
			}
		});
		va.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setZoom(factor, false);
				commit();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}
		});
		va.start();
	}

	public void scaleZoom(final float factor) {
		setZoom(currentZoom * factor, false);
	}

	public void scaleAndCommitZoom(final float factor) {
		setZoom(currentZoom * factor, true);
	}

	public float getZoom() {
		return currentZoom;
	}

	public void commit() {
		if (!isCommited) {
			isCommited = true;
			this.<ZoomListener> getListener().zoomChanged(initialZoom,
					currentZoom, true);
			initialZoom = currentZoom;
		}
	}

	float adjust(final float zoom) {
		return MathUtils.adjust(
				ZOOM_ROUND_FACTOR <= 0 ? zoom : MathUtils.round(zoom,
						ZOOM_ROUND_FACTOR), MIN_ZOOM, MAX_ZOOM);
	}

}
