package org.emdev.common.fonts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.emdev.common.fonts.data.FontInfo;

import android.util.Log;

public class ExtStorageFontProvider extends AbstractCustomFontProvider {

	private final File fontsFolder;

	public ExtStorageFontProvider(final File targetAppStorage) {
		super(2, "External");

		fontsFolder = new File(targetAppStorage, "fonts");
		fontsFolder.mkdirs();
	}

	@Override
	protected final InputStream openCatalog() throws IOException {
		return null;
	}

	public final File getFontFile(final FontInfo fi) {
		return fi.path.startsWith("/") ? new File(fi.path) : new File(fontsFolder, fi.path);
	}

}
