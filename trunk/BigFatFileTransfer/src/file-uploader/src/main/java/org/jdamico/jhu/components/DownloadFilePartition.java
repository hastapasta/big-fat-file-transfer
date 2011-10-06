package org.jdamico.jhu.components;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.dataobjects.SourceFile;
import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.Download;
import org.jdamico.jhu.runtime.ProcessEntry;
import org.jdamico.jhu.runtime.ProgressThread;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

public class DownloadFilePartition extends ParentPartition { 
	
	public DownloadFilePartition(Download download) {
		this.download = download;
	}
	private static final Logger log = Logger.getLogger(FilePartition.class);
	
	
	
	private Download download;
	
	public void startNew(String strInputFile, int size, String host, int port)
	throws Exception {
		
		download.setTotalStartTime(Calendar.getInstance());
		
		
		Controller control = new Controller();
		
		
		
		long t1 = System.currentTimeMillis();
		
		String partialElementName = null;
		
		
		long lpart = size;
		
		//File file = inputFile;
		
		
		long filesize = control.getRemoteFileSize(strInputFile,false,host,port,download);
		download.setSize(filesize);
		

		long heapMaxSize = Runtime.getRuntime().maxMemory();
		
		if (filesize < size) {
			System.out
					.println("The file size, is smaller than the part file size.");
		}
		 
		String toFolder = Constants.conf.getFileDirectory()+ Constants.PATH_SEPARATOR;//file.getParent() + Constants.PATH_SEPARATOR;
		
		
		/*
		 * New download so delete all previous files from client and server.
		 */

		control.deleteFiles(download.getName());
		control.deleteFilesRemote(download.getName(),host,port);
		
		/*
		 * Create new empty control file.
		 */

		
		File f = new File (Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + download.getName() + ".xml");		
		f.createNewFile();
				
		int tail = 0;
		;
		if (filesize == 0) {
			tail = 1;
		} else {
			tail = (int)(filesize % lpart);
		}
		
		int noOfParts = (int)(filesize / lpart);
		
		int i = 0;
		if (tail > 0) { 
			noOfParts++;
		}
		int arraySize = noOfParts;
		
		for (i=0;i<noOfParts;++i)
		{
			download.tableModel2.addProcessFile(new ProcessEntry(download.getName() + ".part_" + i,strInputFile,download));
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.SPLITTING);
		}
		
		
		ProgressThread pThread = new ProgressThread(download,ProgressThread.REMOTE_MAIN_FILE_SPLITTING);
		/*
		 * OFP temporarily removed.
		 */
		pThread.start();
		
		download.setStatus(download.SPLITTING);
		
		
		
		control.remoteFileSplit(download.getName(),download.getFolder(),noOfParts,lpart,false,download);
		while (pThread.done != true) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ie) {
				return;
			}
		}
		
			
		
		for (i=0;i<noOfParts;++i)
		{
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.WAITING_CHECKSUM);
		}
		
		
		
		partialFileList = new PartialFile[arraySize];
		download.setStatus(Download.CALCULATION_CHECKSUM_PRE);
		
		pThread = new ProgressThread(download,ProgressThread.REMOTE_FILE_CHECKSUM);
		pThread.start();
		String sourceFileMD5="";
		
		sourceFileMD5 = control.getRemoteFileMD5(strInputFile,false,true,host,port);
				
		pThread.done = true;
		pThread.interrupt();
		
		
		String[] ret = sourceFileMD5.split(",");
		
		if (ret[1].equals("FAILED"))
			download.uploadManager.message("Checksum retreival failed");
		else
			download.setBeforeChecksum(ret[0]);
		
		download.setProgress(100);
			
		log.info(strInputFile + " | "+sourceFileMD5 +" [ok]");
		
		
		
		for (i = 0; i < noOfParts; ++i) {
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.CALCULATION_CHECKSUM_PRE);
			//int percent = 100 * (i + 1) / (noOfParts);
			//download.setProgress(percent);
			partialElementName = download.getName() + ".part_" + i;
			
			pThread = new ProgressThread(download.tableModel2.getProcessEntry(i),ProgressThread.REMOTE_FILE_CHECKSUM);
			pThread.start();
			
			String md5 = control.getRemoteFileMD5(partialElementName,true,true,host,port);
			
			pThread.done = true;
			pThread.interrupt();
			
			ret = md5.split(",");
			
			download.tableModel2.getProcessEntry(i).setBeforeChecksum(ret[0]);
			download.tableModel2.getProcessEntry(i).setProgress(100);
			download.tableModel2.getProcessEntry(i).setSize(control.getRemoteFileSize(partialElementName,true,host,port,download.tableModel2.getProcessEntry(i)));
			
	
			
			log.info(partialElementName + " | "+md5 + " [ok]");
			
			partialFileList[i] = new PartialFile(partialElementName, md5);
		}
		
		
		
		SourceFile sourceFile = new SourceFile(download.getName(), download.getBeforeChecksum(), (int)lpart);
		
		FileMap fileMap = new FileMap(sourceFile, partialFileList);
		
		String test = fileMap.getSourceFile().getMd5();
		
		control.writeFileMapXML(fileMap, toFolder);
		System.out.println("\rLocal FileMap generated.                        ");
		
		PartialFile[] sentFiles = null;
				
		download.setStatus(Download.DOWNLOADING);
		for (i=0;i<noOfParts;++i)
		{
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.DOWNLOADING);
		}
		
		int remainingFiles = 0;
		
		int rInit = 0;
		
		boolean allSent = false;
		
		boolean isDownloaded;
		
		if (remainingFiles == noOfParts)
			allSent = true;
		
		boolean errorFlug = false;
		pThread = new ProgressThread(download,ProgressThread.PROCESS_FILE_DOWNLOAD);
		pThread.start();
		while (!(allSent)) {
			
			sentFiles = control.checkLocalSentFiles(download.getName() + ".xml");
		
			
			isDownloaded = false;
			for (i = 0; i < sentFiles.length; ++i) {
				if (!(sentFiles[i].isTransferred())) {
					partialElementName = download.getName() + ".part_" + i;
					
					download.tableModel2.getProcessEntry(i).setStageStartTime(Calendar.getInstance());
					
					download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.DOWNLOADING);
					
					File partialElementFile = new File(partialElementName);
					++remainingFiles;
		
					isDownloaded = control.downloadFiles(partialElementName,host, port, true);
					if (isDownloaded){						
						//partialElementFile.delete();
					} else {
						errorFlug=true;
					}

					String time = "";
		
					//int percent = 100 * remainingFiles / sentFiles.length;
					
					long lTotalSize = control.getTotalProcessingFileSize(download.getName(),null);
					
					int percent = (int) (100 * lTotalSize / download.getSize());
					
					++rInit;
					/* Float transferRate = Float.valueOf(rInit * size
							/ diff.floatValue() / 1000.0F); */
		
					/* float eta = (sentFiles.length - remainingFiles) * size
							/ 1000 / transferRate.floatValue()
							/ transferRate.floatValue();*/
					
					Float transferRate = new Float(0);
					float eta = 0;
					
					download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.CALCULATION_CHECKSUM_POST);
					
					String md5 = control.getFileMD5(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + partialFileList[i].getName(),download.tableModel2.getProcessEntry(i),true);
					
					download.tableModel2.getProcessEntry(i).setProgress(100);
					
					download.tableModel2.getProcessEntry(i).setAfterChecksum(md5);
		
					System.out.print("\rDownloaded "
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
					download.setProgress(percent);
					
					download.tableModel2.getProcessEntry(i).setStageEndTime(Calendar.getInstance());
				}
		
			}
		
			if (remainingFiles != noOfParts) continue;
			allSent = true;
		}
		
		pThread.done = true;
		pThread.interrupt();
		
		
		
		if (!(allSent) || errorFlug)
			return;
		
		download.setStatus(Download.JOINING);
		
		
		

		
		pThread = new ProgressThread(download,ProgressThread.LOCAL_MAIN_FILE_JOINING);
		pThread.start();
		
		fileMap = new FileMap(sourceFile, partialFileList);
		log.info("Telling server to consolidate file: " + download.getName());
		control.consolidateFile(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + download.getName() + ".xml");
		
		pThread.done = true;
		pThread.interrupt();
		
		/*
		 * Note: we don't have to use the ProgressThread for displaying the progress of 
		 * local file checksums. The progress indicator is built directly into 
		 * Controller.createChecksum(), so all you have to do is pass the BaseEntity.
		 */

		download.setStatus(BaseEntity.CALCULATION_CHECKSUM_POST);
		
		String strFinalMd5 = control.getFileMD5(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + download.getName(),download,false);
		
		download.setProgress(100);
		
		
		download.setAfterChecksum(strFinalMd5);
		
		if (!download.getAfterChecksum().equals(download.getBeforeChecksum())) {
			download.setStatus(BaseEntity.ERROR);
			throw new Exception("Error, checksums do not match.");
		}
			
		
	
		
		//File rmF = new File(toFolder + file.getName() + ".xml");
		//rmF.delete();
		System.out.println("\nFile " + download.getName() + " Sent!");
		
		for (i=0;i<noOfParts;++i)
		{
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.COMPLETE);
		}
		
		download.setStatus(Download.COMPLETE);
		download.setProgress(100);
		
		download.setTotalEndTime(Calendar.getInstance());
		
	}

	public void startLoad(String inputFile, int size, String host, int port, File existingControlFile)
			throws Exception {
		
		/*
		 * Resuming a transfer. Get the control file.
		 */
		
		String strControlFile = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + 
		inputFile + ".xml";
		String sourceFileName = inputFile;
		
		Controller control = new Controller();
		ObjConverter converter = new ObjConverter();
		
		//String strFileMap = control.getFileMapXml(strControlFile);
		
		FileMap fileMap = converter.convertToFileMap(Helper.getInstance()
				//.getStringFromFile(strFileMap));
				.getStringFromFile(strControlFile));
		
		String tmp = fileMap.getSourceFile().getName();
		
		this.partialFileList = fileMap.getPartialFileList();
		
		for (int i=0;i<partialFileList.length;i++)
		{
			download.tableModel2.addProcessFile(new ProcessEntry(partialFileList[i].getName(),inputFile,download));
			download.tableModel2.getProcessEntry(i).setBeforeChecksum(partialFileList[i].getMd5());	
			download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.UPLOADING);
			download.tableModel2.getProcessEntry(i).setSize(control.getFileSize(partialFileList[i].getName(),null));
			if (partialFileList[i].isTransferred() == true)
			{
				download.tableModel2.getProcessEntry(i).setProgress(100);
				download.tableModel2.getProcessEntry(i).setStatus(ProcessEntry.UPLOADED);
				if (partialFileList[i].getAfterMd5() != null)
					download.tableModel2.getProcessEntry(i).setAfterChecksum(partialFileList[i].getAfterMd5());
			}
			else
			{
				download.tableModel2.getProcessEntry(i).setProgress(0);
			}
			
			
		}
		
		System.out.println("");
		
		


	
	}
	
	
	
	
	
	

}



