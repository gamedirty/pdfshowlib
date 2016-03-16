package org.emdev.ui;

import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMenuHelper;

import the.pdfviewerx.EBookDroidApp;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public abstract class AbstractActionActivity<A extends Activity, C extends AbstractActivityController<A>> extends Activity implements ActivityEvents {

	final boolean shouldBeTaskRoot;
	final int eventMask;

	protected boolean recreated;
	C controller;

	protected AbstractActionActivity(final boolean shouldBeTaskRoot, final int... events) {
		this.shouldBeTaskRoot = shouldBeTaskRoot;
		this.eventMask = ActivityEvents.Helper.merge(events);
	}

	@Override
	public final Object onRetainNonConfigurationInstance() {
		return getController();

	}

	public final C getController() {
		if (controller == null) {
			controller = createController();
		}
		return controller;
	}

	protected abstract C createController();

	@Override
	@SuppressWarnings({ "unchecked", "deprecation" })
	protected final void onCreate(final Bundle savedInstanceState) {
		initEbookLib();
		if (shouldBeTaskRoot && !isTaskRoot()) {
			super.onCreate(savedInstanceState);
			// Workaround for Android 2.1-
			finish();
			return;
		}
		// Check if controller was created before
		final Object last = this.getLastNonConfigurationInstance();
		if (last instanceof AbstractActivityController) {
			this.recreated = true;
			this.controller = (C) last;
			this.controller.setManagedComponent((A) this);
			if (Helper.enabled(this.controller.eventMask, BEFORE_RECREATE)) {
				this.controller.beforeRecreate((A) this);
			}
		} else {
			this.recreated = false;
			this.controller = createController();
			if ((this.controller.eventMask & BEFORE_CREATE) == BEFORE_CREATE) {
				this.controller.beforeCreate((A) this);
			}
		}
		super.onCreate(savedInstanceState);
		onCreateImpl(savedInstanceState);
	}

	boolean hasInitLib;

	private void initEbookLib() {
		if (!hasInitLib) {
			new EBookDroidApp(this);
			hasInitLib = true;
		}
	}

	protected void onCreateImpl(final Bundle savedInstanceState) {
	}

	@Override
	protected final void onRestart() {

		super.onRestart();

		if (Helper.enabled(this.eventMask, ON_RESTART)) {
			onRestartImpl();
		}

		if (Helper.enabled(this.controller.eventMask, ON_RESTART)) {
			controller.onRestart(recreated);
		}
	}

	protected void onRestartImpl() {
	}

	@Override
	protected final void onStart() {

		super.onStart();

		if (Helper.enabled(this.eventMask, ON_START)) {
			onStartImpl();
		}

		if (Helper.enabled(this.controller.eventMask, ON_START)) {
			controller.onStart(recreated);
		}
	}

	protected void onStartImpl() {
	}

	@Override
	protected final void onPostCreate(final Bundle savedInstanceState) {

		super.onPostCreate(savedInstanceState);

		if (Helper.enabled(this.eventMask, ON_POST_CREATE)) {
			onPostCreateImpl(savedInstanceState);
		}
		if (Helper.enabled(this.controller.eventMask, ON_POST_CREATE)) {
			controller.onPostCreate(savedInstanceState, recreated);
		}
	}

	protected void onPostCreateImpl(final Bundle savedInstanceState) {
	}

	@Override
	protected final void onResume() {

		super.onResume();

		if (Helper.enabled(this.eventMask, ON_RESUME)) {
			onResumeImpl();
		}

		if (Helper.enabled(this.controller.eventMask, ON_RESUME)) {
			controller.onResume(recreated);
		}
	}

	protected void onResumeImpl() {
	}

	@Override
	protected final void onPostResume() {

		super.onPostResume();

		if (Helper.enabled(this.eventMask, ON_POST_RESUME)) {
			onPostResumeImpl();
		}

		if (Helper.enabled(this.controller.eventMask, ON_POST_RESUME)) {
			controller.onPostResume(recreated);
		}
	}

	protected void onPostResumeImpl() {
	}

	@Override
	protected final void onPause() {
		final boolean finishing = isFinishing();

		super.onPause();

		if (Helper.enabled(this.eventMask, ON_PAUSE)) {
			onPauseImpl(finishing);
		}

		if (Helper.enabled(this.controller.eventMask, ON_PAUSE)) {
			controller.onPause(finishing);
		}
	}

	protected void onPauseImpl(final boolean finishing) {
	}

	@Override
	protected final void onStop() {
		final boolean finishing = isFinishing();

		super.onStop();

		if (Helper.enabled(this.eventMask, ON_STOP)) {
			onStopImpl(finishing);
		}

		if (Helper.enabled(this.controller.eventMask, ON_STOP)) {
			controller.onStop(finishing);
		}
	}

	protected void onStopImpl(final boolean finishing) {
	}

	@Override
	protected final void onDestroy() {
		if (shouldBeTaskRoot && !isTaskRoot()) {
			super.onDestroy();
			return;
		}

		final boolean finishing = isFinishing();

		super.onDestroy();

		if (Helper.enabled(this.eventMask, ON_DESTROY)) {
			onDestroyImpl(finishing);
		}

		if (Helper.enabled(this.controller.eventMask, ON_DESTROY)) {
			controller.onDestroy(finishing);
		}
	}

	protected void onDestroyImpl(final boolean finishing) {
	}

	@Override
	public final boolean onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu != null) {
			updateMenuItems(menu);
		}
		return true;
	}

	protected void updateMenuItems(final Menu menu) {
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		if (onMenuItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		if (onMenuItemSelected(item)) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	protected boolean onMenuItemSelected(final MenuItem item) {
		final int actionId = item.getItemId();
		final ActionEx action = getController().getOrCreateAction(actionId);
		if (action.getMethod().isValid()) {
			ActionMenuHelper.setActionParameters(item, action);
			action.run();
			return true;
		}
		return false;
	}

	public final void onButtonClick(final View view) {
		final int actionId = view.getId();
		final ActionEx action = getController().getOrCreateAction(actionId);
		action.onClick(view);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		if (data != null) {
			final int actionId = data.getIntExtra(ActionMenuHelper.ACTIVITY_RESULT_ACTION_ID, 0);
			if (actionId != 0) {
				final ActionEx action = getController().getOrCreateAction(actionId);
				action.putValue(ActionMenuHelper.ACTIVITY_RESULT_CODE, Integer.valueOf(resultCode));
				action.putValue(ActionMenuHelper.ACTIVITY_RESULT_DATA, data);
				action.run();
			}
		}
	}

	public final void setActionForView(final int id) {
		final View view = findViewById(id);
		final ActionEx action = getController().getOrCreateAction(id);
		if (view != null && action != null) {
			view.setOnClickListener(action);
		}
	}
}
