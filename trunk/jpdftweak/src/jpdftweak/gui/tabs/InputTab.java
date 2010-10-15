package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jpdftweak.core.PdfBookmark;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfPageRange;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.PasswordInputBox;
import jpdftweak.gui.TableComponent;
import jpdftweak.gui.TableComponentModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.exceptions.BadPasswordException;

public class InputTab extends Tab {

	private JTextField filename;
	private JCheckBox multiFiles, batchProcessing;
	private TableComponent fileCombination;
	private JButton selectfile;
	private List<PdfInputFile> inputFiles = new ArrayList<PdfInputFile>();
	private JComboBox filesCombo;
	private final MainForm mf;
	private int batchTaskSelection = -1;
	private boolean useTempFiles = false;

	public InputTab(MainForm mf) {
		super(new FormLayout("f:p, f:p:g, f:p, f:p", "f:p, f:p, f:p:g"));
		this.mf = mf;
		CellConstraints cc = new CellConstraints();
		this.add(new JLabel("Filename"), cc.xy(1,1));
		this.add(filename = new JTextField(), cc.xy(2, 1));
		filename.setEditable(false);
		this.add(selectfile = new JButton("Select..."), cc.xy(3, 1));
		selectfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		JButton clear;
		this.add(clear = new JButton("Clear"), cc.xy(4,1));
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filesCombo.removeAllItems();
				for(PdfInputFile f : inputFiles) {
					f.close();
				}
				inputFiles.clear();
				InputTab.this.mf.setInputFile(null);
				fileCombination.clear();
				selectfile.setEnabled(true);
				multiFiles.setEnabled(!batchProcessing.isSelected());
				batchProcessing.setEnabled(!multiFiles.isSelected());
				updateFileName();
			}
		});
		this.add(multiFiles= new JCheckBox("Multiple file input / Select pages"), cc.xyw(1, 2, 2));
		ActionListener l = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (multiFiles.isSelected()) {
					batchProcessing.setEnabled(false);
					selectfile.setEnabled(true);
					fileCombination.setEnabled(true);
				} else if (batchProcessing.isSelected()) {
					multiFiles.setEnabled(false);
					selectfile.setEnabled(true);
				} else {
					batchProcessing.setEnabled(true);
					multiFiles.setEnabled(true);
					selectfile.setEnabled(inputFiles.size() == 0);
					fileCombination.setEnabled(false);
				}
			}
		};
		multiFiles.addActionListener(l);
		this.add(batchProcessing = new JCheckBox("Batch processing"), cc.xyw(3,2,2));
		batchProcessing.addActionListener(l);
		filesCombo = new JComboBox();
		this.add(fileCombination = new TableComponent(new String[] {"File", "From Page", "To Page", "Include Odd", "Include Even"}, new Class[] {PdfInputFile.class, Integer.class, Integer.class, Boolean.class, Boolean.class}, new Object[] {null, 1, -1, true, true}), cc.xyw(1, 3, 4));
		fileCombination.setEnabled(false);
		fileCombination.getTable().getColumnModel().getColumn(0).setPreferredWidth(300);
		fileCombination.getTable().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(filesCombo));
		fileCombination.setRowListener(new TableComponentModel.RowListener() {
			public void rowChanged(int rowIndex, int columnIndex) {
				if (columnIndex < 3) {
					Object[] row = fileCombination.getRow(rowIndex);
					PdfInputFile ifile = (PdfInputFile)row[0];
					int from = (Integer)row[1];
					int to = (Integer)row[2];
					int pages = ifile == null ? 1 : ifile.getPageCount();
					boolean changed = false;
					if (from > pages) {
						row[1] = columnIndex == 0 ? 1 : pages;
						changed = true;
					}
					if (from < -pages) {
						row[1] = columnIndex == 0 ? 1 : -pages;
						changed = true;
					}
					if (from == 0) {
						row[1] = 1;
						changed=true;
					}
					if (to > pages) {
						row[2] = columnIndex == 0 ? -1 : pages;
						changed = true;
					}
					if (to < -pages) {
						row[2] = columnIndex == 0 ? -1 : -pages;
						changed = true;
					}
					if (to == 0) {
						row[2] = -1;
						changed=true;
					}
					if (changed && columnIndex != 0) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								JOptionPane.showMessageDialog(InputTab.this.mf, "Entered value out of range. Value has been adjusted.", "JPDFTweak", JOptionPane.WARNING_MESSAGE);								
							}
						});
					}
				}
			}
		});
		updateFileName();
	}

	private void updateFileName() {
		String fn;
		if (inputFiles.size() == 0) {
			fn="(No file selected)";
		} else if (inputFiles.size() ==1) {
			fn = inputFiles.get(0).getFile().getName();
		} else {
			fn = "("+inputFiles.size()+" files selected)";
		}
		filename.setText(fn);
	}

	protected void selectFile() {
		JFileChooser pdfChooser = mf.getPdfChooser();
		if (multiFiles.isSelected() || batchProcessing.isSelected()) {
			pdfChooser.setMultiSelectionEnabled(true);
		}
		if (pdfChooser.showOpenDialog(mf) != JFileChooser.APPROVE_OPTION) {
			pdfChooser.setMultiSelectionEnabled(false);
			return;
		}
		File[] files = new File[] {pdfChooser.getSelectedFile()};
		if (multiFiles.isSelected() || batchProcessing.isSelected()) {
			files = pdfChooser.getSelectedFiles();
			pdfChooser.setMultiSelectionEnabled(false);
		}
		for(File file : files) {
			PdfInputFile f;
			try {
				try {
					f = new PdfInputFile(file, "");
				} catch (BadPasswordException ex) {
					try {
						char[] pwd = PasswordInputBox.askForPassword(mf);
						if (pwd == null) break;
						String password = new String(pwd);
						f = new PdfInputFile(file, password);
					} catch (BadPasswordException ex2) {
						JOptionPane.showMessageDialog(mf, "Bad owner password", "Cannot open input file", JOptionPane.WARNING_MESSAGE);
						break;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(mf, ex.getMessage(), "Error reading input file", JOptionPane.ERROR_MESSAGE );
				return;
			}
			inputFiles.add(f);
			if (inputFiles.size() == 1) mf.setInputFile(f);
			filesCombo.addItem(f);
			fileCombination.addRow(f, 1, f.getPageCount(), true, true);
		}
		if (!multiFiles.isSelected() && !batchProcessing.isSelected() && inputFiles.size() > 0)
			selectfile.setEnabled(false);
		if (inputFiles.size()>1) {
			if (batchProcessing.isSelected()) {
				batchProcessing.setEnabled(false);
			} else {
				multiFiles.setSelected(true);
				multiFiles.setEnabled(false);
			}
		}
		updateFileName();
	}

	@Override
	public String getTabName() {
		return "Input";
	}

	@Override
	public void checkRun() throws IOException {
		fileCombination.checkRun("input file");
		if (inputFiles.size() == 0) 
			throw new IOException("No input file selected");
		if (multiFiles.isSelected()) {
			for(PdfPageRange range : getPageRanges()) {
				if (range.getPages().length == 0) {
					throw new IOException("At least one input file page range contains no pages");
				}
			}
		}
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws DocumentException, IOException {
		if (batchProcessing.isSelected()) {
			inputFiles.get(batchTaskSelection).reopen();
			return new PdfTweak(inputFiles.get(batchTaskSelection), useTempFiles);
		} else if (multiFiles.isSelected()) {
			for (PdfInputFile f : inputFiles) {
				f.reopen();
			}
			List<PdfPageRange> ranges = getPageRanges();
			 return new PdfTweak(inputFiles.get(0), ranges, useTempFiles);
		} else {
			inputFiles.get(0).reopen();
			return new PdfTweak(inputFiles.get(0), useTempFiles);
		}
	}

	public List<PdfBookmark> loadBookmarks() {
		if (inputFiles.size() == 0) 
			return new ArrayList<PdfBookmark>();
		if (multiFiles.isSelected()) {
			List<PdfPageRange> ranges = getPageRanges();
			return PdfBookmark.buildBookmarks(ranges);			
		} else {
			return inputFiles.get(0).getBookmarks(1);
		}
	}

	private List<PdfPageRange> getPageRanges() {
		List<PdfPageRange> ranges = new ArrayList<PdfPageRange>();
		for(int i=0; i <fileCombination.getRowCount(); i++) {
			Object[] row = fileCombination.getRow(i);
			if (row[0] == null) continue;
			PdfInputFile ifile = (PdfInputFile)row[0];
			int from = (Integer)row[1];
			int to = (Integer)row[2];
			boolean odd = (Boolean)row[3];
			boolean even = (Boolean)row[4];
			ranges.add(new PdfPageRange(ifile, from, to, odd, even));
		}
		return ranges;
	}

	public int getBatchLength() {
		if (batchProcessing.isSelected()) {
			return inputFiles.size();
		} else {
			return 1;
		}
	}
	
	public void selectBatchTask(int batchTaskSelection) {
		this.batchTaskSelection = batchTaskSelection;
	}

	public void setUseTempFiles(boolean useTempFiles) {
		this.useTempFiles = useTempFiles;
	}
}
