package org.jdamico.jhu.components;

import java.io.File;
import java.io.IOException;

import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.SourceFile;
import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.FileEntry;
import org.jdamico.jhu.runtime.ParentEntry;
import org.jdamico.jhu.runtime.ProcessEntry;
import org.jdamico.jhu.runtime.Upload;
import org.jdamico.jhu.runtime.UploadManager;
import org.vikulin.utils.Constants;

public class UpdateControlFile {
	
	private ParentEntry pe;
	private String controlFileName;
	private UploadManager um;
	
	
	public UpdateControlFile(ParentEntry pe,UploadManager um) {
		this.pe = pe;
		this.um = um;
	}
	
	public String update() {
		//this.controlFileName = fileName;
		//this.run();
		
		return "";
		
	}
	
	
	public static void writeControlFile(ParentEntry pe) {
		
		/*
		 * Because of how this code evolved, there are 2 lists for the processing files. 
		 * 
		 * There's partialFileList in FilePartition.java for writing out to the xml control file
		 * and there's ProcessingTableModel for displaying the list in the gui. 
		 * 
		 * ProcessingTableModel contains the most current info so we have to copy that over
		 * to partialFileList before writing out the control file.
		 */
		SourceFile sourceFile;
		
		sourceFile = new SourceFile("U" + pe.getUUID(), pe.getUrl(),pe.getUUID()+"", "");
		sourceFile.setStage(pe.getStage().getCode());
		sourceFile.setTotalSize(pe.getSize());
		/*if (pe.partitioning == null || pe.partitioning.partialFileList==null)
			sourceFile = new SourceFile("U" + pe.getUUID(), pe.getUrl(),pe.getUUID()+"", "", 0);
		else {
			sourceFile = new SourceFile("U" + pe.getUUID(), pe.getUrl(),pe.getUUID()+"", "", pe.partitioning.partialFileList.length);
		}*/
		
		/*
		 * The first entry in tableModel2 is the parent zip file. This file should not be included in the 
		 * partial file list in the control file.
		 */
		
		if (pe.tableModel2.getRowCount()>0) {
			FileEntry zipFE = pe.tableModel2.getProcessEntry(0);
			sourceFile.setMd5(zipFE.getClientChecksum());
			sourceFile.setAfterMd5(zipFE.getServerChecksum());
			sourceFile.setZipSize(zipFE.getSize());
		}
		
		if (pe.tableModel2.getRowCount()>1) {
			for (int i=0;i<pe.partitioning.partialFileList.length;i++)
			{ 
				FileEntry fe = pe.tableModel2.getProcessEntry(i+1);
				pe.partitioning.partialFileList[i].setAfterMd5(fe.getServerChecksum());
				pe.partitioning.partialFileList[i].setMd5(fe.getClientChecksum());
				pe.partitioning.partialFileList[i].setSize(fe.getSize());
				if (fe.getStage().getCode() > ProcessEntry.Stage.UPLOADING.getCode())
					pe.partitioning.partialFileList[i].setUploaded(true);
				
				
			}
			
		}
		
		if (pe.tableModel3.getRowCount()>0) {
			
			for (int i=0;i<pe.tableModel3.getRowCount();i++)
			{ 
				FileEntry fe = pe.tableModel3.getFileEntry(i);
				pe.partitioning.nativeFileList[i].setAfterMd5(fe.getServerChecksum());
				pe.partitioning.nativeFileList[i].setMd5(fe.getClientChecksum());
				pe.partitioning.nativeFileList[i].setSize(fe.getSize());
				
				
				
			}
			
		}
		
		String toFolder = Constants.conf.getFileDirectory()+ Constants.PATH_SEPARATOR;
		Controller control = new Controller();
		
		FileMap fileMap;
		fileMap = new FileMap(sourceFile, pe.partitioning.partialFileList,pe.partitioning.nativeFileList);
		
		try {
			control.writeFileMapXML(fileMap, toFolder);
			
			
			//um.message("Successfully updated control file");
			
		}
		catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			//um.message("Error updating control file.\n " + ioe.getMessage());
		}
		
		try {
			control.uploadFiles(new File(toFolder + sourceFile.getName() + ".xml"), null, Constants.conf.getServerHost(), Constants.conf.getServerPort(), (Upload)pe, 0);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}
	
	

}
