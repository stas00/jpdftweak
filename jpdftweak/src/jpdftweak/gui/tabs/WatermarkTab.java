package jpdftweak.gui.tabs;

import java.awt.Color;
import java.awt.Dimension;
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

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.TableComponent;

import com.itextpdf.text.DocumentException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class WatermarkTab extends Tab {

	private MainForm mainForm;
	private JCheckBox pdfWatermark, textWatermark, pageNumbers, watermarkUseColor, useMask, differentPageNumbers;
	private JTextField filename, pgnoSize, pgnoHOffset, pgnoVOffset, maskText;
	private JTextField watermarkText, watermarkSize, watermarkOpacity;
	private JComboBox pgnoHRef, pgnoVRef;
	private JButton fileButton, watermarkColor, load;
	private TableComponent pageNumberRanges;
	
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
		ActionListener pageNumberListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePageNumbersEnabled();
			}
		};
		pageNumbers.addActionListener(pageNumberListener);
		add(new JLabel("Font size:"), cc.xy(1, 11));
		add(pgnoSize = new JTextField("10"), cc.xyw(2, 11, 3));
		add(new JLabel("Horizontal:"), cc.xy(1, 12));
		add(pgnoHOffset = new JTextField("25"), cc.xy(2, 12));
		add(pgnoHRef = new JComboBox(new String[] {"PS points from left margin", "PS points from center", "PS points from right margin", "PS points from inner margin", "PS points from outer margin"}), cc.xyw(3, 12, 2));
		add(new JLabel("Vertical:"), cc.xy(1, 13));
		add(pgnoVOffset = new JTextField("25"), cc.xy(2, 13));
		add(pgnoVRef = new JComboBox(new String[] {"PS points from bottom margin", "PS points from center", "PS points from top margin"}), cc.xyw(3, 13, 2));
		add(useMask = new JCheckBox("Mask: "), cc.xy(1, 14));	
		useMask.addActionListener(pageNumberListener);
		add(maskText = new JTextField("Page %d of %d"), cc.xyw(2, 14, 3));
		add(differentPageNumbers = new JCheckBox("Use different page numbers"), cc.xyw(1, 15, 2));	
		differentPageNumbers.addActionListener(pageNumberListener);
		add(load = new JButton("Load from document"), cc.xyw(3, 15, 2));
		add(pageNumberRanges = PageNumberTab.buildPageNumberRanges(), cc.xyw(1, 16, 4));
		load.addActionListener(new PageNumberTab.PageNumberLoadAction(mf, pageNumberRanges));
		pageNumberRanges.getScrollPane().setPreferredSize(new Dimension(750, 100));
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
		differentPageNumbers.setEnabled(pageNumbers.isSelected());
		load.setEnabled(pageNumbers.isSelected() && differentPageNumbers.isSelected());
		pageNumberRanges.setEnabled(pageNumbers.isSelected() && differentPageNumbers.isSelected());
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
		boolean pnFlipEven = false;
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
				if (differentPageNumbers.isSelected()) {
					PageNumberTab.updatePageNumberRanges(tweak, pageNumberRanges);
				}
				run=true;
				int hIndex = pgnoHRef.getSelectedIndex();
				if (hIndex > 2) {
					hIndex = hIndex * 2 - 6;
					pnFlipEven = true;
				}
				pnPosition = pgnoVRef.getSelectedIndex()*3+hIndex;
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
			tweak.addWatermark(wmFile, wmText, wmSize, wmOpacity, wmColor, pnPosition, pnFlipEven, pnSize, pnHOff, pnVOff, mask);
		}
		if (wmFile != null)
			wmFile.close();
		return tweak;
	}
}
