package org.emdev.common.content;

import java.io.File;
import java.io.IOException;

import org.emdev.ui.progress.IProgressIndicator;
import org.emdev.utils.LengthUtils;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

public enum ContentScheme {

	FILE("file"), UNKNOWN("", "");

	public final String scheme;

	public final String key;

	public final boolean temporary;

	private ContentScheme(final String scheme) {
		this.scheme = scheme;
		this.key = null;
		this.temporary = false;
	}

	private ContentScheme(final String scheme, final String key) {
		this.scheme = scheme;
		this.key = key;
		this.temporary = true;
	}

	public File loadToCache(final Uri uri, final IProgressIndicator progress) throws IOException {
		return null;
	}

	public String getResourceName(final ContentResolver cr, final Uri uri) {
		return getDefaultResourceName(uri, key);
	}

	public static String getDefaultResourceName(final Uri uri, final String defTitle) {
		return LengthUtils.safeString(uri.getLastPathSegment(), defTitle);
	}

	public static ContentScheme getScheme(final Intent intent) {
		return intent != null ? getScheme(intent.getScheme()) : UNKNOWN;
	}

	public static ContentScheme getScheme(final Uri uri) {
		return uri != null ? getScheme(uri.getScheme()) : UNKNOWN;
	}

	public static ContentScheme getScheme(final String scheme) {
		for (final ContentScheme s : values()) {

			if (s.scheme.equalsIgnoreCase(scheme)) {
				return s;
			}
		}
		return UNKNOWN;
	}
}
