package org.ebookdroid.common.settings.types;

import java.lang.reflect.Constructor;

import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.core.HScrollController;
import org.ebookdroid.core.SinglePageController;
import org.ebookdroid.core.VScrollController;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IViewController;
import org.emdev.utils.enums.ResourceConstant;

public enum DocumentViewMode implements ResourceConstant {

	VERTICALL_SCROLL(PageAlign.WIDTH, VScrollController.class),

	HORIZONTAL_SCROLL(PageAlign.HEIGHT, HScrollController.class),

	SINGLE_PAGE(null, SinglePageController.class);

	/** The resource value. */
	private final String resValue;

	private final PageAlign pageAlign;

	private Constructor<? extends IViewController> c;

	private DocumentViewMode(final PageAlign pageAlign, final Class<? extends IViewController> clazz) {
		this.resValue = "";
		this.pageAlign = pageAlign;
		try {
			this.c = clazz.getConstructor(IActivityController.class);
		} catch (final Exception e) {
			this.c = null;
		}
	}

	public IViewController create(final IActivityController base) {
		if (c != null) {
			try {
				return c.newInstance(base);
			} catch (final Exception e) {
			}
		}
		return null;
	}

	public String getResValue() {
		return resValue;
	}

	public static PageAlign getPageAlign(final BookSettings bs) {
		if (bs == null || bs.viewMode == null) {
			return PageAlign.AUTO;
		}
		final PageAlign defAlign = bs.viewMode.pageAlign;
		return defAlign != null ? defAlign : bs.pageAlign;
	}

	public static DocumentViewMode getByOrdinal(final int ord) {
		if (0 <= ord && ord < values().length) {
			return values()[ord];
		}
		return VERTICALL_SCROLL;
	}
}
