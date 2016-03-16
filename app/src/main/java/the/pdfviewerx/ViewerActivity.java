package the.pdfviewerx;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.ByteBufferManager;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.types.ToastPosition;
import org.ebookdroid.ui.viewer.ViewerActivityController;
import org.ebookdroid.ui.viewer.viewers.GLView;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.gl.GLConfiguration;
import org.emdev.utils.LengthUtils;

import android.os.Bundle;

/**
 * 显示pdf的主界面
 * 
 * @author monkey-d-wood
 */
public class ViewerActivity extends AbstractActionActivity<ViewerActivity, ViewerActivityController> {

	private GLView view;

	public ViewerActivity() {
		super(false, ON_CREATE, ON_RESUME, ON_PAUSE, ON_DESTROY);
	}

	@Override
	protected ViewerActivityController createController() {
		return new ViewerActivityController(this);
	}

	public GLView getPdfView() {
		return view;
	}

	@Override
	protected void onCreateImpl(final Bundle savedInstanceState) {

		try {
			GLConfiguration.checkConfiguration();
			view = new GLView(getController());
		} catch (final Throwable th) {
			final ActionDialogBuilder builder = new ActionDialogBuilder(this, getController());
			builder.setMessage(th.getMessage());
			builder.show();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActionActivity#onDestroyImpl(boolean)
	 */
	@Override
	protected void onDestroyImpl(final boolean finishing) {
		view.onDestroy();
	}

	// 显示toast 页码变化
	public void currentPageChanged(final String pageText, final String bookTitle) {
		if (LengthUtils.isEmpty(pageText)) {
			return;
		}

		final AppSettings app = AppSettings.current();
		if (app.pageInTitle) {
			getWindow().setTitle("(" + pageText + ") " + bookTitle);
			return;
		}

		if (app.pageNumberToastPosition == ToastPosition.Invisible) {
			return;
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		BitmapManager.clear("on Low Memory: ");
		ByteBufferManager.clear("on Low Memory: ");
	}

}
