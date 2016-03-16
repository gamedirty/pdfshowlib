package org.ebookdroid.common.settings;

import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.common.settings.types.RotationType;
import org.ebookdroid.common.settings.types.ToastPosition;
import org.ebookdroid.core.curl.PageAnimationType;

public class AppSettings {

	public static final String BACKUP_KEY = "app-settings";

	private static AppSettings current;

	/* =============== UI settings =============== */

	public final String lang;

	public final boolean loadRecent;

	public final boolean confirmClose;

	public final boolean brightnessInNightModeOnly;

	public final int brightness;

	public final boolean keepScreenOn;

	public final RotationType rotation;

	public final boolean fullScreen;

	public final boolean showTitle;

	public final boolean pageInTitle;

	public final ToastPosition pageNumberToastPosition;

	public final ToastPosition zoomToastPosition;

	public final boolean showAnimIcon;

	/* =============== Tap & Scroll settings =============== */

	public final boolean tapsEnabled;

	public final int scrollHeight;

	public final boolean animateScrolling;

	/* =============== Performance settings =============== */

	public final int pagesInMemory;

	public final int decodingThreads;

	public final int decodingThreadPriority;

	public final int drawThreadPriority;

	public final boolean decodingOnScroll;

	public final int bitmapSize;

	public final int heapPreallocate;

	public final int pdfStorageSize;

	/* =============== Default rendering settings =============== */

	public final boolean nightMode;

	public boolean positiveImagesInNightMode;

	final int contrast;

	final int gamma;

	final int exposure;

	final boolean autoLevels;

	final boolean splitPages;

	final boolean splitRTL;

	final boolean cropPages;

	public final DocumentViewMode viewMode;

	final PageAlign pageAlign;

	final PageAnimationType animationType;

	/* =============== DjVU Format-specific settings =============== */

	public final int djvuRenderingMode;

	/* =============== PDF Format-specific settings =============== */

	public final boolean useCustomDpi;

	public final int xDpi;

	public final int yDpi;

	public final String monoFontPack;

	public final String sansFontPack;

	public final String serifFontPack;

	public final String symbolFontPack;

	public final String dingbatFontPack;

	public final boolean slowCMYK;

	/* =============== FB2 Format-specific settings =============== */

	public final String fb2FontPack;

	public final boolean fb2HyphenEnabled;

	public final boolean fb2CacheImagesOnDisk;

	/* =============================================== */

	private AppSettings() {
		/* =============== UI settings =============== */
		lang = "";
		loadRecent = false;
		confirmClose = false;
		brightnessInNightModeOnly = false;
		brightness = 100;
		keepScreenOn = true;
		rotation = RotationType.AUTOMATIC;
		fullScreen = false;
		showTitle = true;
		pageInTitle = true;
		pageNumberToastPosition = ToastPosition.LeftTop;
		zoomToastPosition = ToastPosition.LeftBottom;
		showAnimIcon = true;

		/* =============== Tap & Scroll settings =============== */
		tapsEnabled = true;
		scrollHeight = 50;
		animateScrolling = true;
		/* =============== Performance settings =============== */
		pagesInMemory = 0;
		decodingThreads = 1;
		decodingThreadPriority = 5;
		drawThreadPriority = 5;
		decodingOnScroll = true;
		bitmapSize = 9;
		heapPreallocate = 0;
		pdfStorageSize = 64;
		/* =============== Default rendering settings =============== */
		nightMode = false;
		positiveImagesInNightMode = false;
		contrast = 100;
		gamma = 100;
		exposure = 100;
		autoLevels = false;
		splitPages = false;
		splitRTL = false;
		cropPages = false;
		viewMode = DocumentViewMode.VERTICALL_SCROLL;
		pageAlign = PageAlign.WIDTH;
		animationType = PageAnimationType.NONE;
		/* =============== DjVU Format-specific settings =============== */
		djvuRenderingMode = 0;
		/* =============== PDF Format-specific settings =============== */
		useCustomDpi = false;
		xDpi = 120;
		yDpi = 120;
		monoFontPack = "";
		sansFontPack = "";
		serifFontPack = "";
		symbolFontPack = "";
		dingbatFontPack = "";
		slowCMYK = false;
		/* =============== FB2 Format-specific settings =============== */
		fb2FontPack = "System";
		fb2HyphenEnabled = false;
		fb2CacheImagesOnDisk = false;
	}

	/* =============== */

	public static void init() {
		current = new AppSettings();
	}

	public static AppSettings current() {
		SettingsManager.lock.readLock().lock();
		try {
			return current;
		} finally {
			SettingsManager.lock.readLock().unlock();
		}
	}

	/* =============== */

	static void setDefaultSettings(final BookSettings bs) {
		bs.nightMode = current.nightMode;
		bs.positiveImagesInNightMode = current.positiveImagesInNightMode;
		bs.contrast = current.contrast;
		bs.gamma = current.gamma;
		bs.exposure = current.exposure;
		bs.autoLevels = current.autoLevels;
		bs.splitPages = current.splitPages;
		bs.splitRTL = current.splitRTL;
		bs.cropPages = current.cropPages;
		bs.viewMode = current.viewMode;
		bs.pageAlign = current.pageAlign;
		bs.animationType = current.animationType;
	}
}
