package jpdftweak.cli;

import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.itextpdf.text.DocumentException;

public class PreserveHyperlinksOption implements CommandOption {

	boolean preserveLinks = false;

	public boolean supportsOption(String option) {
		return option.equals("-preserveLinks");
	}

	public boolean setOption(String option, String value) throws IOException, DocumentException {
		preserveLinks = true;
		return preserveLinks;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile) throws IOException, DocumentException {
		if (preserveLinks) {
			tweak.preserveHyperlinks();
		}
	}

	public String getSummary() {
		return 
		" -preserveLinks          Preserve hyperlinks (EXPERIMENTAL)\n";
	}

	public String getHelp(String option) {
		return 
		" -preserveLinks On\n" +
		"    Preserve Hyperlinks (EXPERIMENTAL).";
	}

}
