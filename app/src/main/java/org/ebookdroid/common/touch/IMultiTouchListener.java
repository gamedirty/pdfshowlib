package org.ebookdroid.common.touch;

import android.view.MotionEvent;

public interface IMultiTouchListener {

	void onTwoFingerPinchEnd(MotionEvent e);

	void onTwoFingerPinch(final MotionEvent e, final float oldDistance, final float newDistance);

	void onTwoFingerTap(MotionEvent e);

	void onDoubleTap();

}
