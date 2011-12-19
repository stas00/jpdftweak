package jpdftweak.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfBookmark {
	private final int depth;
	private final String title;
	private final boolean open;
	private final boolean bold;
	private final boolean italic;
	private final int page;
	private final String pagePosition;
	private final String moreOptions;

	
	public PdfBookmark(int depth, String title, boolean open, int page) {
		this(depth, title, open, page, "", false, false, null);
	}
	
	public PdfBookmark(int depth, String title, boolean open, int page, String pagePosition, boolean bold, boolean italic, String moreOptions) {
		this.depth = depth;
		this.title = title;
		this.open = open;
		this.bold = bold;
		this.italic = italic;
		this.page = page;
		this.pagePosition = pagePosition;
		if (moreOptions != null && moreOptions.length()==0)
			moreOptions = null;
		this.moreOptions = moreOptions;
		if (moreOptions != null && !moreOptions.equals(makeString(parseString(moreOptions)))) {
			throw new IllegalArgumentException("More options incorrect");
		}
	}

	public int getDepth() {
		return depth;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isBold() {
		return bold;
	}
	
	public boolean isItalic() {
		return italic;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public String getMoreOptions() {
		return moreOptions;
	}
	
	public int getPage() {
		return page;
	}

	public String getPagePosition() {
		return pagePosition;
	}

	protected HashMap<String,Object> toBookmark(List<HashMap<String,Object>> kids) {
		HashMap<String,Object> result = new HashMap<String,Object>();
		result.putAll(parseString(moreOptions));
		if (kids != null) {
			result.put("Kids", kids);
		}
		if (page != 0)  {
			result.put("Action", "GoTo");
			result.put("Page", page+(pagePosition.length() == 0 ? "" : " "+pagePosition));
		}
		result.put("Open", open?"true":"false");
		String style = ((italic?"italic ":"")+(bold?"bold":"")).trim();
		if (bold || italic) result.put("Style", style);
		result.put("Title", title);
		return result;
	}
	

	public PdfBookmark shiftPageNumber(int offset) {
		if (page == 0) return this;
		return new PdfBookmark(depth, title, open, page+offset, pagePosition, bold, italic, moreOptions);
	}
	
	protected static List<HashMap<String,Object>> makeBookmarks(PdfBookmark[] pdfBookmarks) throws IOException {
		return makeBookmarks(pdfBookmarks, 1, 0, pdfBookmarks.length);
	}
	
	protected static List<HashMap<String,Object>> makeBookmarks(PdfBookmark[] pdfBookmarks, int depth, int from, int to) throws IOException {
		List<HashMap<String,Object>> result = new ArrayList<HashMap<String,Object>>();
		int pos = from;
		while (pos < to) {
			PdfBookmark b = pdfBookmarks[pos];
			if (b.getDepth() != depth) throw new IOException("Invalid depth value");
			int endPos = pos+1;
			while(endPos < to && pdfBookmarks[endPos].depth>depth) endPos++;
			List<HashMap<String,Object>> kids = null;
			if (endPos > pos+1) {
				kids = makeBookmarks(pdfBookmarks, depth+1, pos+1, endPos);
			}
			result.add(b.toBookmark(kids));
			pos = endPos;
		}
		return result;
	}
	
	protected static List<PdfBookmark> parseBookmarks(List<HashMap> bookmarks, int depth) {
		List<PdfBookmark> result = new ArrayList<PdfBookmark>();
		if (bookmarks == null) return result;
		for(HashMap bookmark : bookmarks) {
			fillInBookmark(result, bookmark, depth);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected static void fillInBookmark(List<PdfBookmark> result, HashMap bookmark_, int depth) {
		HashMap bookmark = new HashMap(bookmark_);
		List<HashMap> kids = (List<HashMap>) bookmark.remove("Kids");
		String pagePosition;
		int pageNumber;
		if ("GoTo".equals(bookmark.get("Action")) && bookmark.get("Page") != null) {
			bookmark.remove("Action");
			String page = ((String)bookmark.remove("Page")).trim();
			int pos = page.indexOf(" ");
			if (pos == -1) {
				pageNumber = Integer.parseInt(page);
				pagePosition="";
			} else {
				pageNumber = Integer.parseInt(page.substring(0, pos));
				pagePosition = page.substring(pos+1);
			}
			if (pageNumber == 0) throw new RuntimeException();
		} else {
			pageNumber=0;
			pagePosition="";
		}
		boolean open = true, bold = false, italic = false;
		if (bookmark.get("Open") != null) {
			if ("false".equals(bookmark.remove("Open"))) open = false;
		}
		String style = (String)bookmark.remove("Style");
		if (style != null && style.indexOf("italic") != -1) {
			italic = true;
		}
		if (style != null && style.indexOf("bold") != -1) {
			bold = true;
		}
		String title = (String)bookmark.remove("Title");
		result.add(new PdfBookmark(depth, title, open, pageNumber, pagePosition, bold, italic, makeString(bookmark)));
		if (kids != null) {
			for(HashMap hm : kids) {
				fillInBookmark(result, hm, depth+1);
			}
		}
	}
	private static String makeString(HashMap<?,?> bookmark) {
		if (bookmark.size() == 0) return null;
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<?, ?> entry : bookmark.entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			if (sb.length()>0) sb.append(" ");
			sb.append(key).append("=\"");
			sb.append(value.replaceAll("\"", "\"\""));
			sb.append("\"");
		}
		return sb.toString();
	}
	
	private static HashMap<String,String> parseString(String toParse) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (toParse == null) return result;
		toParse = toParse.trim();
		while(toParse.length()> 0) {
			int pos = toParse.indexOf("=\"");
			String key = toParse.substring(0, pos);
			String value="";
			toParse = toParse.substring(pos+2);
			while (true) {
				pos = toParse.indexOf('"');
				if (pos < toParse.length()-1 && toParse.charAt(pos+1)=='"') {
					value += toParse.substring(0, pos+1);
					toParse= toParse.substring(pos+2);
				} else {
					value +=toParse.substring(0, pos);
					toParse = toParse.substring(pos+1);
					break;
				}
			}
			result.put(key, value);
			toParse = toParse.trim();
		}
		return result;
	}

	public static List<PdfBookmark> buildBookmarks(List<PdfPageRange> ranges) {
		int offset = 0;
		List<PdfBookmark> result = new ArrayList<PdfBookmark>();
		for (PdfPageRange range: ranges) {
			result.add(new PdfBookmark(1, range.getInputFile().getFile().getName(), true, offset+1));
			List<PdfBookmark> bs = range.getInputFile().getBookmarks(2);
			for (PdfBookmark b : bs) {
				result.add(b.shiftPageNumber(offset));
			}
			offset += range.getPages(offset).length;
		}
		return result;
	}

	public static PdfBookmark parseBookmark(String line) {
		Matcher m = Pattern.compile("(-?[0-9]+);(O?B?I?);([^;]*);(-?[0-9]+)( [^;]+)?(;[^;]*)?").matcher(line);
		if (!m.matches()) throw new RuntimeException("Cannot parse bookmark");
		String flags = m.group(2);
		return new PdfBookmark(Integer.parseInt(m.group(1)), unescape(m.group(3)),
				flags.contains("O"), Integer.parseInt(m.group(4)), 
				(m.group(5) == null ? "" : unescape(m.group(5).substring(1))),
				flags.contains("B"),flags.contains("I"), 
				(m.group(6) == null ? null : unescape(m.group(6).substring(1))));		
	}
	
	@Override
	public String toString() {
		return depth+";"+(open?"O":"")+(bold?"B":"")+(italic?"I":"")+";"+
			escape(title)+";"+page+(pagePosition.length() == 0 ? "" : " ")+
			escape(pagePosition)+(moreOptions==null ? "" : (";"+escape(moreOptions)));
	}
	
	private static String escape(String str) {
		StringBuffer sb = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++) {
			if ((str.charAt(i)) <32 || "\\;\"'".contains(""+str.charAt(i))) {
				char chr = str.charAt(i);
				sb.append("\\").append(chr<16?"0":"").append(Integer.toHexString(chr).toUpperCase());
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}
	
	private static String unescape(String str) {
		StringBuffer sb = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i)=='\\') {
				sb.append((char)Integer.parseInt(str.substring(i+1, i+3), 16));
				i+=2;
			} else {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}
}
