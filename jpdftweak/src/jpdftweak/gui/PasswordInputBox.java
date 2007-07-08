package jpdftweak.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PasswordInputBox extends JDialog{

	private JPasswordField password;
	private boolean ok = false;

	public PasswordInputBox(JFrame parent) {
		super (parent, "Enter owner password", true);
		setLayout(new FormLayout("f:p:g, f:p, f:p", "f:p, f:p, f:p:g"));
		CellConstraints cc = new CellConstraints();
		add(password = new JPasswordField(60), cc.xyw(1, 1, 3));
		JButton b;
		add(b = new JButton("OK"), cc.xy(2, 2));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				dispose();
			}
		});
		getRootPane().setDefaultButton(b);
		add(b = new JButton("Cancel"), cc.xy(3, 2));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public char[] getPassword() {
		return ok ? password.getPassword() : null;
	}

	public static char[] askForPassword(JFrame parent) {
		PasswordInputBox pib = new PasswordInputBox(parent);
		return pib.getPassword();
	}
}
