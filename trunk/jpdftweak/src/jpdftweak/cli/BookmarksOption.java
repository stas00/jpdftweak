package jpdftweak.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jpdftweak.core.PdfBookmark;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;

public class BookmarksOption implements CommandOption {

	private List<PdfBookmark> bookmarks = new ArrayList<PdfBookmark>();
	private boolean nobookmarks=false;
	
	public boolean supportsOption(String option) {
		return option.equals("-bookmark") || option.equals("-loadbookmarks");
	}
	
	public boolean setOption(String option, String value) throws IOException {
		if(option.equals("-loadbookmarks")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(value), "UTF-8"));
			String line;
			while((line=br.readLine()) != null) {
				bookmarks.add(PdfBookmark.parseBookmark(line));
			}
			br.close();
		} else if (value.equals("-")) {
			nobookmarks = true;
		} else {
			bookmarks.add(PdfBookmark.parseBookmark(value));
		}
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (nobookmarks) {
			tweak.updateBookmarks(new PdfBookmark[0]);
		} if (bookmarks.size()>0) {
			tweak.updateBookmarks(bookmarks.toArray(new PdfBookmark[bookmarks.size()]));			
		}
	}


	public String getSummary() {
		return 
			" -bookmark               Add PDF bookmark(s)\n"+
			" -loadbookmarks          Load PDF bookmarks from CSV file\n";
	}
	
	public String getHelp(String option) {
		return 
		" -bookmark {BOOKMARK}\n" +
		"    Add a pdf bookmark.\n"+
		" -bookmark -\n"+
		"    Remove all PDF bookmarks\n"+
		" -loadbookmarks {FILE}\n"+
		"    Load PDF bookmarks from CSV file.\n\n"+
		" The file format is the same used by -savebookmarks; the -bookmark\n"+
		" options needs single lines from it.";
	}
}
