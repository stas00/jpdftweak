package jpdftweak.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TableComponent extends JPanel {
	private final JScrollPane jsp;
	private final JButton add, up, down, delete;
	private final JTable jt;
	private final TableComponentModel tcm;
	private final Object[] sample;

	public TableComponent(String[] captions, Class[] classes, Object[] sample) {
		if (sample.length != classes.length) throw new IllegalArgumentException();
		this.sample = sample;
		setLayout(new FormLayout("f:p:g,f:p:g,f:p:g,f:p:g", "f:p:g, f:p"));
		CellConstraints cc = new CellConstraints();
		add(jsp = new JScrollPane(jt = new JTable(tcm = new TableComponentModel(captions, classes))), cc.xyw(1, 1, 4));
		jsp.setPreferredSize(new Dimension(750, 400));
		add(add = new JButton("Add"), cc.xy(1, 2));
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
					return;
				tcm.addRow(TableComponent.this.sample);
			}
		});
		jt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (jt.getEditorComponent() != null) {
						jt.getEditorComponent().addFocusListener(new FocusAdapter() {						
						public void focusLost(FocusEvent e) {
							if (jt.getCellEditor() != null)
								jt.getCellEditor().stopCellEditing();
						}
					});
				}
			}			
		});
		add(up = new JButton("Up"), cc.xy(2, 2));
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = jt.getSelectedRow();
				if (sel ==-1 || sel == 0)
					return;
				if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
					return;
				tcm.moveRow(sel, -1);
				jt.getSelectionModel().setSelectionInterval(sel-1, sel-1);		
			}
		});
		add(down = new JButton("Down"), cc.xy(3, 2));
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = jt.getSelectedRow();
				if (sel ==-1 || sel == tcm.getRowCount()-1)
					return;
				if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
					return;
				tcm.moveRow(sel, 1);
				jt.getSelectionModel().setSelectionInterval(sel+1, sel+1);		
			}
		});
		add(delete = new JButton("Delete"), cc.xy(4, 2));
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = jt.getSelectedRowCount()-1; i >= 0; i--) {
					int row = jt.getSelectedRows()[i];
					if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
						return;
					tcm.deleteRow(row);
				}
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		add.setEnabled(enabled);
		up.setEnabled(enabled);
		down.setEnabled(enabled);
		delete.setEnabled(enabled);
		jsp.setEnabled(enabled);
		jt.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public void clear() {
		if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
			return;
		tcm.clear();
	}

	public void addRow(Object... params) {
		if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing())
			return;
		tcm.addRow(params);
	}

	public JTable getTable() { return jt;}

	public int getRowCount() {
		return tcm.getRowCount();
	}

	public Object[] getRow(int rowIndex) {
		return tcm.getRow(rowIndex);
	}

	public JComponent getScrollPane() {
		return jsp;
	}
	
	public void setRowListener(TableComponentModel.RowListener listener) {
		tcm.setRowListener(listener);
	}

	public void checkRun(String strTableName) throws IOException {
		if (jt.getCellEditor() != null && !jt.getCellEditor().stopCellEditing()) {
			throw new IOException("Please finish editing the "+strTableName+" table!");
		}
	}
}
