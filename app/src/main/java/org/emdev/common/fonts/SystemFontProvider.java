package org.emdev.common.fonts;

import java.util.Arrays;
import java.util.List;

import org.emdev.common.fonts.data.FontFamily;
import org.emdev.common.fonts.data.FontFamilyType;
import org.emdev.common.fonts.data.FontInfo;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.common.fonts.data.FontStyle;

public class SystemFontProvider extends AbstractFontProvider {

	public static final String SYSTEM_FONT_PACK = "System";

	public SystemFontProvider() {
		super(0, SYSTEM_FONT_PACK);
	}

	@Override
	protected List<FontPack> load() {
		return Arrays.asList((FontPack) new SystemFontPack(this, SYSTEM_FONT_PACK));
	}

	private static class SystemFontPack extends FontPack {

		public SystemFontPack(SystemFontProvider manager, final String name) {
			super(manager, name);
			for (final FontFamilyType type : FontFamilyType.values()) {
				final FontFamily ff = new SystemFontFamily(type);
				this.types[type.ordinal()] = ff;
			}
		}
	}

	private static class SystemFontFamily extends FontFamily {

		public SystemFontFamily(final FontFamilyType type) {
			super(type);
			for (final FontStyle fs : FontStyle.values()) {
				final FontInfo fi = new FontInfo("", fs);
				this.fonts[fs.ordinal()] = fi;
			}
		}
	}
}
