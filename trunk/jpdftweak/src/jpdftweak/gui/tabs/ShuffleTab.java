package jpdftweak.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import jpdftweak.core.PageDimension;
import jpdftweak.core.PdfTweak;
import jpdftweak.core.ShuffleRule;
import jpdftweak.core.ShuffleRule.PageBase;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.PreviewPanel;
import jpdftweak.gui.TableComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;

public class ShuffleTab extends Tab {

	private PreviewPanel pp;
	
	private JCheckBox shufflePages;
	private JButton updatePreview, use;
	private JComboBox previewFormat, preset;
	private JTextField pagesPerPass, configString;
	private TableComponent shuffleRulesTable;
	
	private int shufflePagesPerPass;
	private ShuffleRule[] shuffleRules;

	private final MainForm mf;
	
	public ShuffleTab(MainForm mf) {
		super(new BorderLayout());
		this.mf = mf;
		JPanel panel1 = new JPanel(new FormLayout("f:p, f:p:g, f:p, f:p", "f:p, f:p, f:p, 10dlu, f:p, f:p:g"));
		JPanel panel2 = new JPanel(new BorderLayout());
		JSplitPane jsp;
		add(jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2), BorderLayout.CENTER);
		jsp.setResizeWeight(0.5);
		CellConstraints cc = new CellConstraints();
		panel2.add(previewFormat = new JComboBox(), BorderLayout.NORTH);
		panel2.add(pp = new PreviewPanel(), BorderLayout.CENTER);
		for(PageDimension d : PageDimension.getCommonSizes()) {
			previewFormat.addItem(d);
		}
		previewFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pp.setPageFormat((PageDimension)previewFormat.getSelectedItem());
			}
		});
		previewFormat.setSelectedIndex(0);
		pp.setPreferredSize(new Dimension(50,100));
		previewFormat.setMinimumSize(new Dimension(50, previewFormat.getMinimumSize().height));
		panel1.add(shufflePages = new JCheckBox("Shuffle pages"), cc.xyw(1, 1, 4));
		shufflePages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnabledState();
			}
		});
		panel1.add(new JLabel("Preset: "), cc.xy(1, 2));
		panel1.add(preset = new JComboBox(), cc.xyw(2, 2, 3));
		panel1.add(new JLabel("Config string: "), cc.xy(1, 3));
		panel1.add(configString = new JTextField(), cc.xyw(2, 3, 2));
		for(String p : ShuffleRule.predefinedRuleSets) {
			preset.addItem(p.substring(p.indexOf('=')+1));
		}
		preset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String p : ShuffleRule.predefinedRuleSets) {
					if (p.endsWith("="+(String)preset.getSelectedItem())) {
						configString.setText(p.substring(0, p.indexOf('=')));
						parseConfigString();
					}
				}
			}
		});
		panel1.add(use = new JButton("Use"), cc.xy(4, 3));
		use.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parseConfigString();
			}
		});
		panel1.add(new JSeparator(), cc.xyw(1, 4, 4));
		panel1.add(new JLabel("Each pass covers  "), cc.xy(1, 5));
		panel1.add(pagesPerPass = new JTextField("1"), cc.xy(2, 5));
		panel1.add(new JLabel("  page(s)"), cc.xy(3, 5));
		panel1.add(updatePreview = new JButton("Update"), cc.xy(4,5));
		updatePreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parseGUI();
			}
		});
		panel1.add(shuffleRulesTable = new TableComponent(new String[]{"Page", "OffsetX", "OffsetY", "ScaleFactor", "Rotate", "NewPageBefore", "FrameWidth"}, new Class[] {String.class, String.class, String.class,  Double.class, String.class, Boolean.class, Double.class}, new Object[] {"+1", "0%", "0%", 1.0, "None", true, 0.0}), cc.xyw(1, 6, 4));
		JComboBox rotateValues = new JComboBox(new String[] {"None", "Left", "Upside-Down", "Right"});
		shuffleRulesTable.getTable().getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(rotateValues));
		shuffleRulesTable.getScrollPane().setPreferredSize(new Dimension(400, 100));
		preset.setSelectedIndex(0);
		updateEnabledState();
	}
	
	protected void parseGUI() {
		StringBuffer sb = new StringBuffer();
		try {
			int pages = Integer.parseInt(pagesPerPass.getText());
			sb.append(pages+":");
			for (int i = 0; i < shuffleRulesTable.getRowCount(); i++) {
				Object[] row = shuffleRulesTable.getRow(i);
				String tmp = (String)row[0];
				PageBase pb = PageBase.ABSOLUTE;
				if (tmp.startsWith("+")) {
					pb = PageBase.BEGINNING; tmp = tmp.substring(1);
				} else if (tmp.startsWith("-")) {
					pb = PageBase.END; tmp = tmp.substring(1);
				}
				int page = Integer.parseInt(tmp);
				tmp = (String) row[1];
				boolean oxp = false, oyp = false;
				if (tmp.endsWith("%")) {
					oxp = true;
					tmp = tmp.substring(0, tmp.length()-1);
				}
				double ox = Double.parseDouble(tmp);
				tmp = (String) row[2];
				if (tmp.endsWith("%")) {
					oyp = true;
					tmp = tmp.substring(0, tmp.length()-1);
				}
				double oy = Double.parseDouble(tmp);	
				double scale = (Double) row[3];
				char rotate = ((String)row[4]).charAt(0);
				boolean npb = (Boolean)row[5];
				double fw = (Double) row[6];
				ShuffleRule rule = new ShuffleRule(npb, pb, page, rotate, scale, ox, oxp, oy, oyp, fw);
				if (i > 0) sb.append(",");
				sb.append(rule.toString());
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(mf, "Unparsable option: "+ex.getMessage());
			return;
		} catch (NullPointerException ex) {
			JOptionPane.showMessageDialog(mf, "Please fill in all the fields.");
			return;			
		}
		configString.setText(sb.toString());
		parseConfigString();
	}

	private void parseConfigString() {
		try {
			parseConfigStringInternal();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(mf, "Unparsable config string: "+ex.getMessage());
		}
	}
	
	private void parseConfigStringInternal() {
		String cstr = configString.getText();
		int[] ppp = new int[1];
		ShuffleRule[] rules = ShuffleRule.parseRuleSet(cstr, ppp);
		int pages = ppp[0];		
		pp.setConfig(rules);
		pagesPerPass.setText(""+pages);
		shuffleRulesTable.clear();
		for (int i = 0; i < rules.length; i++) {
			shuffleRulesTable.addRow(rules[i].getPageString(), rules[i].getOffsetXString(), rules[i].getOffsetYString(), rules[i].getScale(), rotateName(rules[i].getRotate()), rules[i].isNewPageBefore(), rules[i].getFrameWidth());
		}
		shufflePagesPerPass = pages;
		shuffleRules = rules;	
	}

	private String rotateName(char rotate) {
		switch(rotate) {
		case 'N': return "None";
		case 'L': return "Left";
		case 'R': return "Right";
		case 'U': return "Upside-Down";
		default: throw new IllegalArgumentException();
		}
	}

	private void updateEnabledState() {
		boolean b = shufflePages.isSelected();
		updatePreview.setEnabled(b);
		use.setEnabled(b);
		previewFormat.setEnabled(b);
		preset.setEnabled(b);
		pagesPerPass.setEnabled(b);
		configString.setEnabled(b);
		shuffleRulesTable.setEnabled(b);
	}

	@Override
	public String getTabName() {
		return ("Shuffle/N-up");
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		if (shufflePages.isSelected()) {
			tweak.shufflePages(shufflePagesPerPass, shuffleRules);
		}
		return tweak;
	}

}
