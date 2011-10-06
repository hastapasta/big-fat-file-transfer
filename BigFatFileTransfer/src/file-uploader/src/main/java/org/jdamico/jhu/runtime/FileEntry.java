package org.jdamico.jhu.runtime;

import java.io.File;
import java.util.UUID;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.components.ParentPartition;
import org.jdamico.jhu.components.UpdateControlFile;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

public class FileEntry extends BaseEntity implements Runnable{
	
	ParentEntry pe;
	
	public String strClientChecksum;
	public String strServerChecksum;
	
	private Actions action;
	
	private boolean bDone;
	
	private String strErrorMessage;
	
	
	private volatile Thread thread;
	
	public enum Actions {
		UNCOMPRESSING(0),
		UPLOADING(1),
		SERVERCHECKSUM(2),
		CLIENTCHECKSUM(3),
		JOIN(4);
		
		
		private int code;
		
		
		private Actions(int c) {
			code = c;
			
		}
		
		public int getCode() {
			return code;
		}
		
		
		
		
		
	}
	

	
	
	
		
	public FileEntry(String file, ParentEntry pe) {
		
		super(file);
		
		//this.uploadManager = uploadManager;
		size = -1l;
				
		this.stage = Stage.INITIALIZING;
		
		this.pe = pe;
		
	}
	
	
	public ParentEntry getParentEntry() {
		return this.pe;
	}
	
	public boolean isProcessFile() {
		return false;
	}
	
	
	public void updateRemoteSplitProgress()
	{
		
		Controller control = new Controller();
		try {
			long lCurSize = control.getRemoteTotalProcessingFileSize(this,Constants.conf.getServerHost(),Constants.conf.getServerPort());
			this.setProgress((int) ((lCurSize * 100 / this.getSize())));
		
			//this.repaint();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	protected void stateChanged() {
		/*
		 * We don't want to worry about observing all 3 tables, so we'll
		 * just observe the top one and send notifications there.
		 */
		pe.stateChanged();
	}
	
	public UUID getUUID() {
		return this.pe.getUUID();
	}
	
	public void updateJoinProgress()
	{
		
		Controller control = new Controller();
		try {
			long lCurSize = control.getRemoteFileSize(this,true,Constants.conf.getServerHost(),Constants.conf.getServerPort());
			this.setProgress((int) ((lCurSize * 100 / this.getSize())));
			//this.repaint();
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
			//this.repaint();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	/*public void repaint() {
		this.pe.uploadManager.repaint();
	}*/
	
	public void setClientChecksum(String strInput) {
		this.strClientChecksum = strInput;
		stateChanged();
	}
	

	public void setServerChecksum(String strInput) {
		this.strServerChecksum = strInput;
		stateChanged();
	}
	

	public String getClientChecksum() {
		return strClientChecksum;
	}
	

	public String getServerChecksum() {
		return strServerChecksum;
	}
	
	public String getError() {
		return this.strErrorMessage;
	}
	
	public void updateRemoteUncompressProgress()
	{
		
		Controller control = new Controller();
		//ObjConverter converter = new ObjConverter();
		
		try {
			String strProgressFile = this.getName();
			long lFileSize = control.getRemoteProgressValue(this, Constants.conf.getServerHost(), Constants.conf.getServerPort());
			
			this.setProgress((int) (lFileSize *100/this.pe.getSize()));
	
					
	
		}
		catch (NumberFormatException nfe) {
			/*
			 * This exception is expected if a read of progress.txt occurs while it's being written to.
			 * In that case the http request returns a blank value.
			 */
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	
	public void run() {
		this.bDone=false;
		Controller control = new Controller();
		this.pe.setStatus(ParentEntry.Status.RUNNING);
		this.setStageStartTime();
		this.strErrorMessage = null;
		if (action == Actions.SERVERCHECKSUM)
		{
			
			this.setServerChecksum("");
			
			try {
				calculateServerChecksum();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				//this.pe.uploadManager.message("Checksum failed.");
				this.strErrorMessage = "Checksum failed";
			}
		
			
		
		}
		else if (action == Actions.CLIENTCHECKSUM) {
			
			try {
				this.setClientChecksum("");
				calculateClientChecksum();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
				
			}
			
		}
		else if (action == Actions.UNCOMPRESSING) {
			
		
			/*
			 * Clean out the uncompress dir first.
			 */
			control.deleteFilesRemote("", this.getUUID()+"", false, true);
			
			ProgressThread pThread;
			pThread = new ProgressThread(this,ProgressThread.REMOTE_FILE_UNCOMPRESSING);
			this.setStage(BaseEntity.Stage.UNCOMPRESSING);
			this.pe.setStage(BaseEntity.Stage.UNCOMPRESSING);
			pThread.start(); 
					
			try {
				control.uncompressRemoteZip(this, true);
				
				//pThread.done = true;
				//pThread.interrupt();
				
				while (pThread.done != true) {
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException ie) {
						return;
					}
				}
				
				this.pe.setProgress(100);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
				
			}
			
		}
		else if (action == Actions.UPLOADING) {
			this.setStage(BaseEntity.Stage.UPLOADING);
			ProgressThread pThread;
			this.setServerChecksum("");
			control.deleteFilesRemote(this.getName(), this.getUUID()+"", false,false);
			pThread = new ProgressThread(this,ProgressThread.PROCESS_FILE_UPLOAD);
			pThread.start();
			
			
			
			
			boolean isUploaded = false;
			
			try {
			
			isUploaded = control.uploadFiles(this.file,
					"U" + this.getUUID() + ".xml", Constants.conf.getServerHost(), Constants.conf.getServerPort(), (Upload)this.pe, 0);
			
			this.setProgress(100);
			
			
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			}
			finally {
				pThread.done = true;
				pThread.interrupt();		
			}
			
			this.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_POST);
			getServerChecksum();
				
		
		}
		else if (action == Actions.JOIN) {
			UpdateControlFile.writeControlFile(this.pe);
			ProgressThread pThread;
			pThread = new ProgressThread(this,ProgressThread.MAIN_FILE_JOINING);
			pThread.start();
			
			
			
			
			try {
				this.pe.setStage(BaseEntity.Stage.JOINING);
				this.setStage(BaseEntity.Stage.JOINING);
				
				control.consolidateFileAtRemoteHost("U" + this.getUUID() + ".xml", Constants.conf.getServerHost(), Constants.conf.getServerPort(), this.pe);
			}
			catch (Exception e) {
				
			}
			finally {
				pThread.done = true;
				pThread.interrupt();		
			}
			

			this.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_POST);
			getServerChecksum();
			
			
		}
		this.pe.setStatus(ParentEntry.Status.STOPPED);
		this.pe.setStage(BaseEntity.Stage.BLANK);
		this.setStageEndTime();
		

		
		UpdateControlFile.writeControlFile(this.pe);
		
		this.bDone=true;
	}
	
	public void run(Actions action)	{
		thread = new Thread(this);
		this.action = action;
		thread.start();
	}
	
	public boolean getDone() {
		return this.bDone;
	}
	
	public void calculateClientChecksum() throws Exception {
		
		Controller control = new Controller();
		this.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_PRE);
		String strChecksum = control.getFileMD5(this.getFile().getAbsolutePath(), this.getUUID()+"", this, false);
		this.setClientChecksum(strChecksum);
		this.setProgress(100);
		
	}
	
	public String getRemotePath() {
		//return Constants.conf.getUncompressFolderName() + Constants.PATH_SEPARATOR;
		String strStartDirectory;
		if (this.pe.isDirectory())
			strStartDirectory = this.pe.getUrl();
		else
			strStartDirectory = this.pe.getFolder();
		System.out.println("strStartDirectory: " + strStartDirectory);
		System.out.println("getUrl: " + this.getUrl());
		String strRelativeDir = this.getUrl().substring(strStartDirectory.length()+1,this.getUrl().length());
		return strRelativeDir;
	}
	
	public void calculateServerChecksum() throws Exception {
		
		//Stage st = this.getStage();
		Controller control = new Controller();
		
		
		//try {

			this.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_SPLIT_POST);
			ProgressThread pThread = new ProgressThread(this,ProgressThread.REMOTE_FILE_CHECKSUM);
			pThread.start();
			
			System.out.println("Calculating remote checksum.");
			String returnval;
			if (isProcessFile())
				returnval = control.getRemoteFileMD5(this.getRemotePath(),this.getUUID() + "",true,false,true);
			else
				returnval = control.getRemoteFileMD5(this.getRemotePath(),this.getUUID() + "",false,true,true);
			
			String ret[] = returnval.split(",");
			
			this.setProgress(100);
			pThread.done = true;
			pThread.interrupt();
			
			if (ret[1].equals("FAILED"))
				throw new Exception ("Checksum failed.");
				//pe.uploadManager.message("Checksum retreival failed.");
			else
				this.setServerChecksum(ret[0]);
		//}
		/*catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
		finally {
			//this.setStage(st);
			
		}*/
		
		
		
	}
	
	
	
	
	
	
	
	
	

	
	
	

}
