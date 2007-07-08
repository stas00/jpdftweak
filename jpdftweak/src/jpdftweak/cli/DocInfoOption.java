package jpdftweak.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;

public class DocInfoOption implements CommandOption {

	public Map<String,String> docinfo =new HashMap<String, String>();
	
	public boolean supportsOption(String option) {
		return option.equals("-docinfo");
	}
	
	public boolean setOption(String option, String value) {
		int pos = value.indexOf("=");
		if (pos == -1) {
			System.err.println("Missing equals sign in -docinfo value: "+value);
			return false;
		}
		docinfo.put(value.substring(0,pos), value.substring(pos+1));
		return true;
	}
	
	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (docinfo.size()>0) {
			tweak.updateInfoDictionary(docinfo);
		}
	}
	
	public String getSummary() {
		return 
			" -docinfo                Change document info\n";
	}
	
	public String getHelp(String option) {
		return
			" -docinfo {NAME}={VALUE}\n" +
			"    Set {NAME} to {VALUE} in document info.";
	}
}
