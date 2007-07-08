package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.pdf.PdfWriter;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;

public class EncryptSignTab extends Tab {

	private final MainForm mainForm;

	private JCheckBox signDocument, encryptDocument, noEncryptMetadata;
	private JComboBox  encryptMode;
	private JButton encryptLoad;
	private JTextField ownerPassword, userPassword;
	private JCheckBox[] permissionBoxes = new JCheckBox[PdfTweak.permissionBits.length];

	private JCheckBox sigVisible;
	private JTextField keystore, alias;
	private JPasswordField keyPassword;
	private JComboBox certLevel;

	public EncryptSignTab(MainForm mf) {
		super(new FormLayout("f:p, f:p:g, f:p", "f:p, f:p, f:p, f:p, f:p, f:p, 10dlu, f:p, f:p, f:p, f:p, f:p, f:p, f:p:g"));
		this.mainForm = mf;
		CellConstraints cc = new CellConstraints();
		this.add(encryptDocument = new JCheckBox("Encrypt PDF"), cc.xyw(1,1,2));
		encryptDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEncryptionControls();
			}
		});
		this.add(encryptLoad = new JButton("Load from document"), cc.xy(3,1));
		encryptLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadEncryptionData();
			}
		});
		this.add(new JLabel("Mode:"), cc.xy(1, 2));
		this.add(encryptMode = new JComboBox(new String[] {"40-bit RC4", "128-bit RC4", "128-bit AES (Acrobat 7.0)"}), cc.xyw(2, 2, 2));
		this.add(new JLabel("Owner password:"), cc.xy(1, 3));
		this.add(ownerPassword = new JTextField(""), cc.xyw(2, 3, 2));
		this.add(new JLabel("User password:"), cc.xy(1, 4));
		this.add(userPassword = new JTextField(""), cc.xyw(2, 4, 2));
		this.add(noEncryptMetadata = new JCheckBox("Do not encrypt metadata"), cc.xyw(1, 5, 3));
		JPanel p;
		this.add(p = new JPanel(new FormLayout("f:p:g, f:p:g, f:p:g, f:p:g", "f:p, f:p, ")), cc.xyw(1, 6, 3));
		for (int i=0; i<8 ; i++) {
			p.add(permissionBoxes[i] = new JCheckBox(PdfTweak.permissionTexts[i]), cc.xy(1 + (i % 4), 1 + (i / 4)));
		}
		p.setBorder(new TitledBorder("Permissions"));
		updateEncryptionControls();
		this.add(new JSeparator(), cc.xyw(1,7,3));
		this.add(signDocument = new JCheckBox("Sign PDF"), cc.xyw(1,8,3));
		signDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSignatureControls();
			}
		});
		add(new JLabel("Keystore file:"), cc.xy(1, 9));
		add(keystore = new JTextField(new File(System.getProperty("user.home"),".keystore").getAbsolutePath()), cc.xyw(2, 9, 2));
		add(new JLabel("Alias:"), cc.xy(1, 10));
		add(alias = new JTextField("mykey"), cc.xyw(2, 10, 2));
		add(new JLabel("Passphrase:"), cc.xy(1, 11));
		add(keyPassword = new JPasswordField(""), cc.xyw(2, 11, 2));
		add(new JLabel("Certification level"), cc.xy(1, 12));
		add(certLevel = new JComboBox(new String[] {"Not certified", "No changes allowed", "Form filling allowed", "Form filling and annotations allowed"}), cc.xyw(2, 12, 2));
		add(sigVisible = new JCheckBox("Show signature on page 1"), cc.xyw(1, 13,3));
		updateSignatureControls();
	}

	protected void loadEncryptionData() {
		if (mainForm.getInputFile() == null) return;
		PdfInputFile ifile = mainForm.getInputFile();
		int cm = ifile.getCryptoMode();
		if (cm != -1) encryptMode.setSelectedIndex(cm);
		noEncryptMetadata.setSelected(!ifile.isMetadataEncrypted());
		userPassword.setText(ifile.getUserPassword());
		ownerPassword.setText(ifile.getOwnerPassword());
		int permissions = ifile.getPermissions();
		for (int i = 0; i < permissionBoxes.length; i++) {
			permissionBoxes[i].setSelected((PdfTweak.permissionBits[i] & permissions) != 0);
		}
	}
	
	private void updateEncryptionControls() {
		boolean b = encryptDocument.isSelected();
		encryptLoad.setEnabled(b);
		encryptMode.setEnabled(b);
		userPassword.setEnabled(b);
		ownerPassword.setEnabled(b);
		noEncryptMetadata.setEnabled(b);
		for(int i=0; i<permissionBoxes.length; i++) {
			permissionBoxes[i].setEnabled(b);
		}
	}
	
	private void updateSignatureControls() {
		boolean b = signDocument.isSelected();
		sigVisible.setEnabled(b);
		keystore.setEnabled(b);
		alias.setEnabled(b);
		keyPassword.setEnabled(b);
		certLevel.setEnabled(b);
	}

	@Override
	public String getTabName() {
		return "Encrypt/Sign";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException {
		if (encryptDocument.isSelected()) {
			int permissions = 0;
			for (int i = 0; i < permissionBoxes.length; i++) {
				if (permissionBoxes[i].isSelected()) {
					permissions |= PdfTweak.permissionBits[i];
				}
			}
			tweak.setEncryption(encryptMode.getSelectedIndex() + (noEncryptMetadata.isSelected()?PdfWriter.DO_NOT_ENCRYPT_METADATA: 0), 
					permissions, ownerPassword.getText().getBytes("ISO-8859-1"), 
					userPassword.getText().getBytes("ISO-8859-1"));
		}
		if (signDocument.isSelected()) {
			tweak.setSignature(new File(keystore.getText()), alias.getText(), keyPassword.getPassword(), certLevel.getSelectedIndex(), sigVisible.isSelected());
		}
		return tweak;
	}

}
