package org.jdamico.jhu.components;

import javax.swing.JOptionPane;

import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.FileEntry;
import org.jdamico.jhu.runtime.ParentEntry;
import org.jdamico.jhu.runtime.ProcessEntry;
import org.jdamico.jhu.runtime.BaseEntity.Stage;
import org.jdamico.jhu.runtime.FileEntry.Actions;

public class MultipleOperation implements Runnable{
	
	/*This class was created because there needed to be a higher level class that cycles
	 * through all the files for a "Checksum All" opertion (either remote or local).
	 * 
	 * This code shouldn't reside in uploadmanager for obvious reasons - it needs to handle the gui.
	 * 
	 * Ideally this code should be in ParentEntry.java or one of its subclasses, but that thread code
	 * is already set up to handle initial process (either a complete transfer or a load).
	 * 
	 * 
	 * 
	 * 
	 */
	
	/*
	 * This code should be reworked to be more object oriented (i.e. it shouldn't have all these conditional
	 * statements). 
	 * 
	 * One thing that needs to be done is to create a ParentTableModel class with a universal getEntry() method.
	 * 
	 * 
	 */
	
	private ParentEntry pe;
	private boolean bNative;
	private boolean bClient;
	private Thread thread;
	
	
	public MultipleOperation(ParentEntry pe,boolean bnative, boolean bclient) {
		this.pe = pe;
		this.bNative = bnative;
		this.bClient = bclient;
		thread = new Thread(this);
		thread.start();
		
	}
	

	public void run() {
		
		if (this.bNative == true) {
			if (this.bClient == true) {
				nativeClientChecksumAll();
			}
			else {
				nativeServerChecksumAll();
			}
				
		}
		else {
			if (this.bClient == true) {
				processClientChecksumAll();
			}
			else {
				processServerChecksumAll();
			}
			
		}
			
		
		
		
	}
	
	public void nativeClientChecksumAll() {
		
		for (int i=0;i<this.pe.tableModel3.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel3.getFileEntry(i);
			fe.setClientChecksum("");
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_NATIVE_PRE);
		}
		for (int i=0;i<this.pe.tableModel3.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel3.getFileEntry(i);
		
			fe.run(ProcessEntry.Actions.CLIENTCHECKSUM);	
			/* sleep first so that the thread gets a chance to setup */
			do {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException ie) {
					
				}
				
			} while (!fe.getDone());
			
			
		}
		
		
	}
	
	public void processClientChecksumAll() {
		
		for (int i=0;i<this.pe.tableModel2.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel2.getProcessEntry(i);
			fe.setClientChecksum("");
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_PRE);
		}
		for (int i=0;i<this.pe.tableModel2.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel2.getProcessEntry(i);
			fe.run(ProcessEntry.Actions.CLIENTCHECKSUM);	
			/* sleep first so that the thread gets a chance to setup */
			do {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException ie) {
					
				}
				
			} while (!fe.getDone());
		}
	}
	
	public void nativeServerChecksumAll() {
		
		for (int i=0;i<this.pe.tableModel3.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel3.getFileEntry(i);
			fe.setServerChecksum("");
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_NATIVE_POST);
		}
		for (int i=0;i<this.pe.tableModel3.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel3.getFileEntry(i);

			fe.run(ProcessEntry.Actions.SERVERCHECKSUM);
			
				
			
			/* sleep first so that the thread gets a chance to setup */
			do {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException ie) {
					
				}
				
			} while (!fe.getDone());
			
			if (fe.getError() != null) {
				
				
				String strMessage = "Error processing checksum. Continue or abort?";
				String strTitle = "Error";
				String[] choices = new String[2];
				choices[0] = "Continue";
				choices[1] = "Abort";
				int response = this.pe.uploadManager.dialog(strMessage, strTitle, choices);
				
				if (response == 1 || response == -1) {
					break;
				}
				else if (response != 0) {
					System.out.println("Invalid dialog response");
                    JOptionPane.showMessageDialog(null, "Unexpected response " + response);	
				}
				
				
			}
		}
	}
	
	public void processServerChecksumAll() {
		for (int i=0;i<this.pe.tableModel2.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel2.getProcessEntry(i);
			fe.setServerChecksum("");
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_POST);
		}
		for (int i=0;i<this.pe.tableModel2.getRowCount();i++) {
			FileEntry fe = this.pe.tableModel2.getProcessEntry(i);
			fe.run(ProcessEntry.Actions.SERVERCHECKSUM);	
			/* sleep first so that the thread gets a chance to setup */
			do {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException ie) {
					
				}
				
			} while (!fe.getDone());
		}
		
		
	}

}
