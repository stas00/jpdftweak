package jpdftweak.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jpdftweak.core.PageDimension;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfPageRange;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.tabs.DocumentInfoTab;
import jpdftweak.gui.tabs.DummyTab;
import jpdftweak.gui.tabs.EncryptSignTab;
import jpdftweak.gui.tabs.InputTab;
import jpdftweak.gui.tabs.OutputTab;
import jpdftweak.gui.tabs.PageSizeTab;
import jpdftweak.gui.tabs.ShuffleTab;
import jpdftweak.gui.tabs.Tab;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

public class MainForm extends JFrame {

	private Tab[] tabs = {
			new InputTab(this),
			new PageSizeTab(this),
			new DummyTab("Watermark"),
			new ShuffleTab(this),
			new DummyTab("Bookmarks"),
			new DummyTab("Attachments"),
			new DummyTab("Interaction"),
			new DocumentInfoTab(this),
			new DummyTab("Misc"),
			new EncryptSignTab(this),
			new OutputTab(this),
	};
	
	private PdfInputFile inputFile;
	private JFileChooser pdfChooser = new JFileChooser();
	
	public MainForm() {
		super("jPDF Tweak 0.1");
		pdfChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "PDF files (*.pdf)";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith("pdf");
			}
		});
		setLayout(new FormLayout("f:p:g,f:p,f:p", "f:p:g,f:p"));
		CellConstraints cc = new CellConstraints();
		JTabbedPane jtp;
		add(jtp = new JTabbedPane(), cc.xyw(1, 1, 3));
		for(Tab tab: tabs) {
			jtp.addTab(tab.getTabName(), tab);
		}
		JButton run;
		add(run = new JButton("Run"), cc.xy(2, 2));
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runTweaks();
			}
		});
		JButton quit;
		add(quit = new JButton("Quit"), cc.xy(3, 2));
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		pack();
		getRootPane().setDefaultButton(run);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	protected void runTweaks() {
		try {
			for(Tab tab : tabs) {
				tab.checkRun();
			}
			PdfTweak tweak = null;
			for(Tab tab: tabs) {
				tweak = tab.run(tweak);
			}
			JOptionPane.showMessageDialog(this, "Finished", "JPDFTweak", JOptionPane.INFORMATION_MESSAGE);
		} catch (DocumentException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);	    
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public JFileChooser getPdfChooser() {
		return pdfChooser;
	}
	
	public PdfInputFile getInputFile() {
		return inputFile;
	}
	
	public void setInputFile(PdfInputFile inputFile) {
		this.inputFile = inputFile;
	}
}
