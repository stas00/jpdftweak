package jpdftweak.cli;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfPageLabels.PdfPageLabelFormat;

public class PageNumberOption implements CommandOption {

	private List<PdfPageLabelFormat> pageLabels = new ArrayList<PdfPageLabelFormat>();
	private boolean noLabels = false;

	public boolean supportsOption(String option) {
		return option.equals("-pagenumbers") || option.equals("-loadpagenumbers");
	}

	public boolean setOption(String option, String value) throws IOException {
		if (option.equals("-loadpagenumbers")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(value), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				pageLabels.add(parsePageLabel(line));
			}
			br.close();
		} else if (value.equals("-")) {
			noLabels = true;
		} else {
			pageLabels.add(parsePageLabel(value));
		}
		return true;
	}

	private PdfPageLabelFormat parsePageLabel(String label) {
		Pattern p = Pattern.compile("([0-9]+)=(.*:)?([0-9]+)([aAiI-]?)");
		Matcher m = p.matcher(label);
		if (!m.matches()) {
			throw new RuntimeException("Cannot parse page label: " + label);
		}
		String prefix = m.group(2);
		if (prefix == null)
			prefix = "";
		if (prefix.length() > 0)
			prefix = prefix.substring(0, prefix.length() - 1);
		int nstyle = PdfPageLabels.DECIMAL_ARABIC_NUMERALS;
		if (m.group(4).length() > 0) {
			nstyle = "xIiAa-".indexOf(m.group(4).charAt(0));
		}
		return new PdfPageLabelFormat(Integer.parseInt(m.group(1)), nstyle,
				prefix, Integer.parseInt(m.group(3)));
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
	throws IOException, DocumentException {
		if (noLabels) {
			tweak.setPageNumbers(new PdfPageLabelFormat[0]);
		}
		if (pageLabels.size() > 0) {
			tweak.setPageNumbers(pageLabels
					.toArray(new PdfPageLabelFormat[pageLabels.size()]));
		}
	}

	public String getSummary() {
		return 
		" -pagenumbers            Add PDF page number format(s)\n" +
		" -loadpagenumbers        Load PDF page number format from CSV file\n";
	}

	public String getHelp(String option) {
		return 
		" -pagenumbers {NUMBERFORMAT}\n" +
		"    Add a page number format.\n" +
		" -pagenumbers -\n" +
		"    Remove all page number formats.\n" +
		" -loadpagenumbers {FILE}\n" +
		"    Load page numbers from text file.\n\n" +
		" A bookmark looks like this:\n" +
		"    page=[prefix:]pagenum[type]\n" +
		"  [page] is the page number of the first page where the format\n" +
		"         is used.\n" +
		"  [pagenum] is the number that this page should get\n" +
		"  [type] is the number type: nothing or one of 'I i A a -'.\n" +
		"  [prefix] if given (separated by a colon) is the prefix before\n" +
		"         the number.\n\n" + " Examples:\n" + " 1=Page :1\n" +
		" 10=10A\n" + " 20=Chapter:1I\n" + " 100=Appendix:100-";
	}
}
