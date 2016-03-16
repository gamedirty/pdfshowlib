package org.ebookdroid.common.settings.types;

import org.emdev.utils.enums.ResourceConstant;

public enum BookRotationType implements ResourceConstant {

	/**
    *
    */
	UNSPECIFIED(null),
	/**
    *
    */
	LANDSCAPE(RotationType.LANDSCAPE),
	/**
    *
    */
	PORTRAIT(RotationType.PORTRAIT);

	private final String resValue;

	private final RotationType orientation;

	private BookRotationType(final RotationType orientation) {
		this.resValue = "";
		this.orientation = orientation;
	}

	@Override
	public String getResValue() {
		return resValue;
	}

	public int getOrientation(final RotationType defRotation) {
		return orientation != null ? orientation.getOrientation() : defRotation.getOrientation();
	}
}
