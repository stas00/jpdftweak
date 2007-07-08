package jpdftweak.cli;

import java.io.File;
import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;

public class SignOptions implements CommandOption {

	private String keystore=new File(System.getProperty("user.home"),".keystore").getAbsolutePath();
	private String alias = "mykey";
	private int certlevel = 0;
	private boolean visible = false;
	private String passphrase = null;
	
	public boolean supportsOption(String option) {
		return option.equals("-keystore") || option.equals("-keyalias") || 
			option.equals("-certlevel") || option.equals("-sign") || 
			option.equals("-signvisible");
	}
	
	public boolean setOption(String option, String value) {
		if (option.equals("-keystore")) {
			keystore = value;
		} else if(option.equals("-keyalias")) {
			alias = value;
		} else if (option.equals("-certlevel")) {
			certlevel = Integer.parseInt(value);
		} else {
			passphrase = value;
			visible=option.equals("-signvisible");
		}
		return true;
	}
	
	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (passphrase != null) {
			tweak.setSignature(new File(keystore), alias, passphrase.toCharArray(), certlevel, visible);
		}
	}
	
	public String getSummary() {
		return
			" -sign                   Sign PDF\n" +
			" -signvisible            Sign PDF and show signature on page 1\n" +
			" -keystore               Set keystore filename for signing\n" +
			" -keyalias               Set key alias for signing\n" +
			" -certlevel              Set certification level\n";
	}
	
	public String getHelp(String option) {
		if (option.equals("-keystore")) {
			return
				" -keystore {FILENAME}\n"+
				"    Set the keystore to get the signing key from.";
		} else if(option.equals("-keyalias")) {
			return
				" -keyalias {ALIAS}\n"+
				"    Set the alias of the signing key in the keystore.\n" +
				"    Default is 'mykey'.";
		} else if (option.equals("-certlevel")) {
			return
			" -certlevel {LEVEL}\n"+
			"    Set the certification level of the new signature.\n"+
			"    Available levels:\n"+
			"        0 = Not certified (default)\n" +
			"        1 = No changes allowed\n" +
			"        2 = Form filling allowed\n" +
			"        3 = Form filling and annotations allowed";
		} else {
			return
			" -sign {PASSPHRASE}\n"+
			" -signvisible {PASSPHRASE}\n"+
			"    Sign this pdf with the selected key.\n" +
			"    Use {PASSPHRASE} to unlock key in keystore.\n"+
			"    Signature will be visible on page 1 if -signvisible\n" +
			"    is used.";
		}
	}
}
