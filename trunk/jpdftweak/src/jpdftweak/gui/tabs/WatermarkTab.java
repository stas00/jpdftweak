package jpdftweak.gui.tabs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;

public class WatermarkTab extends Tab {

	private MainForm mainForm;
	private JCheckBox pdfWatermark, textWatermark, pageNumbers, watermarkUseColor, useMask;
	private JTextField filename, pgnoSize, pgnoHOffset, pgnoVOffset, maskText;
	private JTextField watermarkText, watermarkSize, watermarkOpacity;
	private JComboBox pgnoHRef, pgnoVRef;
	private JButton fileButton, watermarkColor;
	
	public WatermarkTab(MainForm mf) {
		super(new FormLayout("f:p, f:p:g, 80dlu, f:p", "f:p, f:p, 10dlu, f:p, f:p, f:p, f:p, f:p, 10dlu, f:p, f:p, f:p, f:p, f:p, f:p, f:p:g"));
		mainForm = mf;
		CellConstraints cc = new CellConstraints();
		add(pdfWatermark = new JCheckBox("Add first page of PDF as background watermark"), cc.xyw(1, 1, 4));
		pdfWatermark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePDFWatermarkEnabled();
			}
		});
		add(new JLabel("Filename: "), cc.xy(1, 2));
		add(filename = new JTextField(""), cc.xyw(2, 2, 2));
		filename.setEditable(false);
		add(fileButton = new JButton("..."), cc.xy(4, 2));
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser pdfChooser = mainForm.getPdfChooser();
				if (pdfChooser.showOpenDialog(mainForm) == JFileChooser.APPROVE_OPTION) {
					filename.setText(pdfChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		add(new JSeparator(), cc.xyw(1, 3, 4));
		add(textWatermark = new JCheckBox("Add transparent text watermark"), cc.xyw(1, 4, 4));
		textWatermark.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTextWatermarkEnabled();
			}
		});
		add(new JLabel("Text:"), cc.xy(1, 5));
		add(watermarkText = new JTextField("Confidential"), cc.xyw(2, 5, 3));
		add(new JLabel("Font size:"), cc.xy(1, 6));
		add(watermarkSize = new JTextField("100"), cc.xyw(2, 6, 3));
		add(new JLabel("Opacity:"), cc.xy(1, 7));
		add(watermarkOpacity = new JTextField("0.25"), cc.xyw(2, 7, 3));
		add(watermarkUseColor = new JCheckBox("Color:"), cc.xy(1, 8));
		add(watermarkColor = new JButton(""), cc.xyw(2, 8, 3));
		watermarkColor.setBackground(Color.BLACK);
		watermarkColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(mainForm, "Select Color", watermarkColor.getBackground());
				if (c != null) watermarkColor.setBackground(c);
			}
		});
		add(new JSeparator(), cc.xyw(1, 9, 4));	
		add(pageNumbers = new JCheckBox("Add page numbers"), cc.xyw(1, 10, 4));	
		pageNumbers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePageNumbersEnabled();
			}
		});
		add(new JLabel("Font size:"), cc.xy(1, 11));
		add(pgnoSize = new JTextField("10"), cc.xyw(2, 11, 3));
		add(new JLabel("Horizontal:"), cc.xy(1, 12));
		add(pgnoHOffset = new JTextField("25"), cc.xy(2, 12));
		add(pgnoHRef = new JComboBox(new String[] {"PS points from left margin", "PS points from center", "PS points from right margin"}), cc.xyw(3, 12, 2));
		add(new JLabel("Vertical:"), cc.xy(1, 13));
		add(pgnoVOffset = new JTextField("25"), cc.xy(2, 13));
		add(pgnoVRef = new JComboBox(new String[] {"PS points from bottom margin", "PS points from center", "PS points from top margin"}), cc.xyw(3, 13, 2));
		add(useMask = new JCheckBox("Mask: "), cc.xy(1, 14));	
		useMask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePageNumbersEnabled();
			}
		});
		add(maskText = new JTextField("Page %d of %d"), cc.xyw(2, 14, 3));
		updatePDFWatermarkEnabled();
		updateTextWatermarkEnabled();
		updatePageNumbersEnabled();
	}

	protected void updatePDFWatermarkEnabled() {
		fileButton.setEnabled(pdfWatermark.isSelected());
		filename.setEnabled(pdfWatermark.isSelected());
	}

	protected void updateTextWatermarkEnabled() {
		watermarkText.setEnabled(textWatermark.isSelected());
		watermarkSize.setEnabled(textWatermark.isSelected());
		watermarkOpacity.setEnabled(textWatermark.isSelected());
		watermarkUseColor.setEnabled(textWatermark.isSelected());
		watermarkColor.setEnabled(textWatermark.isSelected());
	}
	
	private void updatePageNumbersEnabled() {
		pgnoSize.setEnabled(pageNumbers.isSelected());
		pgnoHOffset.setEnabled(pageNumbers.isSelected());
		pgnoVOffset.setEnabled(pageNumbers.isSelected());
		pgnoHRef.setEnabled(pageNumbers.isSelected());
		pgnoVRef.setEnabled(pageNumbers.isSelected());
		useMask.setEnabled(pageNumbers.isSelected());
		maskText.setEnabled(pageNumbers.isSelected() && useMask.isSelected());
	}
	
	@Override
	public String getTabName() {
		return "Watermark";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		boolean run = false;
		PdfInputFile wmFile = null;
		String wmText = null;
		int wmSize=0, pnSize=0, pnPosition=-1;
		float wmOpacity=0, pnHOff=0, pnVOff=0;
		Color wmColor = null;
		String mask = null;
		try {
			if (pdfWatermark.isSelected()) {
				run = true;
				wmFile = new PdfInputFile(new File(filename.getText()), "");
			}
			if (textWatermark.isSelected()) {
				run = true;
				wmText = watermarkText.getText();
				wmSize = Integer.parseInt(watermarkSize.getText());
				wmOpacity = Float.parseFloat(watermarkOpacity.getText());
			}
			if (pageNumbers.isSelected()) {
				run=true;
				pnPosition = pgnoVRef.getSelectedIndex()*3+pgnoHRef.getSelectedIndex();
				pnSize = Integer.parseInt(pgnoSize.getText());
				pnHOff = Float.parseFloat(pgnoHOffset.getText());
				pnVOff = Float.parseFloat(pgnoVOffset.getText());
			}
			if (watermarkUseColor.isSelected()) {
				wmColor = watermarkColor.getBackground();
			}
			if (useMask.isSelected()) {
				mask = maskText.getText();
			}
		} catch (NumberFormatException ex) {
			throw new IOException("Unparsable value: "+ex.getMessage());
		}
		if (run) {
			tweak.addWatermark(wmFile, wmText, wmSize, wmOpacity, wmColor, pnPosition, pnSize, pnHOff, pnVOff, mask);
		}
		return tweak;
	}
}
