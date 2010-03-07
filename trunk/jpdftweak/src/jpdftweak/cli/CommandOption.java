package jpdftweak.cli;

import java.io.IOException;

import com.itextpdf.text.DocumentException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

public interface CommandOption {
	public boolean supportsOption(String option);
	public boolean setOption(String option, String value) throws IOException, DocumentException;
	public void run(PdfTweak tweak, PdfInputFile masterFile) throws IOException, DocumentException;
	public String getSummary();
	public String getHelp(String option);
}