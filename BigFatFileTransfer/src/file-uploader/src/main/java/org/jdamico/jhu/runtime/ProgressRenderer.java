package org.jdamico.jhu.runtime;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.vikulin.utils.Constants;*/

// This class renders a JProgressBar in a table cell.
class ProgressRenderer extends JProgressBar implements TableCellRenderer {




	// Constructor for ProgressRenderer.
	public ProgressRenderer(int min, int max) {
		super(min, max);
	}

	/*
	 * Returns this JProgressBar as the renderer for the given table cell.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// Set JProgressBar's percent complete value.
		Float tmp = ((Float) value).floatValue();
		if (tmp == -1) {
			this.setVisible(false);
			//this.setBackground(Color.GREEN);
			//this.setForeground(this.getBackground());
		}
		else {
			setValue((int) ((Float) value).floatValue());
			if (this.isVisible() == false)
				this.setVisible(true);
		}
		return this;
	}
	
	
	
	
}