package org.ebookdroid.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ebookdroid.common.keysbinding.KeyBindingsManager;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.common.touch.DefaultGestureDetector;
import org.ebookdroid.common.touch.IGestureDetector;
import org.ebookdroid.common.touch.IMultiTouchListener;
import org.ebookdroid.common.touch.MultiTouchGestureDetector;
import org.ebookdroid.common.touch.TouchManager;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IView;
import org.ebookdroid.ui.viewer.IViewController;
import org.emdev.ui.actions.AbstractComponentController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.progress.IProgressIndicator;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class AbstractViewController extends
		AbstractComponentController<IView> implements IViewController {

	public static final int DOUBLE_TAP_TIME = 500;

	private static final Float FZERO = Float.valueOf(0);

	public final IActivityController base;

	public final DocumentModel model;

	public final DocumentViewMode mode;

	protected boolean isInitialized = false;

	protected boolean isShown = false;

	protected final AtomicBoolean inZoom = new AtomicBoolean();

	protected final AtomicBoolean inQuickZoom = new AtomicBoolean();

	protected final AtomicBoolean inZoomToColumn = new AtomicBoolean();

	protected final PageIndex pageToGo;

	protected int firstVisiblePage;

	protected int lastVisiblePage;

	protected boolean layoutLocked;

	private List<IGestureDetector> detectors;

	public AbstractViewController(final IActivityController base,
			final DocumentViewMode mode) {
		super(base, base.getView());

		this.base = base;
		this.mode = mode;
		this.model = base.getDocumentModel();

		this.firstVisiblePage = -1;
		this.lastVisiblePage = -1;

		this.pageToGo = base.getBookSettings().getCurrentPage();
	}

	protected List<IGestureDetector> getGestureDetectors() {
		if (detectors == null) {
			detectors = initGestureDetectors(new ArrayList<IGestureDetector>(4));
		}
		return detectors;
	}

	protected List<IGestureDetector> initGestureDetectors(
			final List<IGestureDetector> list) {
		final GestureListener listener = new GestureListener();
		list.add(new MultiTouchGestureDetector(listener));
		list.add(new DefaultGestureDetector(base.getContext(), listener));
		return list;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#getView()
	 */
	@Override
	public final IView getView() {
		return base.getView();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#getBase()
	 */
	@Override
	public final IActivityController getBase() {
		return base;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#init(the.pdfviewer3.ui.viewer.IActivityController.IBookLoadTask)
	 */
	@Override
	public final void init(final IProgressIndicator task) {
		if (!isInitialized) {
			try {
				model.initPages(base, task);
			} finally {
				isInitialized = true;
			}
		}
	}

	/**
	 *
	 */
	@Override
	public final void onDestroy() {
		// isShown = false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#show()
	 */
	@Override
	public final void show() {
		if (!isInitialized) {
			return;
		}
		if (!isShown) {
			isShown = true;

			invalidatePageSizes(InvalidateSizeReason.INIT, null);

			final BookSettings bs = base.getBookSettings();
			bs.lastChanged = System.currentTimeMillis();

			final Page page = pageToGo.getActualPage(model, bs);
			final int toPage = page != null ? page.index.viewIndex : 0;

			goToPage(toPage, bs.offsetX, bs.offsetY);
		} else {
		}
	}

	protected final void updatePosition(final Page page,
			final ViewState viewState) {
		final PointF pos = viewState.getPositionOnPage(page);
		SettingsManager.positionChanged(base.getBookSettings(), pos.x, pos.y);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.core.events.ZoomListener#zoomChanged(float, float,
	 *      boolean)
	 */
	@Override
	public final void zoomChanged(final float oldZoom, final float newZoom,
			final boolean committed) {
		if (!isShown) {
			return;
		}

		inZoom.set(!committed);
		EventPool.newEventZoom(this, oldZoom, newZoom, committed).process()
				.release();

		if (committed) {
		} else {
			inQuickZoom.set(false);
			inZoomToColumn.set(false);
		}
	}

	protected void scrollToColumn(final Page page, final RectF column,
			final PointF pos, final int screenHeight) {
		final ViewState vs = ViewState.get(AbstractViewController.this);
		final RectF pb = vs.getBounds(page);
		final RectF columnRegion = page.getPageRegion(pb, new RectF(column));
		columnRegion.offset(-vs.viewBase.x, -vs.viewBase.y);

		final float toX = columnRegion.left;
		final float toY = pb.top + pos.y * pb.height() - 0.5f * screenHeight;
		getView().scrollTo((int) toX, (int) toY);

		vs.release();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#updateMemorySettings()
	 */
	@Override
	public final void updateMemorySettings() {
		EventPool.newEventReset(this, null, false).process().release();
	}

	public final int getScrollX() {
		return getView().getScrollX();
	}

	public final int getWidth() {
		return getView().getWidth();
	}

	public final int getScrollY() {
		return getView().getScrollY();
	}

	public final int getHeight() {
		return getView().getHeight();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public final boolean dispatchKeyEvent(final KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			final Integer actionId = KeyBindingsManager.getAction(event);
			final ActionEx action = actionId != null ? getOrCreateAction(actionId)
					: null;
			if (action != null) {
				action.run();
				return true;
			} else {
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			final Integer id = KeyBindingsManager.getAction(event);
			if (id != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public final boolean onTouchEvent(final MotionEvent ev) {
		for (final IGestureDetector d : getGestureDetectors()) {
			if (d.enabled() && d.onTouchEvent(ev)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#onLayoutChanged(boolean,
	 *      boolean, android.graphics.Rect, android.graphics.Rect)
	 */
	@Override
	public boolean onLayoutChanged(final boolean layoutChanged,
			final boolean layoutLocked, final Rect oldLaout,
			final Rect newLayout) {
		if (layoutChanged && !layoutLocked) {
			if (isShown) {
				EventPool
						.newEventReset(this, InvalidateSizeReason.LAYOUT, true)
						.process().release();
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#toggleRenderingEffects()
	 */
	@Override
	public final void toggleRenderingEffects() {
		EventPool.newEventReset(this, null, true).process().release();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#invalidateScroll()
	 */
	@Override
	public final void invalidateScroll() {
		if (!isShown) {
			return;
		}
		getView().invalidateScroll();
	}

	/**
	 * Sets the page align flag.
	 * 
	 * @param align
	 *            the new flag indicating align
	 */
	@Override
	public final void setAlign(final PageAlign align) {
		EventPool.newEventReset(this, InvalidateSizeReason.PAGE_ALIGN, false)
				.process().release();
	}

	/**
	 * Checks if view is initialized.
	 * 
	 * @return true, if is initialized
	 */
	protected final boolean isShown() {
		return isShown;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#getFirstVisiblePage()
	 */
	@Override
	public final int getFirstVisiblePage() {
		return firstVisiblePage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#getLastVisiblePage()
	 */
	@Override
	public final int getLastVisiblePage() {
		return lastVisiblePage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#redrawView()
	 */
	@Override
	public final void redrawView() {
		getView().redrawView(ViewState.get(this));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebookdroid.ui.viewer.IViewController#redrawView(org.ebookdroid.core.ViewState)
	 */
	@Override
	public final void redrawView(final ViewState viewState) {
		getView().redrawView(viewState);
	}

	protected final boolean processTap(final TouchManager.Touch type,
			final MotionEvent e) {
		final float x = e.getX();
		final float y = e.getY();

		return processActionTap(type, x, y);
	}

	protected boolean processActionTap(final TouchManager.Touch type,
			final float x, final float y) {
		final Integer actionId = TouchManager.getAction(type, x, y, getWidth(),
				getHeight());
		final ActionEx action = actionId != null ? getOrCreateAction(actionId)
				: null;
		if (action != null) {
			action.run();
			return true;
		}
		return false;
	}

	protected class GestureListener extends SimpleOnGestureListener implements
			IMultiTouchListener {

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap(final MotionEvent e) {
			return processTap(TouchManager.Touch.DoubleTap, e);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
		 */
		@Override
		public boolean onDown(final MotionEvent e) {
			getView().forceFinishScroll();
			return true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent,
		 *      android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2,
				final float vX, final float vY) {
			final Rect l = getScrollLimits();
			float x = vX, y = vY;
			if (Math.abs(vX / vY) < 0.5) {
				x = 0;
			}
			if (Math.abs(vY / vX) < 0.5) {
				y = 0;
			}
			getView().startFling(x, y, l);
			getView().redrawView();
			return true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent,
		 *      android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
				final float distanceX, final float distanceY) {
			float x = distanceX, y = distanceY;
			if (Math.abs(distanceX / distanceY) < 0.5) {
				x = 0;
			}
			if (Math.abs(distanceY / distanceX) < 0.5) {
				y = 0;
			}
			getView().scrollBy((int) x, (int) y);
			return true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapUp(final MotionEvent e) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent e) {
			return processTap(TouchManager.Touch.SingleTap, e);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.ebookdroid.common.touch.IMultiTouchListener#onTwoFingerPinch(float,
		 *      float)
		 */
		@Override
		public void onTwoFingerPinch(final MotionEvent e,
				final float oldDistance, final float newDistance) {
			final float factor = newDistance / oldDistance;
			base.getZoomModel().scaleZoom(factor);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.ebookdroid.common.touch.IMultiTouchListener#onTwoFingerPinchEnd()
		 */
		@Override
		public void onTwoFingerPinchEnd(final MotionEvent e) {
			base.getZoomModel().commit();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.ebookdroid.common.touch.IMultiTouchListener#onTwoFingerTap()
		 */
		@Override
		public void onTwoFingerTap(final MotionEvent e) {
			processTap(TouchManager.Touch.TwoFingerTap, e);
		}

		@Override
		public void onDoubleTap() {
			// base.getZoomModel().scaleZoom(7);
			float zoom = base.getZoomModel().getZoom();
			if (zoom > 1) {
				base.getZoomModel().smoothZoomTo(1);
			} else {
				base.getZoomModel().smoothZoomTo(3);
			}
		}
	}
}
