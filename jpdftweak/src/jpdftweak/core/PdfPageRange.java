package jpdftweak.core;

import java.util.Arrays;

public class PdfPageRange {

	private final PdfInputFile inputFile;
	private final int from;
	private final int to;
	private final boolean includeOdd;
	private final boolean includeEven;
	private final IntegerList emptyBefore;

	public PdfPageRange(PdfInputFile inputFile, int from, int to, boolean includeOdd, boolean includeEven, IntegerList emptyBefore) {
		this.inputFile = inputFile;
		if (from<0) from +=inputFile.getPageCount()+1;
		if (to<0) to +=inputFile.getPageCount()+1;
		this.from = from;
		this.to = to;
		this.includeOdd = includeOdd;
		this.includeEven = includeEven;
		this.emptyBefore = emptyBefore;
	}

	public PdfInputFile getInputFile() {
		return inputFile;
	}

	public int[] getPages(int pagesBefore) {
		int emptyPagesBefore =emptyBefore.getValue()[pagesBefore % emptyBefore.getValue().length];
		int[] pages = new int[emptyPagesBefore + Math.abs(to-from)+1];
		Arrays.fill(pages, 0, emptyPagesBefore, -1);
		int length = emptyPagesBefore;
		for(int i=from; ; i+= from > to ? -1 : 1) {
			if ((i % 2 == 0 && includeEven) ||
					(i % 2 == 1 && includeOdd)) {
				pages[length++] = i;		
			}
			if (i == to) break;
		}
		if (length != pages.length) {
			int[] newPages = new int[length];
			System.arraycopy(pages, 0, newPages, 0, length);
			return newPages;
		}
		return pages;
	}
}
