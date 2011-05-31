package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jpdftweak.core.PdfToImage;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.DocumentException;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;


public class OutputTab extends Tab {

	private JTextField outputFile; 
	private JSlider qualitySlider;
	private JCheckBox burst, multipageTiff, transparent, uncompressed, pageMarks, tempfiles, optimizeSize, fullyCompressed;
	private JLabel colorLabel, compressionLabel, qualityLabel, warning;
	private JComboBox fileType, colorMode, compressionType;
	private JPanel imagePanel;
	private final MainForm mainForm;

	public OutputTab(MainForm mf){
		super(new FormLayout("f:p, f:p:g, f:p", "f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p, f:p:g"));
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
				String filename = pdfChooser.getSelectedFile().getAbsolutePath();
				filename = setCorrectExtension(filename);
				if (new File(filename).exists()) {
					if (JOptionPane.showConfirmDialog (mainForm,
							"Overwrite existing file?","Confirm Overwrite",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) return;
				}
				outputFile.setText(filename);
			}
		});
		this.add(multipageTiff = new JCheckBox("Export as Tiff multipage image"), cc.xyw(1, 2, 3));
		multipageTiff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findSharedLibrary();
				fileType.setEnabled(false);
				burst.setSelected(false);
				if(!multipageTiff.isSelected()){
					whichToEnable(0);
				} else{
					whichToEnable(100);
				}
				String filename = outputFile.getText();
				filename = setCorrectExtension(filename);
				outputFile.setText(filename);
			}
		});
		this.add(burst = new JCheckBox("Burst pages (use * in file name as wildcard for page number)"), cc.xyw(1, 3, 3));
		burst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				multipageTiff.setSelected(false);
				fileType.setEnabled(burst.isSelected());
				warning.setIcon(null);
				warning.setToolTipText("");
				if(!burst.isSelected() && !multipageTiff.isSelected()){
					whichToEnable(0);
				} else {
					whichToEnable(fileType.getSelectedIndex());
					if (fileType.getSelectedIndex() != 0){
						findSharedLibrary();
					}
				}
				String filename = outputFile.getText();
				filename = setCorrectExtension(filename);
				outputFile.setText(filename);
			}
		});
		
		this.add(new JLabel("Type:"), cc.xy(1,4));
		this.add(fileType = new JComboBox(new javax.swing.DefaultComboBoxModel(new PdfToImage.ImageType[] { PdfToImage.ImageType.PDF, PdfToImage.ImageType.JPG, PdfToImage.ImageType.PNG, PdfToImage.ImageType.GIF, PdfToImage.ImageType.PAM, PdfToImage.ImageType.PNM, PdfToImage.ImageType.BMP, PdfToImage.ImageType.TIFF })), cc.xyw(2, 4, 2));
		fileType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				whichToEnable(fileType.getSelectedIndex());

				if(fileType.getSelectedIndex() == 0){
					setOptionsEnabled(false, false, false, false, true);
					if(warning.getToolTipText().equals("<html>Images will be exported in 72 dpi")){
						warning.setIcon(null);
						warning.setToolTipText("");
					}
				}
				else{
					findSharedLibrary();
				}
				String filename = outputFile.getText();
				filename = setCorrectExtension(filename);
				outputFile.setText(filename);
				
			}
		});
		fileType.setEnabled(false);
		
		this.add(imagePanel = new JPanel(new FormLayout("f:p, f:p:g, f:p", "f:p, f:p, f:p, 30px")), cc.xyw(1, 5, 3));
		imagePanel.add(colorLabel = new JLabel("Color Mode:"), cc.xy(1, 1));
		imagePanel.add(colorMode = new JComboBox(new javax.swing.DefaultComboBoxModel(new PdfToImage.ColorMode[] { PdfToImage.ColorMode.RGB, PdfToImage.ColorMode.GRAY, PdfToImage.ColorMode.BNW, PdfToImage.ColorMode.BNWI})), cc.xyw(2, 1, 2));
		colorMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(fileType.getSelectedIndex() == 7 || multipageTiff.isSelected()){
					PdfToImage.ColorMode selectedColorMode = (PdfToImage.ColorMode) colorMode.getSelectedItem();
					switch (selectedColorMode) {
					case GRAY:{
						transparent.setEnabled(false);
						break;
					}
					case BNW:{
						transparent.setEnabled(false);
						break;
					}
					case BNWI:{
						transparent.setEnabled(false);
						break;
					}
					default:
						transparent.setEnabled(true);
						break;
					}
				}
			}
		});
		imagePanel.add(compressionLabel = new JLabel("Compression:"), cc.xy(1, 2));
		imagePanel.add(compressionType = new JComboBox(new javax.swing.DefaultComboBoxModel(new PdfToImage.TiffCompression[] { PdfToImage.TiffCompression.NONE, PdfToImage.TiffCompression.LZW, PdfToImage.TiffCompression.JPEG, PdfToImage.TiffCompression.ZLIB, PdfToImage.TiffCompression.PACKBITS, PdfToImage.TiffCompression.DEFLATE})), cc.xyw(2, 2, 2));
		compressionType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				PdfToImage.TiffCompression selectedTiffCompression = (PdfToImage.TiffCompression) compressionType.getSelectedItem();
				switch (selectedTiffCompression) {
				case JPEG:{
					setOptionsEnabled(true, true, true, colorMode.getSelectedIndex() == 0, false);
					break;
				}
				case ZLIB:{
					setOptionsEnabled(true, true, true, colorMode.getSelectedIndex() == 0, false);
					break;
				}
				default:
					setOptionsEnabled(true, true, false, colorMode.getSelectedIndex() == 0, false);
					break;
				}
			}
		});
		imagePanel.add(qualityLabel = new JLabel("Quality:"), cc.xy(1, 3));
		imagePanel.add(qualitySlider = new JSlider(), cc.xyw(2, 3, 2));
		qualitySlider.setValue(100);
		imagePanel.add(transparent = new JCheckBox("Save transparency of image background"), cc.xyw(1, 4, 2));
		imagePanel.add(warning = new JLabel(""), cc.xy(3, 4));
		imagePanel.setBorder(new TitledBorder("Burst as Image Options"));
		this.add(new JSeparator(), cc.xyw(1, 6, 3));
		this.add(uncompressed = new JCheckBox("Save uncompressed"), cc.xyw(1, 7, 3));
		this.add(pageMarks = new JCheckBox("Remove PdfTk page marks"), cc.xyw(1, 8, 3));
		uncompressed.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				pageMarks.setText((uncompressed.isSelected()?"Add":"Remove")+
						" PdfTk page marks");
			}
		});
		this.add(tempfiles = new JCheckBox("Use temporary files for intermediary results (saves RAM)"), cc.xyw(1,9,3));
		this.add(optimizeSize = new JCheckBox("Optimize PDF size (will need a lot of RAM)"), cc.xyw(1,10,3));
		this.add(fullyCompressed = new JCheckBox("Use better compression (Acrobat 6.0)"), cc.xyw(1,11,3));
		this.add(new JLabel("<html>You can use the following variables in the output filename:<br>" +
				"<tt>&lt;F></tt>: Input filename without extension<br>"+
				"<tt>&lt;FX></tt>: Input filename with extension<br>" +
				"<tt>&lt;P></tt>: Input file path without filename<br>" +
				"<tt>&lt;#></tt>: Next free number (where file does not exist)<br>" +
				"<tt>*</tt> Page number (for bursting pages)"), cc.xyw(1,12,3));
		setOptionsEnabled(false, false, false, false, true);
	}
	
	private void findSharedLibrary() {
		try {
			PdfToImage.setJavaLibraryPath();
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(mainForm, ex.getMessage(),
					"Error reading file", JOptionPane.ERROR_MESSAGE);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(mainForm, ex.getMessage(),
					"Error reading file", JOptionPane.ERROR_MESSAGE);
		}
		String sharedLibraryName = PdfToImage.checkForLibraries();
		if(sharedLibraryName != null){
			if (sharedLibraryName.equals("nojmupdf")) {
				warning.setIcon(new javax.swing.ImageIcon(getClass().getResource(
						"/warning.png")));
				warning.setToolTipText("<html>This feature is not available<br>" +
						"in compact version. If you are not<br>" +
						"using compact version verify that<br>" +
						"lib/JmuPdf.jar is present and your<br>" +
						"download was not corrupted.");
				multipageTiff.setSelected(false);
				fileType.setSelectedItem(PdfToImage.ImageType.PDF);
				fileType.setSelectedIndex(0);
			} else if(sharedLibraryName.contains(".")){
				warning.setIcon(new javax.swing.ImageIcon(getClass().getResource(
						"/warning.png")));
				warning.setToolTipText("<html>\""
						+ sharedLibraryName
						+ "\" needs to be in <br>\""
						+ PdfToImage.getJarFolder() + "\"<br>" 
						+ "to export in image file type");
				multipageTiff.setSelected(false);
				fileType.setSelectedItem(PdfToImage.ImageType.PDF);
				fileType.setSelectedIndex(0);
			}
		} else {
			warning.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/info.png")));
			warning.setToolTipText("<html>Images will be exported in 72 dpi");
		}
	}
	
	private String setCorrectExtension(String filename){
		if(!filename.equals("") && (burst.isSelected() || multipageTiff.isSelected())){
			if(filename.contains(".")){
				filename = filename.substring(0, filename.lastIndexOf("."));
			}
			if(fileType.getSelectedIndex() == 7 || multipageTiff.isSelected()){
				return filename += ".tiff";
			}else{
				return filename += "."+fileType.getSelectedItem().toString().toLowerCase();
			}
		}else if(!filename.equals("") && !burst.isSelected() && !multipageTiff.isSelected()){
			if(filename.contains(".")){
				filename = filename.substring(0, filename.lastIndexOf("."));
			}
			return filename+".pdf";
		}
		else{
			return "";
		}
	}
	
	private void whichToEnable(int option){
		if (option >= 1 && option <= 5){
			javax.swing.DefaultComboBoxModel rg = new javax.swing.DefaultComboBoxModel(new PdfToImage.ColorMode[] { PdfToImage.ColorMode.RGB, PdfToImage.ColorMode.GRAY });
			if(colorMode.getModel().getSize() != 2){
				colorMode.setModel(rg);
			}
		} else if (option > 5){
			javax.swing.DefaultComboBoxModel rgbb = new javax.swing.DefaultComboBoxModel(new PdfToImage.ColorMode[] { PdfToImage.ColorMode.RGB, PdfToImage.ColorMode.GRAY, PdfToImage.ColorMode.BNW, PdfToImage.ColorMode.BNWI });
			if(colorMode.getModel().getSize() != 4){
				colorMode.setModel(rgbb);
			}
		}
		switch (option) {
		case 0:{
			setOptionsEnabled(false, false, false, false, true);
			break;
		}
		case 1:{
			setOptionsEnabled(true, false, true, false, false);
			break;
		}
		case 2:{
			setOptionsEnabled(true,false,false,true,false);
			break;
		}
		case 3:{
			setOptionsEnabled(true,false,false,true,false);
			break;
		}
		case 4:{
			setOptionsEnabled(true,false,false,true,false);
			break;
		}
		case 5:{
			setOptionsEnabled(true,false,false,false,false);
			break;
		}
		case 6:{
			setOptionsEnabled(true,false,false,false,false);
			break;
		}
		case 7:{
			setOptionsEnabled(true,true,false,true,false);
			break;
		}
		case 100:{ //multipageTiff enabled
			setOptionsEnabled(true,true,false,true,false);
			break;
		}
		default:
			setOptionsEnabled(false, false, false, false, true);
			break;
		}
	}
	
	
	private void setOptionsEnabled(boolean color, boolean compression, boolean quality, boolean transparency, boolean pdfOptions){
		colorLabel.setEnabled(color);
		colorMode.setEnabled(color);
		compressionLabel.setEnabled(compression);
		compressionType.setEnabled(compression);
		qualityLabel.setEnabled(quality);
		qualitySlider.setEnabled(quality);
		transparent.setEnabled(transparency);
		uncompressed.setEnabled(pdfOptions);
		pageMarks.setEnabled(pdfOptions);
		optimizeSize.setEnabled(pdfOptions);
		fullyCompressed.setEnabled(pdfOptions);
	}

	private boolean matchTransparency(boolean transparency) {
		if(transparent.isEnabled()){
			return transparency;
		}
		else{
			return false;
		}
	}
	
	@Override
	public String getTabName() {
		return "Output";
	}

	@Override
	public void checkRun() throws IOException {
		if (outputFile.getText().length() == 0)
			throw new IOException("No output file selected");  
		String outputFileName = outputFile.getText();
		if(mainForm.getInputTab().getBatchLength() > 1) {
			if (!outputFileName.contains("<F>") && !outputFileName.contains("<FX>") &&!outputFileName.contains("<P>") &&!outputFileName.contains("<#>")) {
				throw new IOException("Variables in output file name required for batch mode");
			}
		}
		mainForm.getInputTab().setUseTempFiles(tempfiles.isSelected());
	}
	
	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException{
		if (pageMarks.isSelected()) {
			if (uncompressed.isSelected()) {
				tweak.addPageMarks();
			} else {
				tweak.removePageMarks();
			}
		}
		boolean matchedTransparency = matchTransparency(transparent.isSelected());
		boolean burstImages = (fileType.getSelectedIndex() != 0 && !multipageTiff.isSelected());
		PdfToImage.ImageType type = (PdfToImage.ImageType) fileType.getSelectedItem();
		if(multipageTiff.isSelected()){
			type = PdfToImage.ImageType.TIFF;
		}
		tweak.setPdfImages(new PdfToImage(burstImages,(PdfToImage.ColorMode) colorMode.getSelectedItem(), type,(PdfToImage.TiffCompression) compressionType.getSelectedItem(), qualitySlider.getValue(), matchedTransparency));
		tweak.writeOutput(outputFile.getText(),multipageTiff.isSelected(), burst.isSelected(), uncompressed.isSelected(), optimizeSize.isSelected(), fullyCompressed.isSelected());
		return null;
	}

	

}
