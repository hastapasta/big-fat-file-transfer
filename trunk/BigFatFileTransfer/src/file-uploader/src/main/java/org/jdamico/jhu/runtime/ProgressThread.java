package org.jdamico.jhu.runtime;

import org.jdamico.jhu.components.Controller;


public class ProgressThread extends Thread {
	
	private BaseEntity be;
	private int type;
	
	public boolean done = false;
	
	public static final int PROCESS_FILE_UPLOAD = 0;
	//public static final int LOCAL_FILE_CHECKSUM = 1;
	public static final int MAIN_FILE_JOINING = 2;
	public static final int MAIN_FILE_SPLITTING = 4;
	public static final int REMOTE_MAIN_FILE_SPLITTING = 5;
	public static final int REMOTE_FILE_CHECKSUM = 6;
	public static final int PROCESS_FILE_DOWNLOAD = 7;
	public static final int LOCAL_MAIN_FILE_JOINING = 8;
	public static final int REMOTE_FILE_UNCOMPRESSING = 8;

	
	public ProgressThread(BaseEntity be,int type)
	{
		this.be = be;
		this.type = type;
		Controller control = new Controller();
		control.deleteFilesRemote("progress.txt",be.getUUID()+"",false,false);	
	}
	
	
	public void run() {
		try {
			
			while (!done == true)
			{
		
				if (type == ProgressThread.PROCESS_FILE_UPLOAD)
				{
					ProcessEntry pe = (ProcessEntry)be;
					pe.updateProcessEntryProgress();						
				}
				else if (type == ProgressThread.MAIN_FILE_JOINING)
				{
					ProcessEntry pe = (ProcessEntry)be;
					pe.updateJoinProgress();		
				}
				else if (type == ProgressThread.MAIN_FILE_SPLITTING)
				{
					be.updateSplitProgress();
				}
				else if (type == ProgressThread.REMOTE_MAIN_FILE_SPLITTING)
				{
					//Only can be done with a download
					Download dl = (Download)be;
					dl.updateRemoteSplitProgress();
					if (be.getProgress() == 100)
						this.done=true;
				}
				else if (type == ProgressThread.REMOTE_FILE_CHECKSUM)
				{
					//Download dl = (Download)be;
					be.updateRemoteChecksumProgress();	
					if (be.getProgress() == 100)
						this.done=true;
				}
				else if (type == ProgressThread.REMOTE_FILE_UNCOMPRESSING)
				{
					FileEntry fe = (FileEntry) be;
					fe.updateRemoteUncompressProgress();
					if (be.getProgress() == 100)
						this.done=true;
				}
				/* This is no longer necessary. Pass in the fileentity to the checksum function
				 * to display the progress.
				 */
				/*else if (type == ProgressThread.LOCAL_FILE_CHECKSUM)
				{
					//Download dl = (Download)be;
					be.updateLocalChecksumProgress();			
				}*/
				else if (type == ProgressThread.PROCESS_FILE_DOWNLOAD)
				{
					ParentEntry pe = (ParentEntry)be;
					pe.updateDownloadProcessEntitiesProgress();
				}
				else if (type == ProgressThread.LOCAL_MAIN_FILE_JOINING)
				{
					ParentEntry pe = (ParentEntry)be;
					pe.updateLocalJoinProgress();
				}
				
				
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException ie)
				{
					if (done == true)
						break;
					
				}
			}
			
	
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	
		
	}
}
