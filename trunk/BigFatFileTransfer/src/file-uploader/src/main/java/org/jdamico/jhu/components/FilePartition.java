package org.jdamico.jhu.components;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.NativeFile;
import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.dataobjects.SourceFile;
import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.FileEntry;
import org.jdamico.jhu.runtime.ParentEntry;
import org.jdamico.jhu.runtime.Upload;
import org.jdamico.jhu.runtime.ProcessEntry;
import org.jdamico.jhu.runtime.ProgressThread;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

public class FilePartition extends ParentPartition { 
	
	public FilePartition(Upload upload) {
		this.upload = upload;
	}
	private static final Logger log = Logger.getLogger(FilePartition.class);
	private Controller control;
	
	
	private Upload upload;
	
	public void startNew(File file, int size, String host, int port)
	throws Exception {
		
		upload.setTotalStartTime(Calendar.getInstance());
		control = new Controller();
		
		String strControlFilePath = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + "U" + upload.getUUID() + ".xml";
		
		/*
		 * In case we are are reusing the uncompressdir, delete all files.
		 * 
		 */
		
		control.deleteFilesRemote("", upload.getUUID()+"", false, true);
		

		
		/*
		 *  Determine if a single file or a directory
		 */
		
		if (file.isDirectory()) {
			upload.setDirectory(true);
			
			/*
			 * Generate recursive list of files.       
			 */
			
			
			ArrayList<String> listFiles = this.getFiles(file);
			
			upload.partitioning.nativeFileList = new NativeFile[listFiles.size()];
			int count=0;
			for (String tmpFile : listFiles) {
				FileEntry fe = new FileEntry(tmpFile,upload);
				
				upload.tableModel3.addFile(fe);		
				
				upload.partitioning.nativeFileList[count] = new NativeFile(tmpFile,"");
				count++;
			}
			
		}
		else {
			upload.partitioning.nativeFileList = new NativeFile[1];
			upload.partitioning.nativeFileList[0] = new NativeFile(upload.getFile().getAbsolutePath(),"");
			
			upload.setDirectory(false);
			upload.tableModel3.addFile(new FileEntry(upload.getUrl(),upload));
		}
		
		
		upload.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_NATIVE_PRE);
		upload.setProgress(-1);
		long lTotalSize=0;
		for (int i =0;i<upload.tableModel3.getRowCount();i++) {
			FileEntry fe = upload.tableModel3.getFileEntry(i);
			fe.setSize(control.getFileSize(fe.getName(), fe.getFolder()));
			lTotalSize += fe.getSize();
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_NATIVE_PRE);
			fe.setStageStartTime();
	
		}
		
		upload.setSize(lTotalSize);
		
		
		for (int i =0;i<upload.tableModel3.getRowCount();i++) {
			FileEntry fe = upload.tableModel3.getFileEntry(i);
			String strMd5 = control.getFileMD5(fe.getFile().getAbsolutePath(),upload.getUUID()+"",upload.tableModel3.getFileEntry(i),false);
			
			fe.setClientChecksum(strMd5);
			fe.setProgress(100);
			
		}
		
		
		
		
		String strZipFileName = upload.getTempLocationPath() + ".zip";
		upload.tableModel2.addProcessFile(new ProcessEntry(strZipFileName,upload));
		upload.tableModel2.getProcessEntry(0).setZipFile(true);
		upload.tableModel2.getProcessEntry(0).setStage(BaseEntity.Stage.COMPRESSING);
		upload.setStage(BaseEntity.Stage.COMPRESSING);     
		control.createLocalZip(strZipFileName,upload.tableModel2.getProcessEntry(0));
		
		
		
		 
		
		
		
		/*
		 * Write out the control file
		 */
		
		
		/*this.sourceFile = new SourceFile("U" + upload.getUUID(),upload.getUrl(), upload.getUUID()+"","", 0);
		this.sourceFile.setStage(BaseEntity.COMPRESSING);
		
		writeControlFile();*/
		
		UpdateControlFile.writeControlFile(upload);
		
		
		upload.setProgress(100);
		
		String sourceFileName = file.getAbsolutePath();
		long t1 = System.currentTimeMillis();
		
		String partialElementName = null;
		
		
	
		
		
		int npart = size;
		
		File fileZip = new File(strZipFileName);
		
		
		long filesize = fileZip.length();
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		
		if (filesize < size) {
			System.out
					.println("The file size, is smaller than the part file size.");
		}
		 
		String toFolder = Constants.conf.getFileDirectory()+ Constants.PATH_SEPARATOR + upload.getUUID() + Constants.PATH_SEPARATOR;
		
				
		int tail = 0;
		;
		if (filesize == 0) {
			tail = 1;
		} else {
			tail = (int)(filesize % npart);
		}
		
		int noOfParts = (int)(filesize / npart);
		
		int i = 0;
		if (tail > 0) { 
			noOfParts++;
		}
		int arraySize = noOfParts;
		
		//upload.tableModel2.addProcessFile(new ProcessEntry(strZipFileName,upload));
		upload.tableModel2.getProcessEntry(0).setStage(ProcessEntry.Stage.SPLITTING);
		
		upload.tableModel2.getProcessEntry(0).setSize(control.getFileSize(fileZip.getName(), Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + upload.getUUID() + Constants.PATH_SEPARATOR));
					
		
		for (i=0;i<noOfParts;++i)
		{
			upload.tableModel2.addProcessFile(new ProcessEntry(fileZip.getAbsolutePath() + ".part_" + i,upload));
			upload.tableModel2.getProcessEntry(i+1).setStage(ProcessEntry.Stage.SPLITTING);
		}
		
		ProgressThread pThread = new ProgressThread(upload.tableModel2.getProcessEntry(0),ProgressThread.MAIN_FILE_SPLITTING);
		pThread.start();
		
		upload.setStage(Upload.Stage.SPLITTING);
		Splitter splitter = new Splitter(fileZip, noOfParts, npart, upload);
		splitter.split(toFolder);
		upload.setProgress(100);
		upload.tableModel2.getProcessEntry(0).setProgress(100);
		
		pThread.done = true;
		pThread.interrupt();
		
		for (i=0;i<upload.tableModel2.getRowCount();++i)
		{
			//upload.tableModel2.getProcessEntry(i).setStage(ProcessEntry.WAITING_CHECKSUM);
			upload.tableModel2.getProcessEntry(i).setSize(control.getFileSize(upload.tableModel2.getProcessEntry(i).getName(), Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + upload.getUUID() + Constants.PATH_SEPARATOR));
		}
		
		
		
		partialFileList = new PartialFile[arraySize];
		
		
		for (i=0;i<upload.tableModel2.getRowCount();++i)
		{
			FileEntry fe = upload.tableModel2.getProcessEntry(i);
			fe.setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_NATIVE_PRE);
			String sourceFileMD5 = control.getFileMD5(fe.getUrl(),fe.getUUID()+"",fe,false);
			fe.setClientChecksum(sourceFileMD5);
			fe.setProgress(100);
			if (i>0)
				partialFileList[i-1] = new PartialFile(fe.getName(), sourceFileMD5);
		}
		
		UpdateControlFile.writeControlFile(upload);
		
		
		PartialFile[] sentFiles = null;
		/*try {
			sentFiles = control.checkSentFiles(file.getName() + ".xml", host,
					port);
		} catch (Exception e) {
			System.out
					.println("Unable to check remote FileMap at remote host.");
			upload.error(e.getMessage());
			return;
			
		}*/
		
		upload.setStage(Upload.Stage.UPLOADING);
		for (i=1;i<=noOfParts;++i)
		{
			upload.tableModel2.getProcessEntry(i).setStage(ProcessEntry.Stage.UPLOADING);
		}
		
		int remainingFiles = 0;
		boolean isUploaded;
		
		//isUploaded = control.uploadFiles(new File(strControlFilePath), null, host,
			//	port, upload, null);
		isUploaded = control.uploadFiles(new File(strControlFilePath),null,host,
					port, upload, null);
		if (!isUploaded) return;
		System.out
				.println("Starting upload. Local FileMAP xml sent to remote host!");
		/*if (sentFiles == null) {
			isUploaded = control.uploadFiles(new File(toFolder + file.getName() + ".xml"), null, host,
					port, upload, null);
			if (!isUploaded) return;
			System.out
					.println("Starting upload. Local FileMAP xml sent to remote host!");
		}*/
		//} else {
		//	System.out.println("Resuming upload.");
		//	for (i = 0; i < sentFiles.length; ++i) {
		//		if (!(sentFiles[i].isUploaded()))
		//			continue;
		//		++remainingFiles;
		//	}
		//}
		/*try {
			sentFiles = control.checkSentFiles(file.getName() + ".xml", host, port);
		} catch (Exception e) {
			System.out
					.println("Unable to check remote FileMap at remote host.");
			upload.error(e.getMessage());
			return;
		}*/

		sentFiles = partialFileList;
		int rInit = 0;
		
		boolean allSent = false;
		
		if (remainingFiles == noOfParts)
			allSent = true;
		
		boolean errorFlug = false;
		//pThread = new ProgressThread(upload,ProgressThread.PROCESS_FILE_UPLOAD);
		//pThread.start();
		while (!(allSent)) {
			
			/*sentFiles = control.checkSentFiles(file.getName() + ".xml", host,
					port);*/		
			int percent = 0;
			isUploaded = false;
			for (i = 0; i < sentFiles.length; ++i) {
				if (!(sentFiles[i].isTransferred())) {
					
					FileEntry feCurrent = upload.tableModel2.getProcessEntry(i+1);
					partialElementName = toFolder + file.getName() + ".zip.part_" + i;
					
					feCurrent.setStageStartTime(Calendar.getInstance());
					
					feCurrent.setStage(ProcessEntry.Stage.UPLOADING);
					
					File partialElementFile = new File(partialElementName);
					++remainingFiles;
					
					pThread = new ProgressThread(feCurrent,ProgressThread.PROCESS_FILE_UPLOAD);
					pThread.start();
		
					isUploaded = control.uploadFiles(partialElementFile,
							"U" + upload.getUUID() + ".xml", host, port, upload, i);
					
					pThread.done = true;
					pThread.interrupt();
					
					feCurrent.setProgress(100);
					
					
					if (isUploaded){						
						//partialElementFile.delete();
					} else {
						errorFlug=true;
					}

					String time = "";
		
					//int percent = 100 * remainingFiles / sentFiles.length;
					
					
					try {
						lTotalSize = control.getRemoteTotalProcessingFileSize(upload.tableModel2.getProcessEntry(0),host,port);
						percent = (int) (100 * lTotalSize / upload.tableModel2.getProcessEntry(0).getSize());
					}
					catch (Exception ex) {
						System.out.println("Error code FP1");
						System.out.println("Issue retrieving total processing file size.");
						System.out.println(ex.getStackTrace());
					}
					
					
					
					++rInit;
					/* Float transferRate = Float.valueOf(rInit * size
							/ diff.floatValue() / 1000.0F); */
		
					/* float eta = (sentFiles.length - remainingFiles) * size
							/ 1000 / transferRate.floatValue()
							/ transferRate.floatValue();*/
					
					Float transferRate = new Float(0);
					float eta = 0;
					
					feCurrent.setStage(ProcessEntry.Stage.CALCULATION_CHECKSUM_SPLIT_POST);
					
					pThread = new ProgressThread(upload.tableModel2.getProcessEntry(i+1),ProgressThread.REMOTE_FILE_CHECKSUM);
					pThread.start();
					
					String md5 = control.getRemoteFileMD5(upload.tableModel2.getProcessEntry(i+1).getName(),upload.getUUID()+"",true,false,true);
					
					pThread.done = true;
					pThread.interrupt();
					
					String[] ret = md5.split(",");
					if (ret[1].equals("SUCCESS"))
						feCurrent.setServerChecksum(ret[0]);
					else
						upload.error();
					
					feCurrent.setProgress(100);
		
					System.out.print("\rUploaded "
							+ remainingFiles
							+ "/"
							+ sentFiles.length
							+ " ["
							+ time
							+ "] "
							+ percent
							+ "% ["
							+ Helper.getInstance().formatDecimalCurrency(
									transferRate, "#####.##")
							+ " KBps] ETA: "
							+ Helper.getInstance().formatDecimalCurrency(
									Float.valueOf(eta), "#####.##")
							+ " m           ");
					//upload.setProgress(percent);
					
					feCurrent.setStageEndTime(Calendar.getInstance());
				}
		
			}
		
			if (remainingFiles != noOfParts) continue;
			allSent = true;
		}
		
		//pThread.done = true;
		//pThread.interrupt();
		
		if (!(allSent) || errorFlug)
			return;
		
		upload.setStage(Upload.Stage.JOINING);
		
		upload.tableModel2.getProcessEntry(0).setStage(BaseEntity.Stage.JOINING);
		
		
		/*for (i=0;i<noOfParts;++i)
		{
			upload.tableModel2.getProcessEntry(i).setStage(ProcessEntry.CALCULATION_CHECKSUMS_POST);
		}*/
		
		UpdateControlFile.writeControlFile(upload);
		
		pThread = new ProgressThread(upload.tableModel2.getProcessEntry(0),ProgressThread.MAIN_FILE_JOINING);
		pThread.start();
		
		
		log.info("Telling server to consolidate file: " + file.getName());
		String response = control.consolidateFileAtRemoteHost("U" + upload.getUUID() + ".xml", host,
				port, upload);
		
		pThread.done = true;
		pThread.interrupt();
		
		if (response.equals("SUCCESS")) {
			//upload.setProgress(100);
			upload.tableModel2.getProcessEntry(0).setProgress(100);
		}
		else {
			upload.error();
		}
		
		upload.tableModel2.getProcessEntry(0).setStage(BaseEntity.Stage.CALCULATION_CHECKSUM_ZIP_POST);
		pThread = new ProgressThread(upload.tableModel2.getProcessEntry(0),ProgressThread.REMOTE_FILE_CHECKSUM);
		pThread.start();
			
		String strChecksum = control.getRemoteFileMD5(upload.tableModel2.getProcessEntry(0).getName(), upload.getUUID()+"",true,false, true);
		
		pThread.done = true;
		pThread.interrupt();
		
		String[] ret = strChecksum.split(",");
		
		if (ret[1].toUpperCase().equals("SUCCESS")) {
			upload.tableModel2.getProcessEntry(0).setServerChecksum(ret[0]);
			upload.tableModel2.getProcessEntry(0).setProgress(100);
		}
		else 
			upload.error();
		
		
		System.out.println("\nFile " + file.getName() + " Sent!");
		
		upload.setStage(BaseEntity.Stage.UNCOMPRESSING);
		upload.tableModel2.getProcessEntry(0).setStage(BaseEntity.Stage.UNCOMPRESSING);
		
		
		pThread = new ProgressThread(upload.tableModel2.getProcessEntry(0),ProgressThread.REMOTE_FILE_UNCOMPRESSING);
		pThread.start(); 
				
		control.uncompressRemoteZip(upload.tableModel2.getProcessEntry(0), true);
		
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
		
		for (int j=0;j<upload.tableModel3.getRowCount();j++) {
			FileEntry fe = upload.tableModel3.getFileEntry(j);
			fe.setStage(FileEntry.Stage.CALCULATION_CHECKSUM_NATIVE_POST);
			String strStartDirectory;
			if (upload.isDirectory())
				strStartDirectory = upload.getUrl();
			else
				strStartDirectory = upload.getFolder();
			
			pThread = new ProgressThread(fe,ProgressThread.REMOTE_FILE_CHECKSUM);
			pThread.start();
			
			String strRelativeDir = fe.getUrl().substring(strStartDirectory.length()+1,fe.getUrl().length());
			
			strChecksum = control.getRemoteFileMD5(strRelativeDir,upload.getUUID()+"",false,true, true);
			
			
			while (pThread.done != true) {
				try {
      					Thread.sleep(1000);
				}
				catch (InterruptedException ie) {
					return;
				}
			}
			
			ret = strChecksum.split(",");
			
			if (ret[1].toUpperCase().equals("SUCCESS")) {
				fe.setServerChecksum(ret[0]);
				fe.setProgress(100);
			}
			else 
				upload.error();
			
			
		
		}
		
		
		
		for (i=0;i<noOfParts;++i)
		{
			upload.tableModel2.getProcessEntry(i).setStage(ProcessEntry.Stage.COMPLETE);
		}
		
		upload.setStage(Upload.Stage.COMPLETE);
		upload.setStatus(Upload.Status.STOPPED);
		upload.setProgress(100);
		
		upload.setTotalEndTime(Calendar.getInstance());
		
		UpdateControlFile.writeControlFile(upload);
	}
	
	public void loadControlFile()
	throws Exception {
		
	}

	public void startLoad(File inputFile, int size, String host, int port, File existingControlFile)
			throws Exception {
		
		/*
		 * Resuming a transfer. Get the control file.
		 */
		
		/*String strControlFile = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR +
		"U" + upload.getUUID() + ".xml";
		String sourceFileName = inputFile.getAbsolutePath();*/
		
		Controller control = new Controller();
		ObjConverter converter = new ObjConverter();
		
		//String strFileMap = control.getFileMapXml(existingControlFile.getAbsolutePath());
		
		FileMap fileMap = converter.convertToFileMap(Helper.getInstance()
				//.getStringFromFile(strFileMap));
				.getStringFromFile(existingControlFile.getAbsolutePath()));
		
		String tmp = fileMap.getSourceFile().getName();
		
		upload.setUUID(fileMap.getSourceFile().getUUID()+"");
		upload.setStatus(ParentEntry.Status.STOPPED);
		upload.setSize(fileMap.getSourceFile().getTotalSize());
		upload.setProgress(0);
		upload.setStage(BaseEntity.Stage.BLANK);
		
		
		this.partialFileList = fileMap.getPartialFileList();
		this.nativeFileList = fileMap.getNativeFileList();
		
		upload.tableModel2.addProcessFile(new ProcessEntry(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + upload.getUUID() + Constants.PATH_SEPARATOR + upload.getName() + ".zip",upload));
		//upload.tableModel2.getProcessEntry(0).setSize(fileMap.getSourceFile().getZipSize());
		upload.tableModel2.getProcessEntry(0).setSize(control.getFileSize(upload.tableModel2.getProcessEntry(0).getName(),upload.tableModel2.getProcessEntry(0).getFolder()));
		upload.tableModel2.getProcessEntry(0).setClientChecksum(fileMap.getSourceFile().getMd5());
		upload.tableModel2.getProcessEntry(0).setServerChecksum(fileMap.getSourceFile().getAfterMd5());
		upload.tableModel2.getProcessEntry(0).setStage(ProcessEntry.Stage.BLANK);
		
		
		if (partialFileList != null) {	
			for (int i=0;i<partialFileList.length;i++)
			{
				upload.tableModel2.addProcessFile(new ProcessEntry(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + upload.getUUID() + Constants.PATH_SEPARATOR + partialFileList[i].getName(),upload));
				ProcessEntry prE = upload.tableModel2.getProcessEntry(i+1);
				prE.setClientChecksum(partialFileList[i].getMd5());	
				prE.setServerChecksum(partialFileList[i].getAfterMd5());	
				prE.setStage(ProcessEntry.Stage.BLANK);
				prE.setSize(control.getFileSize(prE.getName(),prE.getFolder()));
		
				
				
			}
		}
		
		long lTotalSize=0;
		if (nativeFileList != null) {
			for (int i=0;i<nativeFileList.length;i++) {
				upload.tableModel3.addFile(new FileEntry(nativeFileList[i].getName(),upload));
				FileEntry fe = upload.tableModel3.getFileEntry(i);
				fe.setSize(control.getFileSize(fe.getName(), fe.getFolder()));
				lTotalSize += fe.getSize();
				//fe.setSize(nativeFileList[i].getSize());
				fe.setClientChecksum(nativeFileList[i].getMd5());
				fe.setServerChecksum(nativeFileList[i].getAfterMd5());
				fe.setStage(ProcessEntry.Stage.BLANK);
			}
		}
		
		upload.setSize(lTotalSize);
		
		
		
		
		
		
		


	
	}
	
	private ArrayList<String> getFiles(File fileDir) {
		ArrayList<String> listFiles = new ArrayList<String>();
		
		
		for (File file : fileDir.listFiles()) {
			if (!file.isDirectory())
				listFiles.add(file.getAbsolutePath());
			else {
				listFiles.addAll(getFiles(file));
			}
		}
			
		return(listFiles);	
	
		
		
		
	}
	

	/*private void writeControlFile() throws Exception {
		
		
		
		FileMap fileMap = new FileMap(this.sourceFile, this.partialFileList,this.nativeFileList);
		
		control.writeFileMapXML(fileMap, Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR);
		
		
	}*/
	
	
	
	
	
	

}



