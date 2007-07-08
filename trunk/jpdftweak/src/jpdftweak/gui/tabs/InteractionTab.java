package jpdftweak.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.table.TableColumn;

import jpdftweak.core.PdfTweak;
import jpdftweak.core.ViewerPreference;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.TableComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfWriter;

public class InteractionTab extends Tab {
	private JCheckBox[] optionalPrefCheck = new JCheckBox[ViewerPreference.SUPPORTED_VIEWER_PREFERENCES.length];
	private JComboBox[] optionalPrefValue = new JComboBox[ViewerPreference.SUPPORTED_VIEWER_PREFERENCES.length];
	private TableComponent transitions;
	private JCheckBox addTransitions, addPrefs;
	private JComboBox pageMode, pageLayout;
	
	public InteractionTab(MainForm mf) {
		super(new BorderLayout());
		JPanel panel1 = new JPanel(new BorderLayout());
		panel1.add(addTransitions = new JCheckBox("Add page transitions"), BorderLayout.NORTH);
		ActionListener updateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnabledState();
			}
		};
		addTransitions.addActionListener(updateListener);
		panel1.add(transitions = new TableComponent(new String[] {"First Page", "Last Page", "Transition", "TransDur.", "PageDur."},
				new Class[] {Integer.class, Integer.class, String.class, Integer.class, Integer.class},
				new Object[] {1, -1, "None", 0, -1}), BorderLayout.CENTER);
		transitions.getScrollPane().setPreferredSize(new Dimension(200, 300));
		TableColumn c = transitions.getTable().getColumnModel().getColumn(2);
		c.setPreferredWidth(200);
		c.setCellEditor(new DefaultCellEditor(new JComboBox(PdfTweak.TRANSITION_NAMES)));
		FormLayout fl;
		JPanel panel2 = new JPanel(fl = new FormLayout("f:p, f:p:g", "f:p, f:p, f:p, 10dlu"));
		CellConstraints cc = new CellConstraints();
		panel2.add(addPrefs = new JCheckBox("Set Viewer Preferences"), cc.xyw(1, 1, 2));
		addPrefs.addActionListener(updateListener);
		panel2.add(new JLabel("Page Mode: "), cc.xy(1,2));
		panel2.add(pageMode = new JComboBox(new Object[] {"None", "Outline", "Thumbnails", "Full Screen", "Optional Content", "Attachments"}), cc.xy(2,2));
		panel2.add(new JLabel("Page Layout: "), cc.xy(1,3));
		panel2.add(pageLayout = new JComboBox(new Object[] {"Single Page", "One Column", "Two Columns Left", "Two Columns Right", "Two Pages Left", "Two Pages Right"}), cc.xy(2,3));
		panel2.add(new JSeparator(), cc.xyw(1, 4, 2));
		for(int i=0; i<ViewerPreference.SUPPORTED_VIEWER_PREFERENCES.length; i++) {
			ViewerPreference vp = ViewerPreference.SUPPORTED_VIEWER_PREFERENCES[i];
			fl.appendRow(new RowSpec("f:p"));
			panel2.add(optionalPrefCheck[i] = new JCheckBox(vp.getName()+": "), cc.xy(1,i+5));
			panel2.add(optionalPrefValue[i] = new JComboBox(vp.getPossibleValues()), cc.xy(2,i+5));			
			optionalPrefCheck[i].addActionListener(updateListener);
		}
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
		jsp.setResizeWeight(0.5f);
		add(jsp, BorderLayout.CENTER);
		updateEnabledState();
	}
	
	protected void updateEnabledState() {
		transitions.setEnabled(addTransitions.isSelected());
		boolean b = addPrefs.isSelected();
		pageMode.setEnabled(b);
		pageLayout.setEnabled(b);
		for (int i = 0; i < optionalPrefCheck.length; i++) {
			optionalPrefCheck[i].setEnabled(b);
			optionalPrefValue[i].setEnabled(b && optionalPrefCheck[i].isSelected());
		}
	}

	@Override
	public String getTabName() {
		return "Interaction";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		if (addTransitions.isSelected()) {
			for (int i = 0; i < transitions.getRowCount(); i++) {
				Object[] row = transitions.getRow(i);
				int from = (Integer)row[0];
				int to = (Integer)row[1];
				String transition = (String)row[2];
				int trans = Arrays.asList(PdfTweak.TRANSITION_NAMES).indexOf(transition);
				if (trans==-1) throw new RuntimeException();
				int duration = (Integer)row[3];
				int pdur = (Integer)row[4];
				if (from<0) from +=tweak.getPageCount()+1;
				if (to<0) to += tweak.getPageCount()+1;
				for (int j = from; j <= to; j++) {
					tweak.setTransition(j, trans, duration, pdur);
				}
			}
		}
		if (addPrefs.isSelected()) {
			int simplePrefs = 
				(PdfWriter.PageLayoutSinglePage << pageLayout.getSelectedIndex()) +
				(PdfWriter.PageModeUseNone << pageMode.getSelectedIndex());
			Map<PdfName,PdfObject> optionalPrefs = new HashMap<PdfName,PdfObject>();
			for (int i = 0; i < optionalPrefCheck.length; i++) {
				if (optionalPrefCheck[i].isSelected()) {
					optionalPrefs.put(ViewerPreference.SUPPORTED_VIEWER_PREFERENCES[i].getInternalName(), 
							(PdfObject)optionalPrefValue[i].getSelectedItem());
				}
			}
			tweak.setViewerPreferences(simplePrefs, optionalPrefs);
		}
		return tweak;
	}
}
