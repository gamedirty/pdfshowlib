package org.ebookdroid.ui.viewer;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.ebookdroid.CodecType;
import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.ByteBufferManager;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.listeners.IBookSettingsChangeListener;
import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.PageIndex;
import org.ebookdroid.core.events.CurrentPageListener;
import org.ebookdroid.core.events.DecodingProgressListener;
import org.ebookdroid.core.models.DecodingProgressModel;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.core.models.ZoomModel;
import org.ebookdroid.droids.mupdf.codec.exceptions.MuPdfPasswordException;
import org.ebookdroid.ui.viewer.stubs.ViewContollerStub;
import org.emdev.common.content.ContentScheme;
import org.emdev.ui.AbstractActivityController;
import org.emdev.ui.actions.IActionController;
import org.emdev.ui.progress.IProgressIndicator;
import org.emdev.ui.tasks.AsyncTaskExecutor;
import org.emdev.ui.tasks.BaseAsyncTask;

import the.pdfviewerx.EBookDroidApp;
import the.pdfviewerx.ViewerActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

/**
 * activity控制类
 * 
 * @author monkey-d-wood
 */
public class ViewerActivityController extends AbstractActivityController<ViewerActivity> implements IActivityController, DecodingProgressListener, CurrentPageListener,
		IBookSettingsChangeListener {

	private final AtomicReference<IViewController> ctrl = new AtomicReference<IViewController>(ViewContollerStub.STUB);

	private ZoomModel zoomModel;

	private DecodingProgressModel progressModel;

	private DocumentModel documentModel;

	private String bookTitle;

	private ContentScheme scheme = ContentScheme.FILE;

	private String m_fileName;

	private BookSettings bookSettings;

	private final AsyncTaskExecutor executor;

	/**
	 * Instantiates a new base viewer activity.
	 */
	public ViewerActivityController(final ViewerActivity activity) {
		super(activity, BEFORE_CREATE, BEFORE_RECREATE, AFTER_CREATE, ON_POST_CREATE, ON_DESTROY);

		executor = new AsyncTaskExecutor(256, 1, 5, 1, "BookExecutor-" + id);

		SettingsManager.addListener(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActivityController#beforeCreate(android.app.Activity)
	 */
	@Override
	public void beforeCreate(final ViewerActivity activity) {
		activity.getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		activity.setProgressBarIndeterminate(true);
		activity.setProgressBarIndeterminateVisibility(true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActivityController#beforeRecreate(android.app.Activity)
	 */
	@Override
	public void beforeRecreate(final ViewerActivity activity) {
		activity.getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		activity.setProgressBarIndeterminate(true);
		activity.setProgressBarIndeterminateVisibility(true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActivityController#afterCreate(android.app.Activity,
	 *      boolean)
	 */
	@Override
	public void afterCreate(final ViewerActivity activity, final boolean recreated) {
		// setResource("/storage/emulated/0/androidcdd.pdf");
	}

	Uri data;

	public void setResource(String path) {

		File file = new File(path);
		data = Uri.fromFile(file);
		if (!file.exists()) {
			throw new RuntimeException("pdf文件不存在");
		}
		if (!path.toLowerCase().endsWith(".pdf")) {
			throw new RuntimeException("不是pdf文件");
		}
		bookTitle = file.getName();

		documentModel = new DocumentModel(CodecType.PDF);
		documentModel.addListener(ViewerActivityController.this);
		progressModel = new DecodingProgressModel();
		progressModel.addListener(ViewerActivityController.this);

		m_fileName = file.getAbsolutePath();
		bookSettings = SettingsManager.create(id, m_fileName);
		SettingsManager.applyBookSettingsChanges(null, bookSettings);
		startDecoding("");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActivityController#onPostCreate(android.os.Bundle,
	 *      boolean)
	 */
	@Override
	public void onPostCreate(final Bundle savedInstanceState, final boolean recreated) {
		setWindowTitle();

	}

	public void startDecoding(final String password) {
		final BookLoadTask loadTask = new BookLoadTask(password);
		loadTask.run();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.emdev.ui.AbstractActivityController#onDestroy(boolean)
	 */
	@Override
	public void onDestroy(final boolean finishing) {

	}

	@Override
	public void onStop(boolean finishing) {
		super.onStop(finishing);
		if (finishing) {
			if (documentModel != null) {
				documentModel.recycle();
			}
			if (scheme != null && scheme.temporary) {
			}
			SettingsManager.removeListener(this);
			BitmapManager.clear("on finish");
			ByteBufferManager.clear("on finish");

			EBookDroidApp.onActivityClose(finishing);

		}
	}

	protected IViewController switchDocumentController(final BookSettings bs) {
		if (bs != null) {
			try {
				final IViewController newDc = bs.viewMode.create(this);
				if (newDc != null) {
					final IViewController oldDc = ctrl.getAndSet(newDc);
					getZoomModel().removeListener(oldDc);
					getZoomModel().addListener(newDc);
					return ctrl.get();
				}
			} catch (final Throwable e) {
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.events.DecodingProgressListener#decodingProgressChanged(int)
	 */
	@Override
	public void decodingProgressChanged(final int currentlyDecoding) {
		final Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					final ViewerActivity activity = getManagedComponent();
					activity.setProgressBarIndeterminateVisibility(currentlyDecoding > 0);
					activity.getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, currentlyDecoding == 0 ? 10000 : currentlyDecoding);
				} catch (final Throwable e) {
				}
			}
		};

		getView().post(r);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.events.CurrentPageListener#currentPageChanged(org.ebookdroid.core.PageIndex,
	 *      org.ebookdroid.core.PageIndex)
	 */
	@Override
	public void currentPageChanged(final PageIndex oldIndex, final PageIndex newIndex) {
		final Runnable r = new Runnable() {

			@Override
			public void run() {
				final int pageCount = documentModel.getPageCount();
				String pageText = "";
				if (pageCount > 0) {
					final int offset = bookSettings != null ? bookSettings.firstPageOffset : 1;
					if (offset == 1) {
						pageText = (newIndex.viewIndex + 1) + "/" + pageCount;
					} else {
						pageText = offset + "/" + (newIndex.viewIndex + offset) + "/" + (pageCount - 1 + offset);
					}
				}
				getManagedComponent().currentPageChanged(pageText, bookTitle);
				SettingsManager.currentPageChanged(bookSettings, oldIndex, newIndex);
			}
		};

		getView().post(r);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#runOnUiThread(java.lang.Runnable)
	 */
	@Override
	public void runOnUiThread(final Runnable r) {
		final FutureTask<Object> task = new FutureTask<Object>(r, null);

		try {
			getActivity().runOnUiThread(task);
			task.get();
		} catch (final InterruptedException ex) {
			Thread.interrupted();
		} catch (final ExecutionException ex) {
			ex.printStackTrace();
		} catch (final Throwable th) {
			th.printStackTrace();
		}
	}

	public void setWindowTitle() {
		getManagedComponent().getWindow().setTitle(bookTitle);
	}

	/**
	 * Gets the zoom model.
	 * 
	 * @return the zoom model
	 */
	@Override
	public ZoomModel getZoomModel() {
		if (zoomModel == null) {
			zoomModel = new ZoomModel();
		}
		return zoomModel;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getDecodeService()
	 */
	@Override
	public DecodeService getDecodeService() {
		return documentModel != null ? documentModel.decodeService : null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getDecodingProgressModel()
	 */
	@Override
	public DecodingProgressModel getDecodingProgressModel() {
		return progressModel;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getDocumentModel()
	 */
	@Override
	public DocumentModel getDocumentModel() {
		return documentModel;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getDocumentController()
	 */
	@Override
	public final IViewController getDocumentController() {
		return ctrl.get();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getView()
	 */
	@Override
	public final IView getView() {
		return getManagedComponent().getPdfView();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getBookSettings()
	 */
	@Override
	public final BookSettings getBookSettings() {
		return bookSettings;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IActivityController#getActionController()
	 */
	@Override
	public final IActionController<?> getActionController() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.common.settings.listeners.IBookSettingsChangeListener#onBookSettingsChanged(org.ebookdroid.common.settings.books.BookSettings,
	 *      org.ebookdroid.common.settings.books.BookSettings,
	 *      org.ebookdroid.common.settings.books.BookSettings.Diff)
	 */
	@Override
	public void onBookSettingsChanged(final BookSettings oldSettings, final BookSettings newSettings, final BookSettings.Diff diff) {
		if (newSettings == null) {
			return;
		}

		boolean redrawn = false;
		if (diff.isViewModeChanged() || diff.isSplitPagesChanged() || diff.isCropPagesChanged()) {
			redrawn = true;
			final IViewController newDc = switchDocumentController(newSettings);
			if (!diff.isFirstTime() && newDc != null) {
				newDc.init(null);
				newDc.show();
			}
		}

		// if (diff.isRotationChanged()) {
		// getManagedComponent().setRequestedOrientation(newSettings.getOrientation(AppSettings.current()));
		// }

		if (diff.isFirstTime()) {
			getZoomModel().initZoom(newSettings.getZoom());
		}

		final IViewController dc = getDocumentController();

		if (!redrawn && (diff.isEffectsChanged())) {
			redrawn = true;
			dc.toggleRenderingEffects();
		}

		if (!redrawn && diff.isPageAlignChanged()) {
			dc.setAlign(newSettings.pageAlign);
		}

		if (diff.isAnimationTypeChanged()) {
			dc.updateAnimationType();
		}

		currentPageChanged(PageIndex.NULL, documentModel.getCurrentIndex());
		getManagedComponent().invalidateOptionsMenu();
	}

	final class BookLoadTask extends BaseAsyncTask<String, Throwable> implements Runnable, IProgressIndicator {

		private final String m_password;

		public BookLoadTask(final String password) {
			super(getManagedComponent(), 0, false);
			m_password = password;
		}

		@Override
		public void run() {
			executor.execute(this);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Throwable doInBackground(final String... params) {
			try {
				final File cached = scheme.loadToCache(data, this);
				if (cached != null) {
					m_fileName = cached.getAbsolutePath();
					setProgressDialogMessage(startProgressStringId);
				}
				getView().waitForInitialization();
				documentModel.open(m_fileName, m_password);
				getDocumentController().init(this);
				return null;
			} catch (final MuPdfPasswordException pex) {
				return pex;
			} catch (final Exception e) {
				return e;
			} catch (final Throwable th) {
				return th;
			} finally {
			}
		}

		@Override
		protected void onPostExecute(Throwable result) {
			try {
				if (result == null) {
					try {
						getDocumentController().show();

						final DocumentModel dm = getDocumentModel();
						currentPageChanged(PageIndex.NULL, dm.getCurrentIndex());

					} catch (final Throwable th) {
						result = th;
					}
				}

				super.onPostExecute(result);

			} catch (final Throwable th) {
			} finally {
			}
		}

		@Override
		public void setProgressDialogMessage(final int resourceID, final Object... args) {
			publishProgress("Assessing page size %1$d/%2$d");
		}
	}

}
