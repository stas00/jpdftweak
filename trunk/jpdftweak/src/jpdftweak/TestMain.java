package jpdftweak;

import java.io.File;
import java.text.RuleBasedCollator;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.core.ShuffleRule;

public class TestMain {
public static void main(String[] args) throws Exception {
	PdfTweak pdft = new PdfTweak(new PdfInputFile(new File("E:\\fasel.pdf"), ""));
	int[] ppp = new int[1];
	String rs = "1:!+1N0.25+150.0%+300.0%,+1L0.25+250.0%-100.0%,+1U0.25-250.0%-100.0%,+1R0.25-350.0%+183.0%";
	ShuffleRule[] rules = ShuffleRule.parseRuleSet(rs, ppp);
	pdft.shufflePages(ppp[0], rules);
	pdft.writeOutput("E:\\out.pdf", false, false);
}
}
