package org.ebookdroid;

public class EBookDroidLibraryLoader {

	private static boolean alreadyLoaded = false;

	public static void load() {
		if (alreadyLoaded) {
			return;
		}
		try {
			System.loadLibrary("ebookdroid");
			alreadyLoaded = true;
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}

	public static native void free();
}
