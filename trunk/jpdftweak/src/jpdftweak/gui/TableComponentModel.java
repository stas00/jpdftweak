package jpdftweak.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TableComponentModel extends AbstractTableModel {

	List<Object[]> rows = new ArrayList<Object[]>();
	private final String[] columnNames;
	private final Class[] columnClasses;
	private RowListener listener;

	public TableComponentModel(String[] columnNames, Class[] columnClasses) {
		this.columnNames = columnNames;
		if (columnClasses == null) {
			columnClasses = new Class[columnNames.length];
			for (int i = 0; i < columnClasses.length; i++) {
				columnClasses[i] = Object.class;
			}
		}
		this.columnClasses = columnClasses;
		if (columnClasses.length != columnNames.length) throw new IllegalArgumentException();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return rows.get(rowIndex)[columnIndex];
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}

	public void clear() {
		if (getRowCount() > 0) {
			fireTableRowsDeleted(0, getRowCount()-1);
		}
		rows.clear();
	}

	public void addRow(Object[] params) {
		Object[] r = new Object[params.length];
		System.arraycopy(params, 0, r, 0, params.length);
		rows.add(r);
		fireTableRowsInserted(rows.size()-1, rows.size()-1);	
	}

	public void deleteRow(int row) {
		fireTableRowsDeleted(row, row);
		rows.remove(row);
	}

	public void moveRow(int row, int offset) {
		fireTableRowsDeleted(row, row);
		Object[] r = rows.remove(row);
		rows.add(row+offset, r);
		fireTableRowsInserted(row+offset, row+offset);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		rows.get(rowIndex)[columnIndex] = aValue;
		if (listener != null)
			listener.rowChanged(rowIndex, columnIndex);
	}

	public Object[] getRow(int rowIndex) {
		return rows.get(rowIndex);
	}
	
	public void setRowListener(RowListener listener) {
		this.listener = listener;
	}
	
	public static interface RowListener {
	    /**
	     * Invoked when a value in a row changed.
	     */
	    public void rowChanged(int rowIndex, int columnIndex);
	}
}
