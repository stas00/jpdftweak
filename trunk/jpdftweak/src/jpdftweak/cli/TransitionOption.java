package jpdftweak.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;

public class TransitionOption implements CommandOption {

	private List<String> transitions = new ArrayList<String>();
	
	public boolean supportsOption(String option) {
		return option.equals("-trans") || option.equals("-transition")
				|| option.equals("-transitionfile")
				|| option.equals("-transfile");
	}

	public boolean setOption(String option, String value) throws IOException {
		if (option.endsWith("file")) {
			BufferedReader br = new BufferedReader(new FileReader(value));
			String line;
			while ((line = br.readLine()) != null) {
				transitions.add(line);
			}
			br.close();
		} else {
			transitions.add(value);
		}
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		Pattern ptrn = Pattern.compile("(~?[0-9])(?:-(~?[0-9]))?:([A-Za-z_-]+),([0-9]+)(,[0-9]+)?");
		if (transitions.size()>0) {
			for(String trans : transitions) {
				Matcher m = ptrn.matcher(trans);
				if (m.matches()) {
					int from=Integer.parseInt(m.group(1).replace('~', '-'));
					int to = m.group(2) == null ? from : Integer.parseInt(m.group(2).replace('~', '-'));
					if (from< 0) from+=tweak.getPageCount()+1;
					if (to < 0) to+=tweak.getPageCount()+1;
					String transName = m.group(3).replace('_', ' ');
					int tno = Arrays.asList(PdfTweak.TRANSITION_NAMES).indexOf(transName);
					int tduration = Integer.parseInt(m.group(4));
					int pduration = m.group(5)==null ? -1 : Integer.parseInt(m.group(5).substring(1));
					if (tno == -1) {
						System.err.println("Skipping unknown transition name: "+transName);
					} else {
						for(int i=from; i<=to; i++) {
							tweak.setTransition(i, tno, tduration, pduration);
						}
					}
				} else {
					System.err.println("Skipping unparsable transition: "+trans);
				}
			}
		}
	}

	public String getSummary() {
		return 
			" -trans[ition]           Add a page transition\n"+
			" -trans[ition]file       Load page transitions from a file\n";
	}

	public String getHelp(String option) {
		StringBuffer transNames = new StringBuffer();
		for(String name : PdfTweak.TRANSITION_NAMES) {
			transNames.append("\n    "+name.replace(' ', '_'));
		}
		return 
			" -trans[ition] {FROM}[-{TO}]:{TRANS},{DURATION}[,{PAGEDUR}]\n" +
			"    Add page transition {TRANS} to pages {FROM} to {TO}.\n" +
			"    The transition will have a duration of {DURATION}.\n" +
			"    If {PAGEDUR} is given, these pages remain visible for\n" +
			"    that many seconds.\n\n"+
			" -trans[ition]file {FILENAME}\n" +
			"    Load page transitions from a file. The format is the same\n" +
			"    as in the -trans command.\n\n"+
			" Available transitions:"+transNames.toString();
	}
}
