package the.pdfviewerx;

import org.ebookdroid.common.bitmaps.ByteBufferManager;
import org.ebookdroid.common.settings.SettingsManager;
import org.emdev.BaseDroidApp;
import org.emdev.common.fonts.FontManager;

import android.content.Context;

public class EBookDroidApp extends BaseDroidApp {
	public static Context context;

	public EBookDroidApp(Context context) {
		super(context);
		this.context = context;
		onCreate();
	}

	@Override
	public void onCreate() {
		// oncreate中做了的几件事情
		// 初始化设置的一些参数
		// 初始化字体资源
		// 调整设置里边栈的空间大小
		// 为设置添加监听器
		super.onCreate();
		SettingsManager.init(context);
		initFonts();
		SettingsManager.addListener(this);
		ByteBufferManager.setPartSize(1 << 9);
	}

	public static void initFonts() {
		FontManager.init(APP_STORAGE);
	}

	public static void onActivityClose(final boolean finishing) {
		if (finishing && !SettingsManager.hasOpenedBooks()) {
			System.exit(0);
		}
	}

}
