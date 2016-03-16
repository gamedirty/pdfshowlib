package org.ebookdroid.ui.viewer;

import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.models.DecodingProgressModel;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.core.models.ZoomModel;
import org.emdev.ui.actions.IActionController;

import the.pdfviewerx.ViewerActivity;
import android.app.Activity;
import android.content.Context;

public interface IActivityController extends IActionController<ViewerActivity> {

	Context getContext();

	Activity getActivity();

	BookSettings getBookSettings();

	DecodeService getDecodeService();

	DocumentModel getDocumentModel();

	IView getView();

	IViewController getDocumentController();

	IActionController<?> getActionController();

	ZoomModel getZoomModel();

	DecodingProgressModel getDecodingProgressModel();

	void runOnUiThread(Runnable r);

}
