package jpdftweak;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import jpdftweak.cli.CommandLineInterface;
import jpdftweak.gui.MainForm;

public class Main {
	public static final String VERSION = "1.0";

	public static void main(String[] args) {
		String missingLib = findMissingLibName();
		if (args.length == 0) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {}
			if (missingLib != null) {
				JOptionPane.showMessageDialog(null, "The required file lib/"+missingLib+".jar could not be loaded.\nVerify that the file is present and your download was not corrupted.", "JPDFTweak", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					e.printStackTrace();
					JDialog exceptionDialog = new JDialog((Frame)null, "An unexpected error occurred while running jPDF Tweak.");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					pw.write("Please send this error report to <schierlm@users.sourceforge.net>.\n" +
							"Try to provide specific information about when this error occured.\n\n" +
							"jPDF Tweak version: " + VERSION + "\n" +
							"Java version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor")+")\n" +
						    "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " +	System.getProperty("os.arch")+")\n\n");
					e.printStackTrace(new PrintWriter(sw));
					JTextArea jta = new JTextArea(sw.toString(), 20, 80);
					jta.setBackground(new Color(0xff, 0xbb, 0xbb));
					jta.setEditable(false);
					exceptionDialog.add(new JScrollPane(jta), BorderLayout.CENTER);
					exceptionDialog.pack();
					exceptionDialog.setLocationRelativeTo(null);
					exceptionDialog.setVisible(true);
				}
			});
			new MainForm();
		} else {
			if (missingLib != null) {
				System.out.println("The required file lib/"+missingLib+".jar could not be loaded.");
				System.out.println("Verify that the file is present and your download was not corrupted.");
				return;
			}
			try {
				new CommandLineInterface(args);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static String findMissingLibName() {
		String result = null;
		try {
			result = "itext";
			Class.forName("com.itextpdf.text.DocumentException");
			result = "forms";
			Class.forName("com.jgoodies.forms.layout.FormLayout");
			result = "bcprov";
			Class.forName("org.bouncycastle.asn1.ASN1OctetString");
			result = "bcmail";
			Class.forName("org.bouncycastle.cms.CMSEnvelopedData");
			result = "bctsp";
			Class.forName("org.bouncycastle.tsp.TSPException");
			result = null;
		} catch (Throwable t) {
			// nothing to do here
		}
		return result;
	}
}
