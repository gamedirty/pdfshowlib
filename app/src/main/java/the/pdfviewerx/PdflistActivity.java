package the.pdfviewerx;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PdflistActivity extends ListActivity implements Runnable, OnItemClickListener {
	ProgressDialog pDialog;
	final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	ArrayList<File> pdfs;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (pdfs != null && pdfs.size() > 0) {
				getListView().setAdapter(new FileAdapter(pdfs));
			}
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("扫描sdcard中的pdf文件...");
		pDialog.setCancelable(false);
		pdfs = new ArrayList<File>();
		pDialog.show();
		new Thread(this).start();
		Log.i("info", "启动了");
		getListView().setOnItemClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void run() {
		File root = new File(rootPath);
		getPdfFiles(root);
		handler.sendEmptyMessage(0);
	}

	private void getPdfFiles(File root) {
		if (root.canRead()) {
			if (root.isDirectory()) {
				File[] fs = root.listFiles();
				for (File file : fs) {
					getPdfFiles(file);
				}
			} else if (root.isFile()) {
				String name = root.getName();
				if (name.endsWith(".pdf") || name.endsWith(".PDF")) {
					Log.i("info", "命中:" + root.getAbsolutePath());
					pdfs.add(root);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onPause();
		if (pDialog != null && pDialog.isShowing()) {
			pDialog.dismiss();
		}
	}

	class FileAdapter extends BaseAdapter {
		ArrayList<File> datas;

		public FileAdapter(ArrayList<File> datas) {
			this.datas = datas;
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			File curFile = datas.get(position);
			if (convertView == null) {
				convertView = View.inflate(PdflistActivity.this, R.layout.pdflist, null);
				vh = new ViewHolder();
				vh.tv1 = (TextView) convertView.findViewById(R.id.textView1);
				vh.tv2 = (TextView) convertView.findViewById(R.id.textView2);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			vh.tv1.setText(curFile.getName());
			vh.tv2.setText(curFile.getAbsolutePath());
			return convertView;
		}

		class ViewHolder {
			TextView tv1, tv2;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File openFile = pdfs.get(position);
		Log.i("info", "是不是存在:" + openFile.exists());
		Toast.makeText(this, "正在解析...", Toast.LENGTH_SHORT).show();
		Uri uri = Uri.fromFile(openFile);
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setClass(this, PdfActivity.class);
		intent.putExtra("pdfpath", "" + openFile.getAbsolutePath());
		Log.i("info", "路径:" + openFile.getAbsolutePath());
		startActivity(intent);
	}
}
