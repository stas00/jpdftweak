package jpdftweak.cli;

import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

public class EncryptOptions implements CommandOption {

	private String userpw = "", ownerpw = null;
	private int permissions=0;
	private int encmode=0;

	public boolean supportsOption(String option) {
		return option.equals("-ownerpw") || option.equals("-userpw") || option.equals("-permissions") || option.equals("-encmode") || option.equals("-encnometadata");
	}

	public boolean setOption(String option, String value) {
		if (option.equals("-ownerpw")) {
			ownerpw = value;
		} else if (option.equals("-userpw")) {
			userpw = value;
			if (ownerpw == null) ownerpw = value;
		} else if (option.equals("-permissions")) {
			String[] perms = value.split(",");
			permissions=0;
			mainloop:
				for(String perm : perms) {
					for (int i = 0; i < PdfTweak.permissionTexts.length; i++) {
						if (perm.equals(PdfTweak.permissionTexts[i])) {
							permissions += PdfTweak.permissionBits[i];
							continue mainloop;
						}
					}
					System.err.println("Skipping invalid permission: "+perm);
				}
		} else {
			encmode = Integer.parseInt(value)+(option.equals("-encnometadata")? PdfWriter.DO_NOT_ENCRYPT_METADATA : 0);
		}
		return true;
	}
	
	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (ownerpw != null) {
			tweak.setEncryption(encmode, permissions, ownerpw.getBytes("ISO-8859-1"), userpw.getBytes("ISO-8859-1"));	
		}
	}
	
	public String getSummary() {
		return 
			" -ownerpw                Encrypt PDF and set owner password.\n"+
			" -userpw                 Encrypt PDF and set user password.\n"+
			" -permissions            Set file permissions.\n"+
			" -encmode                Set encryption mode.\n"+
			" -encnometadata          Set encryption mode and do not encrypt metadata.\n";
	}
	
	public String getHelp(String option) {
		if (option.equals("-ownerpw")) {
			return
				" -ownerpw {PASSWORD}\n" +
				"    Encrypt PDF and set owner password.";
		} else if (option.equals("-userpw")) {
			return
				" -userpw {PASSWORD}\n" +
				"    Encrypt PDF and set user password.";
		} else if (option.equals("-permissions")) {
			return
				" -PERMISSIONS {PERMISSION}[,{PERMISSION}[,...]]\n" +
				"    Set file permissions for encrypted PDF. \n"+
				"    Permission names are the same as in the GUI.";
		} else {
			return
				" -encmode {MODE}" +
				" -encnometadata {MODE}" +
				"    Set encryption mode. If -encnometadata is used,\n"+
				"    metadata is left unencrypted. Available modes:\n"+
				"        0 = 40-bit RC4 (default)\n"+
				"        1 = 128-bit RC4\n"+
				"        2 = 128-bit AES (Acrobat 7.0)";
		}
	}
}
