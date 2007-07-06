package jpdftweak.cli;

import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.core.ShuffleRule;

import com.lowagie.text.DocumentException;

public class ShuffleOption implements CommandOption {

	String shuffleRule = null;
	public boolean supportsOption(String option) {
		return option.equals("-shuffle");
	}
	
	public boolean setOption(String option, String value) {
		if (shuffleRule != null) {
			System.err.println("Error: more than one -shuffle option used.");
			return false;
		} 
		shuffleRule = value;
		return true;
	}
	
	public void run(PdfTweak tweak, PdfInputFile masterFile)
		throws IOException, DocumentException {
		
		if (shuffleRule != null) {
			int[] passLength = new int[1];
			ShuffleRule[] rules = ShuffleRule.parseRuleSet(shuffleRule, passLength);
			tweak.shufflePages(passLength[0], rules);
		}
	}

	public String getSummary() {
		return " -shuffle                Shuffle pages\n";
	}

	public String getHelp(String option) {
		return 
		" -shuffle {RULE}\n"+
		"   shuffle all pages. {RULE} uses the same syntax as used in the GUI.";
	}
}
