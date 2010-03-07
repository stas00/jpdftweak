package jpdftweak.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.itextpdf.text.DocumentException;

public class AttachOption implements CommandOption {

	List<File> attachFiles = new ArrayList<File>();
	public boolean supportsOption(String option) {
		return option.equals("-attach");
	}

	public boolean setOption(String option, String value) {
		attachFiles.add(new File(value));
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		for (File f : attachFiles) {
			tweak.addFile(f);
		}
	}

	public String getSummary() {
		return " -attach                 Attach a file\n";
	}

	public String getHelp(String option) {
		return
		" -attach {FILENAME}\n"+
		"    Attach file {FILENAME}";
	}
}
