package org.jdamico.jhu.runtime;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

// This class manages the upload table's data.
public class FilesTableModel extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2740117506937325535L;

	// These are the names for the table's columns.
	private static final String[] columnNames = {"Name", "Size", "Progress",
			"Operation","Client Checksum","Server Checksum","Operation Start Time","Operation End Time" };

	// These are the classes for each column's values.
	private static final Class[] columnClasses = {String.class, String.class,
			JProgressBar.class, String.class, String.class, ChecksumString.class,String.class,String.class };

	// The table's list of uploads.
	private ArrayList<FileEntry> fileList = new ArrayList<FileEntry>();
	
	public void addFile(FileEntry fEntry) {

		// Register to be notified when the upload changes.
		fEntry.addObserver(this);

		fileList.add(fEntry);

		// Fire table row insertion notification to table.
		fireTableRowsInserted(getRowCount(), getRowCount());
	}
	

	public FileEntry getFileEntry(int row) {
		return fileList.get(row);
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
		return fileList.size();
	}

	// Get value for a specific row and column combination.
	public Object getValueAt(int row, int col) {

		FileEntry fEntry = fileList.get(row);
		switch (col) {
		case 0: // URL
			return fEntry.getUrl();
		case 1: // Size
			long size = fEntry.getSize();
			return (size == -1) ? "" : Long.toString(size);
		case 2: // Progress
			return new Float(fEntry.getProgress());
		case 3: // Status
			return fEntry.getStage().getDisplay();
			//return BaseEntity.STATUSES[fEntry.getStatus()];
		case 4: //Before Checksum
			return fEntry.getClientChecksum();
		case 5: //After Checksum
			//return fEntry.getAfterChecksum(); 
			return fEntry;
		case 6:
			if (fEntry.getTotalStartTime() != null)
				return fEntry.getTotalStartTime().getTime().toString();
			else
				return "";
		case 7:
			if (fEntry.getTotalEndTime() != null)
				return fEntry.getTotalEndTime().getTime().toString();
			else
				return "";
		}
		return "";
	}

	/*
	 * Update is called when a Upload notifies its observers of any changes
	 */
	public void update(Observable o, Object arg) {
		int index = fileList.indexOf(o);

		// Fire table row update notification to table.
		fireTableRowsUpdated(index, index);
	}
}