package org.emdev.common.fonts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.emdev.common.fonts.data.FontPack;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public abstract class AbstractCustomFontProvider extends AbstractFontProvider {

	protected AbstractCustomFontProvider(int id, String name) {
		super(id, name);
	}

	@Override
	protected List<FontPack> load() {
		try {
			return loadCatalog();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return Collections.emptyList();
	}

	protected List<FontPack> loadCatalog() throws IOException, JSONException {
		final List<FontPack> list = new ArrayList<FontPack>();

		final InputStream stream = openCatalog();
		if (stream != null) {
			final BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			try {
				final StringBuilder buf = new StringBuilder();
				for (String s = in.readLine(); s != null; s = in.readLine()) {
					buf.append(s).append("\n");
				}
				final JSONArray arr = new JSONArray(buf.toString());
				for (int i = 0, n = arr.length(); i < n; i++) {
					list.add(new FontPack(this, arr.getJSONObject(i)));
				}
			} finally {
				try {
					in.close();
				} catch (final Exception ex) {
				}
			}
		}
		return list;
	}

	protected final JSONArray fromJSON(final InputStream stream) throws IOException, JSONException {
		final StringBuilder buf = new StringBuilder();
		final BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		for (String s = in.readLine(); s != null; s = in.readLine()) {
			buf.append(s).append("\n");
		}
		return new JSONArray(buf.toString());
	}

	protected final JSONArray toJSON() throws JSONException {
		JSONArray arr = new JSONArray();

		for (FontPack fp : packs.values()) {
			arr.put(fp.toJSON());
		}

		return arr;
	}

	protected abstract InputStream openCatalog() throws IOException;

}
