package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.TableComponent;

public class DocumentInfoTab extends Tab {

	private final MainForm mainForm;

	private JCheckBox infoChange;
	private TableComponent infoEntries;
	private JButton infoLoad, infoAdd;

	public DocumentInfoTab(MainForm mf) {
		super(new FormLayout("f:p:g, f:p", "f:p, f:p, f:p:g"));
		this.mainForm = mf;
		CellConstraints cc = new CellConstraints();
		this.add(infoChange = new JCheckBox("Change Document Info"), cc.xy(1, 1));
		infoChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b = infoChange.isSelected();
				infoLoad.setEnabled(b);
				infoAdd.setEnabled(b);
				infoEntries.setEnabled(b);
			}
		});

		this.add(infoLoad = new JButton("Load from document"), cc.xy(2, 1));
		infoLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				infoEntries.clear();
				if (mainForm.getInputFile() == null) return;
				Map<String,String> infoDictionary = mainForm.getInputFile().getInfoDictionary();
				for(Map.Entry<String,String> entry : infoDictionary.entrySet()) {
					infoEntries.addRow(entry.getKey(), entry.getValue());
				}
			}
		});
		this.add(infoAdd = new JButton("Add predefined..."), cc.xyw(1, 2, 2));
		infoAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem jmi;
				for(String name : PdfTweak.getKnownInfoNames()) {
					pm.add(jmi = new JMenuItem(name));
					jmi.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String text = ((JMenuItem)e.getSource()).getText();
							infoEntries.addRow(text, "");
						}
					});
				}
				pm.show(infoAdd, 0, infoAdd.getHeight());
			}
		});
		this.add(infoEntries = new TableComponent(new String[] {"Name", "Value"}, new Class[] {String.class, String.class} , new Object[] {"", ""}), cc.xyw(1, 3, 2));
		infoLoad.setEnabled(false);
		infoAdd.setEnabled(false);
		infoEntries.setEnabled(false);
	}

	@Override
	public String getTabName() {
		return "Document Info";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) {
		if (infoChange.isSelected()) {
			Map<String,String> newInfo = new HashMap<String,String>();
			for(int i=0; i< infoEntries.getRowCount(); i++) {
				Object[] row = infoEntries.getRow(i);
				String key = (String)row[0], value=(String)row[1];
				newInfo.put(key, value);
			}
			tweak.updateInfoDictionary(newInfo);
		}
		return tweak;
	}

}
