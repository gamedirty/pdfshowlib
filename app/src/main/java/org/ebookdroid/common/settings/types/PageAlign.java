package org.ebookdroid.common.settings.types;

import org.emdev.utils.enums.ResourceConstant;

/**
 * The Enum PageAlign.
 */
public enum PageAlign implements ResourceConstant {
	/** BY WIDTH. */
	WIDTH(),
	/** BY HEIGHT. */
	HEIGHT(),
	/** AUTO. */
	AUTO();

	/** The resource value. */
	private final String resValue;

	/**
	 * Instantiates a new page align object.
	 * 
	 * @param resValue
	 *            the res value
	 */
	private PageAlign() {
		this.resValue = "";
	}

	/**
	 * Gets the resource value.
	 * 
	 * @return the resource value
	 */
	public String getResValue() {
		return resValue;
	}
}
