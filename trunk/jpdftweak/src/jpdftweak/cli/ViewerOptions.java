package jpdftweak.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.core.ViewerPreference;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfWriter;

public class ViewerOptions implements CommandOption {

	private int pagemode = -1, pagelayout = -1;
	private Map<PdfName, PdfObject> optionalPrefs = new HashMap<PdfName, PdfObject>();
	
	public boolean supportsOption(String option) {
		return option.equals("-pagemode") || option.equals("-pagelayout") || option.equals("-viewpref");
	}

	public boolean setOption(String option, String value) {
		if (option.equals("-pagemode")) {
			pagemode = Arrays.asList(new String[]{"None", "Outline", "Thumbnails", "Full_Screen", "Optional_Content", "Attachments"}).indexOf(value);
			if (pagemode == -1) {
				System.err.println("Invalid page mode: "+value);
				return false;
			}
		} else if (option.equals("-pagelayout")) {
			pagelayout = Arrays.asList(new String[]{"Single_Page", "One_Column", "Two_Columns_Left", "Two_Columns_Right", "Two_Pages_Left", "Two_Pages_Right"}).indexOf(value);
			if (pagelayout == -1) {
				System.err.println("Invalid page layout: "+value);
				return false;
			}
		} else if (option.equals("-viewpref")) {
			int pos = value.indexOf('=');
			if (pos == -1) {
				System.err.println("Invalid viewer preference: "+value);
				return false;
			}
			String pname = value.substring(0, pos).replace('_', ' ');
			String pvalue = value.substring(pos+1);
			for(ViewerPreference vp : ViewerPreference.SUPPORTED_VIEWER_PREFERENCES) {
				if (vp.getName().equals(pname)) {
					for(PdfObject v : vp.getPossibleValues()) {
						if (v.toString().equals(pvalue)) {
							optionalPrefs.put(vp.getInternalName(), v);
							return true;
						}
					}
				}
			}
			System.err.println("Invalid viewer preference: "+value);
			return false;
		}
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (pagemode != -1 || pagelayout != -1 || optionalPrefs.size() > 0) {
			if (pagemode == -1) pagemode = 0;
			if (pagelayout == -1) pagelayout=0;
			int simplePrefs = (PdfWriter.PageModeUseNone << pagemode) +
				(PdfWriter.PageLayoutSinglePage << pagelayout);
			tweak.setViewerPreferences(simplePrefs, optionalPrefs);
		}
	}

	public String getSummary() {
		return 
			" -pagemode               Set page mode (viewer preference)\n"+
			" -pagelayout             Set page layout (viewer preference)\n"+
			" -viewpref               Set additional viewer preferences\n";
	}

	public String getHelp(String option) {
		return
			" -pagemode {PAGEMODE}\n"+
			" -pagelayout {PAGELAYOUT}\n"+
			" -viewpref {NAME}={VALUE}\n"+
			"    Set viewer preferences. Names and values are like in the GUI,\n" +
			"    except that spaces are replaced by underscores.";
	}
}
