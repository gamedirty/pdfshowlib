package org.ebookdroid.common.settings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.listeners.IBookSettingsChangeListener;
import org.ebookdroid.core.PageIndex;
import org.emdev.utils.FileUtils;
import org.emdev.utils.listeners.ListenerProxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {

	public static final int INITIAL_FONTS = 1 << 0;

	private static final String INITIAL_FLAGS = "initial_flags";

	static Context ctx;

	static SharedPreferences prefs;

	static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private static final Map<String, BookSettings> bookSettings = new HashMap<String, BookSettings>();

	static ListenerProxy listeners = new ListenerProxy(IBookSettingsChangeListener.class);

	public static void init(final Context context) {
		if (ctx == null) {
			ctx = context;
			prefs = PreferenceManager.getDefaultSharedPreferences(context);

			AppSettings.init();
		}
	}

	public static BookSettings create(final long ownerId, final String fileName) {
		lock.writeLock().lock();
		try {
			BookSettings current = new BookSettings(fileName);
			AppSettings.setDefaultSettings(current);
			bookSettings.put(current.fileName, current);

			return current;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static boolean hasOpenedBooks() {
		lock.readLock().lock();
		try {
			return !bookSettings.isEmpty();
		} finally {
			lock.readLock().unlock();
		}
	}

	public static BookSettings getBookSettings(final String fileName) {
		lock.writeLock().lock();
		try {
			return getBookSettingsImpl(fileName);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private static BookSettings getBookSettingsImpl(final String fileName) {
		BookSettings bs = loadBookSettingsImpl(fileName);
		if (bs == null) {
			final String mpath = FileUtils.invertMountPrefix(fileName);
			final File f = mpath != null ? new File(mpath) : null;
			if (f != null && f.exists()) {
				bs = loadBookSettingsImpl(mpath);
			}
		}
		return bs;
	}

	private static BookSettings loadBookSettingsImpl(final String fileName) {
		BookSettings bs = bookSettings.get(fileName);
		return bs;
	}

	public static void clearAllRecentBookSettings() {
		lock.writeLock().lock();
		try {
			for (final BookSettings current : bookSettings.values()) {
				current.persistent = false;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void removeBookFromRecents(final String path) {
		lock.writeLock().lock();
		try {
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void deleteBookSettings(final BookSettings bs) {
		lock.writeLock().lock();
		try {
			bs.persistent = false;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void deleteAllBookSettings() {
		lock.writeLock().lock();
		try {
			for (final BookSettings current : bookSettings.values()) {
				current.persistent = false;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void currentPageChanged(final BookSettings current, final PageIndex oldIndex, final PageIndex newIndex) {
		if (current == null) {
			return;
		}
		lock.writeLock().lock();
		try {
			current.currentPageChanged(oldIndex, newIndex);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void zoomChanged(final BookSettings current, final float zoom, final boolean committed) {
		if (current == null) {
			return;
		}
		lock.writeLock().lock();
		try {
			current.setZoom(zoom, committed);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void positionChanged(final BookSettings current, final float offsetX, final float offsetY) {
		if (current == null) {
			return;
		}
		lock.writeLock().lock();
		try {
			current.positionChanged(offsetX, offsetY);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public static void storeBookSettings(final BookSettings current) {
		if (current == null) {
			return;
		}
		lock.readLock().lock();
		try {
		} finally {
			lock.readLock().unlock();
		}
	}

	public static void applyBookSettingsChanges(final BookSettings oldSettings, final BookSettings newSettings) {

		final BookSettings.Diff diff = new BookSettings.Diff(oldSettings, newSettings);
		final IBookSettingsChangeListener l = listeners.getListener();
		l.onBookSettingsChanged(oldSettings, newSettings, diff);
	}

	public static void addListener(final Object l) {
		listeners.addListener(l);
	}

	public static void removeListener(final Object l) {
		listeners.removeListener(l);
	}

	public static void setInitialFlags(final int flag) {
		final int old = prefs.getInt(INITIAL_FLAGS, 0);
		prefs.edit().putInt(INITIAL_FLAGS, old | flag).commit();
	}

}
