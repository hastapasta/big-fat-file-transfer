package org.jdamico.jhu.runtime;

import java.util.UUID;

import javax.swing.JOptionPane;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.components.ParentPartition;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

public abstract class ParentEntry extends BaseEntity implements Runnable {
	
	public UploadManager uploadManager;

	
	protected String strType;
	
	protected boolean bDirectory;
	
	public boolean resume;
	
	private UUID idOne;
	
	protected Status status;
	
	private volatile Thread thread;
	
	public ProcessingFilesTableModel tableModel2;
	public FilesTableModel tableModel3;
	
	public FilePartition partitioning;
	
	public enum Status {
		
		
		PAUSED(0,"Paused"),
		COMPLETE(1,"Complete"),
		CANCELED(2,"Canceled"),
		ERROR(3,"Error"),
		RUNNING(4,"Running"),
		STOPPED(5,"Stopped");
		
		private int code;
		private String display;
		
		private Status(int c, String d) {
			code = c;
			display = d;
		}
		
		public int getCode() {
			return code;
		}
		
		public String getDisplay() {
			return display;
		}
		
		
		
	}
	
	public ParentEntry(String file,UploadManager uploadmanager, String strType) {
		super(file);
		this.uploadManager = uploadmanager;
		this.resume = resume;
		this.strType = strType;
		this.bDirectory = this.getFile().isDirectory();
		
		
		
		
		
	}
	
	// Set this upload's status.
	public void setStatus(Status status) {
		if (this.status==Status.ERROR) return;
		this.status = status;
		stateChanged(); 
	}
	
	// Mark this upload as having an error.
	public void error() {
		this.status = Status.ERROR;
		stateChanged();
	}
	
	public String getType() {
		return strType;
	}
	
	public void setUUID() {
		idOne = UUID.randomUUID();
	}
	
	public void setUUID(String input) {
		idOne = UUID.fromString(input);
	}

	public UUID getUUID() {
		return this.idOne;
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public void setDirectory(boolean input) {
		bDirectory = input;
	}
	
	public boolean isDirectory() {
		return bDirectory;
	}
	
	public void transfer() {
		thread = new Thread(this);
		thread.start();
	}
	
	public void updateRemoteSplitProgress()
	{
		
		Controller control = new Controller();
		try {
			long lCurSize = control.getRemoteTotalProcessingFileSize(this,Constants.conf.getServerHost(),Constants.conf.getServerPort());
			this.setProgress((int) ((lCurSize * 100 / this.getSize())));
			this.repaint();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	
	
	
	
	public void updateLocalJoinProgress()
	{
		
		Controller control = new Controller();
		try {
			long lCurSize = control.getFileSize(this.getName(),Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR);
			this.setProgress((int) ((lCurSize * 100 / this.getSize())));
			this.repaint();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public void updateProcessEntitiesProgress()
	{
	
		Controller control = new Controller();
		
		
		
		
				for (int j=1;j<this.tableModel2.getRowCount();j++)
				{
					ProcessEntry pe = this.tableModel2.getProcessEntry(j);
					
					try
					{
						long lCurSize = control.getRemoteFileSize(pe,false,Constants.conf.getServerHost(),Constants.conf.getServerPort());
						/*
						 * Because we are uploading and calculating the checksum in the same loop, only update
						 * the progress here if the status is set to UPLOADING.
						 */
						if (pe.getStage() == Stage.UPLOADING)
							pe.setProgress((int) ((lCurSize * 100 / pe.getSize())));
					}
					catch (Exception e)
					{
						System.out.println(e.getMessage());
					}
					
					
				}
				
				
		
	}
	
	// Pause this upload.
	public void pause() {
		this.status = Status.PAUSED;
		stateChanged();
	}
	
	public void repaint() {
		uploadManager.repaint();
	}
	
	// Cancel this upload.
	public void cancel() {
		this.status = Status.CANCELED;
		if (thread != null)
			thread.interrupt();
		stateChanged();
	}
	
	public void error(String message) {
		this.status = Status.ERROR;
		stateChanged();
		JOptionPane.showMessageDialog(uploadManager, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	public void updateDownloadProcessEntitiesProgress()
	{
	
		Controller control = new Controller();
		
		
		
		
				for (int j=0;j<this.tableModel2.getRowCount();j++)
				{
					ProcessEntry pe = this.tableModel2.getProcessEntry(j);
					
					try
					{
						long lCurSize = control.getFileSize(pe.getName(),Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR);
						/*
						 * Because we are downloading and calculating the checksum in the same loop, only update
						 * the progress here if the status is set to DOWNLOADING.
						 */
						if (pe.getStage() == Stage.UPLOADING)
							pe.setProgress((int) ((lCurSize * 100 / pe.getSize())));
					}
					catch (Exception e)
					{
						System.out.println(e.getMessage());
					}
					
					
				}
				
				
		
	}
	
	
	
	
	

}
