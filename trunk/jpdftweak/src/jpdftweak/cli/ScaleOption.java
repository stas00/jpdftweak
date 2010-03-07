package jpdftweak.cli;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.itextpdf.text.DocumentException;

public class ScaleOption implements CommandOption {

	boolean enabled, center, preserveRatio;
	float width, height;
	public boolean supportsOption(String option) {
		return option.equals("-scale");
	}

	public boolean setOption(String option, String value) {
		if (enabled) {
			System.err.println("Error: more than one -scale option used.");
			return false;
		}
		Matcher m = Pattern.compile("([0-9.]+),([0-9.]+)(,[cr]*)?").matcher(value);
		if (!m.matches()) {
			System.err.println("Error: Could not parse -scale option: "+value);
			return false;
		}
		enabled = true;
		width = Float.parseFloat(m.group(1));
		height = Float.parseFloat(m.group(2));
		String opts = m.group(3);
		if (opts == null) opts="";
		center = opts.contains("c");
		preserveRatio = !opts.contains("r");
		return true;
	}
	
	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (enabled)
			tweak.scalePages(width, height, center, preserveRatio);
	}

	public String getSummary() {
		return " -scale                  Scale pages\n";
	}

	public String getHelp(String option) {
		return 
			" -scale {WIDTH},{HEIGHT}[,{OPTIONS}]\n"+
			"    Scale all pages to fit to new page size.\n"+
			"    If {OPTIONS} includes \"c\", center pages instead of enlarging them,\n"+
			"    \"r\" in {OPTIONS} ignores aspect ratio.\n"+
			"    Specify {WIDTH} and {HEIGHT} in postscript points.";
	}
}
