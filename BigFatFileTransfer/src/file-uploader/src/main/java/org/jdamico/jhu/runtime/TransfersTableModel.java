package org.jdamico.jhu.runtime;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

// This class manages the upload table's data.
class TransfersTableModel extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2740117506937325535L;

	// These are the names for the table's columns.
	private static final String[] columnNames = { "Type","Name", "Total Size", "UUID","Status",
			"Stage","Total Start Time","Total End Time" };

	// These are the classes for each column's values.
	private static final Class[] columnClasses = { String.class,String.class,String.class,String.class, 
			String.class,String.class, String.class, String.class};

	// The table's list of uploads.
	private ArrayList<ParentEntry> transferList = new ArrayList<ParentEntry>();

	// Add a new upload to the table.
	public void addTransfer(ParentEntry pe) {

		// Register to be notified when the upload changes.
		pe.addObserver(this);

		transferList.add(pe);

		// Fire table row insertion notification to table.
		fireTableRowsInserted(getRowCount(), getRowCount());
	}

	// Get a upload for the specified row.
	public ParentEntry getTransfer(int row) {
		return transferList.get(row);
	}

	// Remove a upload from the list.
	public void clearTransfer(int row) {
		transferList.remove(row);

		// Fire table row deletion notification to table.
		fireTableRowsDeleted(row, row);
	}

	// Get table's column count.
	public int getColumnCount() {
		return columnNames.length;
	}

	// Get a column's name.
	public String getColumnName(int col) {
		return columnNames[col];
	}

	// Get a column's class.
	public Class getColumnClass(int col) {
		return columnClasses[col];
	}

	// Get table's row count.
	public int getRowCount() {
		return transferList.size();
	}

	// Get value for a specific row and column combination.
	public Object getValueAt(int row, int col) {

		ParentEntry pe = transferList.get(row);
		switch (col) {
		case 0:
			return pe.getType();
		case 1: // URL
			return pe.getUrl();
		case 2: //Size
			return pe.getSize();
		case 3: //UUID
			return pe.getUUID() + "";
		case 4: // Status
			return pe.getStatus().getDisplay();
		case 5: // Stage
			return pe.getStage().getDisplay();
		case 6:
			if (pe.getTotalStartTime() != null)
				return pe.getTotalStartTime().getTime().toString();
			else
				return "";
		case 7:
			if (pe.getTotalEndTime() != null)
				return pe.getTotalEndTime().getTime().toString();
			else
				return "";
		}
		return "";
	}

	/*
	 * Update is called when a Upload notifies its observers of any changes
	 */
	public void update(Observable o, Object arg) {
		int index = transferList.indexOf(o);

		// Fire table row update notification to table.
		fireTableRowsUpdated(index, index);
	}
}