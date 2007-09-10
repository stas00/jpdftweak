package jpdftweak;

import javax.swing.UIManager;

import jpdftweak.cli.CommandLineInterface;
import jpdftweak.gui.MainForm;

public class Main {
	public static final String VERSION = "0.9";

	public static void main(String[] args) {
		if (args.length == 0) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {}
			new MainForm();
		} else {
			try {
				new CommandLineInterface(args);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
