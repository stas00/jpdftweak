package jpdftweak.gui;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import jpdftweak.Main;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.tabs.AttachmentTab;
import jpdftweak.gui.tabs.BookmarkTab;
import jpdftweak.gui.tabs.DocumentInfoTab;
import jpdftweak.gui.tabs.EncryptSignTab;
import jpdftweak.gui.tabs.InputTab;
import jpdftweak.gui.tabs.InteractionTab;
import jpdftweak.gui.tabs.OutputTab;
import jpdftweak.gui.tabs.PageNumberTab;
import jpdftweak.gui.tabs.PageSizeTab;
import jpdftweak.gui.tabs.ShuffleTab;
import jpdftweak.gui.tabs.Tab;
import jpdftweak.gui.tabs.WatermarkTab;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.DocumentException;

public class MainForm extends JFrame {

	private InputTab inputTab = new InputTab(this);
	
	private Tab[] tabs = {
			inputTab,
			new PageSizeTab(this),
			new WatermarkTab(this),
			new ShuffleTab(this),
			new PageNumberTab(this),
			new BookmarkTab(this),
			new AttachmentTab(this),
			new InteractionTab(this),
			new DocumentInfoTab(this),
			new EncryptSignTab(this),
			new OutputTab(this),
	};
	
	private PdfInputFile inputFile;
	private JFileChooser pdfChooser = new JFileChooser();
	
	public MainForm() {
		super("jPDF Tweak "+Main.VERSION);
		setIconImage(Toolkit.getDefaultToolkit().createImage(MainForm.class.getResource("/icon.png")));
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
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		PdfTweak tweak = null;
		try {
			for(Tab tab : tabs) {
				tab.checkRun();
			}
			for(int task = 0; task < inputTab.getBatchLength(); task++) {
				inputTab.selectBatchTask(task);
				tweak = null;
				for(Tab tab: tabs) {
					tweak = tab.run(tweak);
				}
			}
			JOptionPane.showMessageDialog(this, "Finished", "JPDFTweak", JOptionPane.INFORMATION_MESSAGE);
		} catch (DocumentException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);	    
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (OutOfMemoryError ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "jPDF Tweak has run out of memory. You may configure Java so that it may use more RAM, or you can enable the Tempfile option on the output tab.", "Out of memory: "+ex.getMessage(), JOptionPane.ERROR_MESSAGE);
		} finally {
			if (tweak != null) tweak.cleanup();
		}
		this.setCursor(null);
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
	
	public InputTab getInputTab() {
		return inputTab;
	}
}
