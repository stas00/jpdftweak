package jpdftweak.core;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

public class PageDimension {

	private final String name;
	private final float width;
	private final float height;

	private static PageDimension[] commonSizes = null;

	public static PageDimension[] getCommonSizes() {
		if (commonSizes == null) {
			commonSizes = new PageDimension[100];
			addCommonSize(0, "A4", PageSize.A4);
			addCommonSize(1, "Letter", PageSize.LETTER);
			addCommonSize(2, "Note", PageSize.NOTE);
			addCommonSize(3, "Legal", PageSize.LEGAL);
			addCommonSize(4, "Tabloid", PageSize.TABLOID);
			addCommonSize(5, "Executive", PageSize.EXECUTIVE);
			addCommonSize(6, "Postcard", PageSize.POSTCARD);
			addCommonSize(7, "A0", PageSize.A0);
			addCommonSize(8, "A1", PageSize.A1);
			addCommonSize(9, "A2", PageSize.A2);
			addCommonSize(10, "A3", PageSize.A3);
			addCommonSize(11, "A5", PageSize.A5);
			addCommonSize(12, "A6", PageSize.A6);
			addCommonSize(13, "A7", PageSize.A7);
			addCommonSize(14, "A8", PageSize.A8);
			addCommonSize(15, "A9", PageSize.A9);
			addCommonSize(16, "A10", PageSize.A10);
			addCommonSize(17, "B0", PageSize.B0);
			addCommonSize(18, "B1", PageSize.B1);
			addCommonSize(19, "B2", PageSize.B2);
			addCommonSize(20, "B3", PageSize.B3);
			addCommonSize(21, "B4", PageSize.B4);
			addCommonSize(22, "B5", PageSize.B5);
			addCommonSize(23, "B6", PageSize.B6);
			addCommonSize(24, "B7", PageSize.B7);
			addCommonSize(25, "B8", PageSize.B8);
			addCommonSize(26, "B9", PageSize.B9);
			addCommonSize(27, "B10", PageSize.B10);
			addCommonSize(28, "archE", PageSize.ARCH_E);
			addCommonSize(29, "archD", PageSize.ARCH_D);
			addCommonSize(30, "archC", PageSize.ARCH_C);
			addCommonSize(31, "archB", PageSize.ARCH_B);
			addCommonSize(32, "archA", PageSize.ARCH_A);
			addCommonSize(33, "American Foolscap", PageSize.FLSA);
			addCommonSize(34, "European Foolscap", PageSize.FLSE);
			addCommonSize(35, "Halfletter", PageSize.HALFLETTER);
			addCommonSize(36, "11x17", PageSize._11X17);

			addCommonSize(37, "Crown Quarto", PageSize.CROWN_QUARTO);
			addCommonSize(38, "Large Crown Quarto", PageSize.LARGE_CROWN_QUARTO);
			addCommonSize(39, "Crown Octavo", PageSize.CROWN_OCTAVO);
			addCommonSize(40, "Large Crown Octavo", PageSize.LARGE_CROWN_OCTAVO);
			addCommonSize(41, "Demy Octavo", PageSize.DEMY_OCTAVO);
			addCommonSize(42, "Demy Quarto", PageSize.DEMY_QUARTO);
			addCommonSize(43, "Royal Quarto", PageSize.ROYAL_QUARTO);
			addCommonSize(44, "Royal Octavo", PageSize.ROYAL_OCTAVO);
			addCommonSize(45, "Small paperback", PageSize.SMALL_PAPERBACK);
			addCommonSize(46, "Panguin small paperback", PageSize.PENGUIN_SMALL_PAPERBACK);
			addCommonSize(47, "Panguin large paperback", PageSize.PENGUIN_LARGE_PAPERBACK);
			commonSizes[96] =new PageDimension("Ledger", PageSize.LEDGER, false);
			commonSizes[97] =new PageDimension("ISO 7810 ID-1", PageSize.ID_1, false);
			commonSizes[98] =new PageDimension("ISO 7810 ID-2", PageSize.ID_2, false);
			commonSizes[99] =new PageDimension("ISO 7810 ID-3", PageSize.ID_3, false);
		}
		return commonSizes;
	}

	private static void addCommonSize(int index, String name, Rectangle size) {
		if (size.width() > size.height()) throw new RuntimeException(name);
		commonSizes[index*2] = new PageDimension(name+" Portrait", size, false);
		commonSizes[index*2+1] = new PageDimension(name+" Landscape", size, true);
	}

	public PageDimension(String name, float width, float height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}

	public PageDimension(String name, Rectangle size, boolean rotated) {
		this.name = name;
		this.width = rotated ? size.height() : size.width();
		this.height = rotated ? size.width() : size.height();
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return name;
	}
}
