package org.emdev;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class BaseDroidApp {

	public static Context context;

	public static File EXT_STORAGE;

	public static File APP_STORAGE;

	public BaseDroidApp(Context context) {
		BaseDroidApp.context = context;
	}

	public void onCreate() {
		this.init();
	}

	protected void init() {

		final PackageManager pm = context.getPackageManager();
		try {
			final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			EXT_STORAGE = Environment.getExternalStorageDirectory();
			APP_STORAGE = getAppStorage(pi.packageName);

		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected File getAppStorage(final String appPackage) {
		File dir = EXT_STORAGE;
		if (dir != null) {
			final File appDir = new File(dir, "." + appPackage);
			if (appDir.isDirectory() || appDir.mkdir()) {
				dir = appDir;
			}
		} else {
			dir = context.getFilesDir();
		}
		dir.mkdirs();
		return dir.getAbsoluteFile();
	}

}
