package the.pdfviewerx;

import android.os.Bundle;

public class PdfActivity extends ViewerActivity {
	@Override
	protected void onCreateImpl(Bundle savedInstanceState) {
		super.onCreateImpl(savedInstanceState);
		setContentView(getPdfView().showPdf(getIntent().getStringExtra("pdfpath")));

	}

}
