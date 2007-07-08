package jpdftweak.cli;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import jpdftweak.core.PdfBookmark;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;

public class InfoOption implements CommandOption {

	String infoFilename=null;
	boolean moreInfo = false, savebookmarks=false;
	public boolean supportsOption(String option) {
		return option.equals("-info")|| option.equals("-moreinfo") || option.equals("-savebookmarks");
	}

	public boolean setOption(String option, String value) {
		infoFilename=value;
		moreInfo = option.equals("-moreinfo");
		savebookmarks = option.equals("-savebookmarks");
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
	throws IOException, DocumentException {
		if ("-".equals(infoFilename)) {
			writeInfo(System.out, masterFile, moreInfo);
		} else if (infoFilename != null) {
			writeInfo(new FileOutputStream(infoFilename), masterFile, moreInfo);
		}
	}

	private void writeInfo(OutputStream out, PdfInputFile file, boolean more) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		if (savebookmarks) {
			for (PdfBookmark b : file.getBookmarks(1)) {
				String bm = b.toString();
				String bm2 = PdfBookmark.parseBookmark(bm).toString();
				if (!bm.equals(bm2)) throw new RuntimeException(bm+" != "+bm2);
				w.write(bm);w.newLine();
			}
		} else {
			w.write("PAGECOUNT "+file.getPageCount());w.newLine();
			w.write("ENCRYPTION "+file.getCryptoMode()+(file.isMetadataEncrypted()?" METADATA":"")+printPermissions(file.getPermissions()));w.newLine();
			w.write("    OWNERPW "+file.getOwnerPassword());w.newLine();
			w.write("    USERPW "+file.getUserPassword());w.newLine();
			for (Map.Entry<String,String> entry : file.getInfoDictionary().entrySet()) {
				w.write("DOCINFO "+entry.getKey()+"="+entry.getValue());w.newLine();
			}
			if (more){
				for (PdfBookmark b : file.getBookmarks(1)) {
					String bm = b.toString();
					String bm2 = PdfBookmark.parseBookmark(bm).toString();
					if (!bm.equals(bm2)) throw new RuntimeException(bm+" != "+bm2);
					w.write("BOOKMARK "+bm);w.newLine();
				}
				Rectangle lastPage = new Rectangle(0, 0);
				int lastChange=0;
				for(int i=1; i<=file.getPageCount(); i++) {
					Rectangle current = file.getPageSize(i);
					if (current.getWidth() != lastPage.getWidth() || current.getHeight() != lastPage.getHeight()) {
						if (lastChange != 0) {
							w.write("PAGESIZE "+lastChange+"-"+i+" "+lastPage.getWidth()+"x"+lastPage.getHeight()); w.newLine();
						}
						lastPage = current;
						lastChange=i;
					}
				}
				if (lastChange != 0) {
					w.write("PAGESIZE "+lastChange+"-"+file.getPageCount()+" "+lastPage.getWidth()+"x"+lastPage.getHeight()); w.newLine();
				}
			}
		}
		w.flush();
		if (out != System.out) w.close();
	}

	private String printPermissions(int permissions) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < PdfTweak.permissionTexts.length; i++) {
			if ((permissions & PdfTweak.permissionBits[i]) != 0) {
				sb.append(" ").append(PdfTweak.permissionTexts[i]);
			}
		}
		return sb.toString();
	}

	public String getSummary() {
		return 
			" -info                   Show information about a PDF file.\n"+
			" -moreinfo               Show more information about a PDF file.\n"+
			" -savebookmarks          Save PDF bookmarks to CSV file\n";
	}

	public String getHelp(String option) {
		String desc;
		if (option.equals("-info")) {
			desc = "Write information about the input PDF file";
		} else if (option.equals("-moreinfo")) {
			desc = "Write more information about the input PDF file";
		} else if (option.equals("-savebookmarks")) {
			desc = "Save PDF bookmarks";
		} else {
			throw new RuntimeException();
		}
		return
			" "+option+" {TEXTFILE}\n" +
			"    "+desc+" to {TEXTFILE}.\n" +
			" "+option+" -\n" +
			"    "+desc+" to standard output.";
	}
}
