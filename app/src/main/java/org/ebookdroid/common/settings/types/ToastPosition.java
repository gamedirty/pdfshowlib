package org.ebookdroid.common.settings.types;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;

import org.emdev.utils.enums.ResourceConstant;

public enum ToastPosition implements ResourceConstant {

	/**
     * 
     */
	Invisible(0),
	/**
     * 
     */
	LeftTop(LEFT | TOP),
	/**
     * 
     */
	RightTop(RIGHT | TOP),
	/**
     * 
     */
	LeftBottom(LEFT | BOTTOM),
	/**
     * 
     */
	Bottom(CENTER | BOTTOM),
	/**
     * 
     */
	RightBottom(RIGHT | BOTTOM);

	public final int position;
	private final String resValue;

	private ToastPosition(int position) {
		this.resValue = "";
		this.position = position;
	}

	@Override
	public String getResValue() {
		return resValue;
	}
}
