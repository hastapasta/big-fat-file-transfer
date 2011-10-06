package org.jdamico.jhu.runtime;

import java.io.File;
import java.util.Calendar;
import java.util.Observable;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

public abstract class BaseEntity extends Observable { 
	
	
	
	Calendar calTotalStartTime;
	Calendar calTotalEndTime;
	
	Calendar calStageStartTime;
	Calendar calStageEndTime;
	
	protected File fileParent;
	protected File file; // upload URL
	
	/* These are for downloads */
	protected String strFileParent;
	protected String strFile;
	
	
	
	
	

	
	public BaseEntity(String inputFileName) {
		Controller control = new Controller();
		String strFolder = control.reverseString(inputFileName);
		String strName = control.reverseString(strFolder.substring(0,strFolder.indexOf(Constants.PATH_SEPARATOR)));
		strFolder = strFolder.substring(strFolder.indexOf(Constants.PATH_SEPARATOR)+1,strFolder.length());
		strFolder = control.reverseString(strFolder);
		
		this.file = new File(inputFileName);
		this.fileParent = new File(strFolder);
		
		this.strFile = strName;
		this.strFileParent = strFolder;
		
		this.calTotalStartTime = null;
		this.calTotalEndTime = null;
		
		this.calStageStartTime = null;
		this.calStageEndTime = null;
		
		
	}
	
	protected long size; // size of upload in bytes
	// private int uploaded; // number of bytes uploaded
	
	protected Stage stage;
	
	
	
	protected int progress;
	
	public enum Stage {
		INITIALIZING(0,"Initializing"),
		//CALCULATION_CHECKSUM_NATIVE_PRE(1,"Checksum - Initial Native"),
		CALCULATION_CHECKSUM_NATIVE_PRE(1,"Checksum - Initial"),
		COMPRESSING(2,"Compressing"),
		//CALCULATION_CHECKSUM_ZIP_PRE(3,"Checksum - Initial Zip"),
		CALCULATION_CHECKSUM_ZIP_PRE(3,"Checksum - Initial"),
		SPLITTING(4,"Splitting"),
		//CALCULATION_CHECKSUM_SPLIT_PRE(5,"Checksum - Initial Split"),
		CALCULATION_CHECKSUM_SPLIT_PRE(5,"Checksum - Initial"),
		UPLOADING(6,"Uploading"),
		//CALCULATION_CHECKSUM_SPLIT_POST(7,"Checksum - Final Split"),
		CALCULATION_CHECKSUM_SPLIT_POST(7,"Checksum - Final"),
		JOINING(8,"Joining"),
		//CALCULATION_CHECKSUM_ZIP_POST(9,"Checksum - Final Zip"),
		CALCULATION_CHECKSUM_ZIP_POST(9,"Checksum - Final"),
		UNCOMPRESSING(10,"Uncompressing"),
		//CALCULATION_CHECKSUM_NATIVE_POST(11,"Checksum - Final Native"),
		CALCULATION_CHECKSUM_NATIVE_POST(11,"Checksum - Final"),
		COMPLETE(12,"Complete"),
		BLANK(13,"");
		
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
	
	public Stage getStage() {
		return this.stage;
	}
	
	public void setStage(Stage input) {
		this.stage = input;
		if (this.stage != Stage.COMPLETE)
			this.setProgress(0);
		stateChanged();
	}
	
	

	

	
	
	
	
	
	
	
	public abstract UUID getUUID();

	//public abstract void pause();
	
	public String getName() {
		return strFile;
	}
	
	public File getFile() {
		return file;
	}
	
	
	
	protected String getFileName(File file) {
		String fileName = file.getName();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}
	
	public String getFolder() {
		return strFileParent;
	}
	
	// Get this upload's URL.
	public String getUrl() {
		return strFileParent + Constants.PATH_SEPARATOR + strFile;
	}
	
	public String getTempLocationPath() {
		return Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + this.getUUID() + Constants.PATH_SEPARATOR + this.strFile;
	}
	
	public String getTempLocationFolder() {
		return Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + this.getUUID() + Constants.PATH_SEPARATOR;
	}
	

	public Calendar getTotalStartTime() {
		return this.calTotalStartTime;
	}
	
	public Calendar getTotalEndTime() {
		return this.calTotalEndTime;
	}
	
	public void setTotalStartTime(Calendar input) {
		this.calTotalStartTime = input;
		stateChanged();
	}
	
	public void setTotalEndTime(Calendar input) {
		this.calTotalEndTime = input;
		stateChanged();
	}
	
	public Calendar getStageStartTime() {
		return this.calStageStartTime;
	}
	
	public Calendar getStageEndTime() {
		return this.calStageEndTime;
	}
	
	public void setStageStartTime(Calendar input) {
		this.calStageStartTime = input;
		stateChanged();
	}
	
	public void setStageEndTime(Calendar input) {
		this.calStageEndTime = input;
		stateChanged();
	}
	
	public void setStageStartTime() {
		this.calStageStartTime = Calendar.getInstance();
		stateChanged();
	}
	
	public void setStageEndTime() {
		this.calStageEndTime = Calendar.getInstance();
		stateChanged();
	}
	
	
	public void setProgress(int percent) {
		this.progress = percent;
		stateChanged();
	}
	
	// Get this upload's size.
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
		stateChanged();
	}
	
		
	// Get this upload's progress.
	public float getProgress() {
		return progress;// ((float) uploaded / size) * 100;
	}
	
	// Notify observers that this upload's status has changed.
	protected void stateChanged() {
		setChanged();
		notifyObservers();
	}
	
	
	
	/*public void updateJoinProgress()
	{
		
		Controller control = new Controller();
		try {
			long lCurSize = control.getRemoteFileSize(this,true,Constants.conf.getServerHost(),Constants.conf.getServerPort());
			this.setProgress((int) ((lCurSize * 100 / this.getSize())));
		
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}*/

	
	
	public void updateRemoteChecksumProgress()
	{
		
		Controller control = new Controller();
		ObjConverter converter = new ObjConverter();
		
		
		
		try {
			String strProgressFile = this.getName();
			int nProgress = (int) control.getRemoteProgressValue(this, Constants.conf.getServerHost(), Constants.conf.getServerPort());
			
			this.setProgress(nProgress);

			
			//pe.setProgress((int) ((lCurSize * 100 / pe.getSize())));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public void updateSplitProgress()
	{
		
		Controller control = new Controller();
		
		
			
			
				
				try {
					long lCurSize = control.getTotalProcessingFileSize(this.getName(),this.getUUID()+"");
					int progress = (int) ((lCurSize *100) / this.getSize());
					this.setProgress(progress);
					//System.out.println("Total split size: " + lCurSize + ", File Size: " + curTransfer.getSize() + ",Percent Done: " + progress);
					
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
			
				
			
		
		
	}
	
	
	
	/*public void updateLocalChecksumProgress()
	{
		
		Controller control = new Controller();
		ObjConverter converter = new ObjConverter();
		
		
		
		try {
			String strProgressFile = this.getName();
			int nProgress = control.getLocalProgressValue(strProgressFile);
			
			this.setProgress(nProgress);
		
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}*/

	//public abstract void cancel();
	
}
