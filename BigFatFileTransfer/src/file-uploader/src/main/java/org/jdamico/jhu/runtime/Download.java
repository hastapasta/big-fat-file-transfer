package org.jdamico.jhu.runtime;

import java.io.File;
import java.util.Calendar;
import java.util.Observable;

import javax.swing.JOptionPane;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.DownloadFilePartition;
import org.jdamico.jhu.components.FilePartition;
import org.vikulin.utils.Constants;

// This class uploads a file from a URL.
public class Download extends ParentEntry {

	// Max size of upload buffer.
	private static final int MAX_BUFFER_SIZE = 1024;
	
	public enum Stage {
		INITIALIZING(0,"Initializing"),
		CALCULATION_CHECKSUM_NATIVE_PRE(1,"Inital Native Checksum"),
		COMPRESSING(2,"Initializing"),
		CALCULATION_CHECKSUM_ZIP_PRE(3,"Initializing"),
		SPLITTING(4,"Initializing"),
		CALCULATION_CHECKSUM_SPLIT_PRE(5,"Initializing"),
		DOWNLOADING(6,"Initializing"),
		CALCULATION_CHECKSUM_SPLIT_POST(7,"Initializing"),
		JOINING(8,"Initializing"),
		CALCULATION_CHECKSUM_ZIP_POST(9,"Initializing"),
		UNCOMPRESSING(10,"Initializing"),
		CALCULATION_CHECKSUM_NATIVE_POST(11,"Initializing");
		
		private int code;
		private String display;
		
		private Stage(int c, String d) {
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
	


	
	
	public DownloadFilePartition partitioning;

	//private String file; // upload URL

	
	// Constructor for Upload.
	public Download(String file, UploadManager uploadManager,String type) {
		
		super(file,uploadManager,type);
		
		
		
		tableModel2 = new ProcessingFilesTableModel();
		tableModel3 = new FilesTableModel();
		

		transfer();
	}

	


	
	// Upload file.
	public void run() {


		partitioning = new DownloadFilePartition(this);
		try {
			
			
			String remote = Constants.conf.getServerHost();
			int port = Constants.conf.getServerPort();// 9999;
			int partSize = Constants.conf.getChunkSize();// 500000000;
			System.out.println("Running as client [Remote Host: http://"
					+ remote + ":" + port + " (" + file + ") (" + size + ")]");
			if (resume == false)
			{
				partitioning.startNew(this.getFolder() + Constants.PATH_SEPARATOR + this.getName(), partSize, remote, port);
			}
			else
				partitioning.startLoad(this.getFolder() + Constants.PATH_SEPARATOR + this.getName(), partSize, remote, port,null);
		} catch (Exception e) {
			System.out.println("Unable to run client with these parameters.");
			e.printStackTrace();
		}
	}

	
	
	
	

	
	/*public void updateJoinProgress()
	{
		uploadManager.updateJoinProgress();
		
	}*/
	
	/*public void updateSplitProgress()
	{
		uploadManager.updateSplitProgress();
		
	}*/
	
	/*public void updateRemoteSplitProgress() {
		uploadManager.updateRemoteSplitProgress();
	}
	
	public void updateRemoteChecksumProgress() {
		uploadManager.updateRemoteChecksumProgress();
	}*/
	

}