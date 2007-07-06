package jpdftweak.cli;

import java.io.IOException;

import com.lowagie.text.DocumentException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

public class RotateOption implements CommandOption {

	private int portraitCount=0, landscapeCount=0;
	private boolean explicit = false, set = false;
	
	public boolean supportsOption(String option) {
		return option.matches("-rot(exp|ate(explicitly)?)?");
	}
	
	public boolean setOption(String option, String value) {
		if (set) {
			System.err.println("Error: more than one -rotate option used.");
			return false;
		} 
		if (option.contains("exp"))
			explicit = true;
		if (value.length() == 1) {
			portraitCount = landscapeCount = fromchar(value.charAt(0));
		} else {
			portraitCount = fromchar(value.charAt(0));
			landscapeCount = fromchar(value.charAt(1));
		}
		set = true;
		return true;
	}

	private int fromchar(char c) {
		switch(c) {
		case 'k': case 'K': case '0': return 0;
		case 'r': case 'R': case '1': return 1;
		case 'u': case 'U': case '2': return 2;
		case 'l': case 'L': case '3': return 3;
		}
		System.err.println("Unknown rotation \""+c+"\", ignoring.");
		return 0;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile) throws DocumentException, IOException {
		if (portraitCount != 0 || landscapeCount != 0)
			tweak.rotatePages(portraitCount, landscapeCount);
		if (explicit) tweak.removeRotation();
	}

	public String getSummary() {
		return 
			" -rot[ate]               Rotate pages, optionally separately for portrait and\n" +
			"                         landscape.\n"+
			" -rot[ate]exp[licitly]   The same, but change real content and not the page's\n" +
			"                         /Rotate attribute.\n";
	}
	
	public String getHelp(String option) {
		String details = "\n"+
			"    If only one value is given, it is used for both portrait and\n" +
			"    landscape pages.\n"+
			"    \n"+
			"    Available options for {X} and {Y}:\n"+
			"        K, k, or 0: Keep page (rotate 0 degrees)\n"+
			"        R, r, or 1: Rotate right (rotate 90 degrees)\n"+
			"        U, u, or 2: Turn upside down (rotate 180 degrees)\n"+
			"        L, l, or 3: Rotate left (rotate 270 degrees)\n"+
			"    \n"+
			"    Example: -rotate KL\n"+
			"        Rotate all landscape pages left, so that all pages will be portrait\n"+
			"        afterwards.";
		if (option.contains("exp")) {
			return
				" -rot[ate]exp[licitly] {X}{Y}\n" +
				"    Rotate portrait pages by {X} and landscape pages by {Y}.\n" +
				"    This will change real content (and destroy interactive features)\n" +
				"    but will not cause problems with tools that do not understand the /Rotate\n" +
				"    attribute. Can also be used to remove the rotate attribute from a file\n" +
				"    while keeping its appearance constant (use 'K' rotation for that).\n"+details;
		} else {
			return
				" -rot[ate] {X}{Y}\n" +
				"    Rotate portrait pages by {X} and landscape pages by {Y}.\n" +
				"    This will change the /Rotation option (and keep interactive features)\n" +
				"    but might cause problems with tools that do not understand this option.\n"+details;
		}
	}

}
