package org.emdev.common.fonts;

import org.emdev.common.fonts.data.FontPack;

public interface IFontProvider extends Iterable<FontPack> {

	int getId();

	int getNewPackId();

	String getName();

	FontPack getFontPack(final String name);

}
