package org.ebookdroid.common.settings.types;

import org.emdev.utils.enums.ResourceConstant;

public enum FontSize implements ResourceConstant {
	/**
     * 
     */
	TINY(0.5f),
	/**
     * 
     */
	SMALL(0.707f),
	/**
     * 
     */
	NORMAL(1.0f),
	/**
     * 
     */
	LARGE(1.414f),
	/**
     * 
     */
	HUGE(2.0f);

	public final float factor;

	private final String resValue;

	private FontSize(final float factor) {
		this.resValue = "";
		this.factor = factor;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.utils.enums.ResourceConstant#getResValue()
	 */
	@Override
	public String getResValue() {
		return resValue;
	}
}
