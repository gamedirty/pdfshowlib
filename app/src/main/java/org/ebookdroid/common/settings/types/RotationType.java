package org.ebookdroid.common.settings.types;

import org.emdev.common.android.AndroidVersion;
import org.emdev.utils.enums.ResourceConstant;

import android.content.pm.ActivityInfo;

public enum RotationType implements ResourceConstant {

	/**
    *
    */
	UNSPECIFIED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, 3),
	/**
    *
    */
	LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, 3),
	/**
    *
    */
	PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, 3),
	/**
   *
   */
	USER(ActivityInfo.SCREEN_ORIENTATION_USER, 3),
	/**
    *
    */
	BEHIND(ActivityInfo.SCREEN_ORIENTATION_BEHIND, 3),
	/**
    *
    */
	AUTOMATIC(ActivityInfo.SCREEN_ORIENTATION_SENSOR, 3),
	/**
    *
    */
	NOSENSOR(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR, 3),
	/**
	 * Not compatible with of versions
	 */
	SENSOR_LANDSCAPE(6 /* ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE */, 10),
	/**
    *
    */
	SENSOR_PORTRAIT(7 /* ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT */, 10),
	/**
    *
    */
	REVERSE_LANDSCAPE(8 /* ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE */, 10),
	/**
    *
    */
	REVERSE_PORTRAIT(9 /* ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT */, 10),
	/**
    *
    */
	FULL_SENSOR(10 /* ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR */, 10);

	private final String resValue;

	private final int orientation;

	private final int version;

	private RotationType(final int orientation, final int version) {
		this.resValue = "";
		this.orientation = orientation;
		this.version = version;
	}

	public String getResValue() {
		return resValue;
	}

	public int getOrientation() {
		return this.version <= AndroidVersion.VERSION ? orientation : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	}

	public int getVersion() {
		return version;
	}
}
