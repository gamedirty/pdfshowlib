package org.emdev.common.fonts;

import java.io.File;
import java.lang.ref.WeakReference;

import org.emdev.common.fonts.data.FontFamilyType;
import org.emdev.common.fonts.data.FontInfo;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.enums.EnumUtils;

import android.util.SparseArray;

public class FontManager {

	public static SystemFontProvider system;
	public static ExtStorageFontProvider external;

	private static final SparseArray<WeakReference<TypefaceEx>> fonts = new SparseArray<WeakReference<TypefaceEx>>();

	public static void init(final File storage) {
		try {
			system = new SystemFontProvider();

			external = new ExtStorageFontProvider(storage);

			system.init();
			external.init();
		} catch (VerifyError ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public static String getExternalFont(final String fontAndFamily, final FontFamilyType defaultFamily, final FontStyle style) {
		final String[] arr = LengthUtils.safeString(fontAndFamily).split(",");
		final String fontPackName = arr[0].trim();
		final FontFamilyType type = getFontFamily(arr, defaultFamily);

		final FontPack fontPack = external.getFontPack(fontPackName);
		if (fontPack != null) {
			return getExternalFont(fontPack, type, style);
		}
		return null;
	}

	public static String[] getExternalFonts(final String fontAndFamily, final FontFamilyType defaultFamily) {
		final FontStyle[] styles = FontStyle.values();
		final String[] fonts = new String[styles.length];

		final String[] arr = LengthUtils.safeString(fontAndFamily).split(",");
		final String fontPackName = arr[0].trim();
		final FontFamilyType type = getFontFamily(arr, defaultFamily);

		final FontPack fontPack = external.getFontPack(fontPackName);
		if (fontPack != null) {
			for (int i = 0; i < styles.length; i++) {
				fonts[i] = getExternalFont(fontPack, type, styles[i]);
			}
		}
		return fonts;
	}

	protected static String getExternalFont(final FontPack fontPack, final FontFamilyType type, final FontStyle style) {
		final FontInfo font = fontPack.getFont(type, style);
		if (font == null) {
			return null;
		}
		final File fontFile = external.getFontFile(font);
		if (fontFile.exists()) {
			return fontFile.getAbsolutePath();
		}
		return null;
	}

	protected static FontFamilyType getFontFamily(final String[] arr, final FontFamilyType defType) {
		FontFamilyType type = defType;
		if (arr.length > 1) {
			type = EnumUtils.getByResValue(FontFamilyType.class, arr[1].trim(), type);
		}
		return type;
	}

}
