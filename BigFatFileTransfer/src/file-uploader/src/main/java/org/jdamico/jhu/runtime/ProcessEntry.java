package org.jdamico.jhu.runtime;

import java.io.File;
import java.util.Calendar;
import java.util.Observable;

import javax.swing.JOptionPane;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.components.UpdateControlFile;
import org.jdamico.jhu.runtime.BaseEntity.Stage;
import org.vikulin.utils.Constants;

// This class uploads a file from a URL.
public class ProcessEntry extends FileEntry {

	// Max size of upload buffer.
	private static final int MAX_BUFFER_SIZE = 1024;
	
	public boolean resume;
	
	private boolean zipfile;
	
	public boolean isProcessFile() {
		return true;
	}
		
	

	
	
	
	/*
	 * This constructor is for downloads.
	 */
	public ProcessEntry(String file,ParentEntry pe) {
		
		super(file,pe);
		
		this.zipfile = false;
	}
	
	public boolean isZipFile() {
		return this.zipfile;
	}
	
	public void setZipFile(boolean input) {
		this.zipfile = input;
	}
	
	public String getRemotePath() {
		return this.getName();
	}
	
	/*public void getClientChecksum() throws Exception {
		
		Controller control = new Controller();
		control.getFileMD5(this.getName(), this.getUUID()+"", this, false);
	}*/
	
	public void updateProcessEntryProgress()
	{
	
		Controller control = new Controller();
		
		
		
		
				
					try
					{
						long lCurSize = control.getRemoteFileSize(this,true,Constants.conf.getServerHost(),Constants.conf.getServerPort());
						/*
						 * Because we are uploading and calculating the checksum in the same loop, only update
						 * the progress here if the status is set to UPLOADING.
						 */
						if (this.getStage() == Stage.UPLOADING)
							this.setProgress((int) ((lCurSize * 100 / this.getSize())));
					}
					catch (Exception e)
					{
						System.out.println(e.getMessage());
					}
					
					
				
				
				
		
	}
	
		


	





	




		
	
	

}