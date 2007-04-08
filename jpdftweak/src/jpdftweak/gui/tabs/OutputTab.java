package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;

import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;

public class OutputTab extends Tab {

	private JTextField outputFile; 
	private JCheckBox burst, uncompressed, pageMarks;
	private final MainForm mainForm;

	public OutputTab(MainForm mf) {
		super(new FormLayout("f:p, f:p:g, f:p", "f:p, f:p, f:p, f:p, f:p:g"));
		this.mainForm = mf;
		CellConstraints cc = new CellConstraints();
		this.add(new JLabel("Filename:"), cc.xy(1,1));
		this.add(outputFile = new JTextField(""), cc.xy(2, 1));
		JButton selectFile;
		this.add(selectFile = new JButton("..."), cc.xy(3, 1));
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser pdfChooser = mainForm.getPdfChooser();
				if (pdfChooser.showSaveDialog(mainForm) != JFileChooser.APPROVE_OPTION) {
					return;
				}
				if (pdfChooser.getSelectedFile().exists()) {
					if (JOptionPane.showConfirmDialog (mainForm,
							"Overwrite existing file?","Confirm Overwrite",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) return;
				}
				outputFile.setText(pdfChooser.getSelectedFile().getAbsolutePath());
			}
		});
		this.add(burst = new JCheckBox("Burst pages (use * in file name as wildcard for page number)"), cc.xyw(1, 2, 3));
		this.add(uncompressed = new JCheckBox("Save uncompressed"), cc.xyw(1, 3, 3));
		this.add(pageMarks = new JCheckBox("Remove PdfTk page marks"), cc.xyw(1, 4, 3));
		uncompressed.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				pageMarks.setText((uncompressed.isSelected()?"Add":"Remove")+
						" PdfTk page marks");
			}
		});
	}

	@Override
	public String getTabName() {
		return "Output";
	}

	@Override
	public void checkRun() throws IOException {
		if (outputFile.getText().length() == 0)
			throw new IOException("No output file selected");  
	}
	
	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		if (pageMarks.isSelected()) {
			if (uncompressed.isSelected()) {
				tweak.addPageMarks();
			} else {
				tweak.removePageMarks();
			}
		}
		tweak.writeOutput(outputFile.getText(), burst.isSelected(), uncompressed.isSelected());
		return null;
	}
}
