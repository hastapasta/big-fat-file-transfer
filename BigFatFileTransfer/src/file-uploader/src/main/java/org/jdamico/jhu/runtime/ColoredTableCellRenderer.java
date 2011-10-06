package org.jdamico.jhu.runtime;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/*import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.vikulin.utils.Constants;*/


class ColoredTableCellRenderer extends DefaultTableCellRenderer {




	

	
	 public void setValue(Object value) {
		 	FileEntry fe = (FileEntry) value;
		 	
		 	setBackground(Color.white);
		 	if (!(fe.getServerChecksum() == null) && !(fe.getServerChecksum().isEmpty())) {
		 		if (!fe.getClientChecksum().equals(fe.getClientChecksum()))
		 			setBackground(Color.red);
		 		else
		 			setBackground(Color.green);
		 	}
		    
		    
		    super.setValue(fe.getServerChecksum());
		  }
	
	
	
	
}