package org.jdamico.jhu.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.ParentEntry;
import org.jdamico.jhu.runtime.Upload;
import org.vikulin.utils.Constants;

public class Splitter {
	private int noOfParts;
	private File fileToSplit;
	private String strFile;
//	private String opSys;
	private long partSize;
	private ParentEntry pe;

	public Splitter(File fileToSplit, int noOfParts, long partSize, ParentEntry pe) {
		this.noOfParts = noOfParts;
		this.fileToSplit= fileToSplit;
		this.partSize = partSize;
		this.pe = pe;
	}

	public static String returnPathSeparator(String filePath) {
		if (filePath == null)
			return "";
		return (filePath.lastIndexOf("/") != -1 ? "/" : "\\");
	}
	

	public void split(String targetDirectory) {
		BufferedInputStream iBuff = null;
		BufferedOutputStream oBuff = null;
		try {
			
			long fileSize = this.fileToSplit.length();
			if (fileSize>(new File(targetDirectory)).getFreeSpace()){
				if (pe != null)
				{
					pe.error("Not enough disk space");
					pe.setStatus(ParentEntry.Status.ERROR);
				}
				return;
			}
//			String filePath = this.fileToSplit.getAbsolutePath();
//			String directory = targetDirectory;
//			String pathSeparator = returnPathSeparator(filePath);
//			directory = filePath.substring(0,
//					filePath.lastIndexOf(pathSeparator));
//			this.opSys = pathSeparator;
			String fileName = this.fileToSplit.getName();
			
			if (this.noOfParts == 1)
				this.partSize = fileSize;
			else
			{
				this.noOfParts = (int) (fileSize/ this.partSize);
				this.noOfParts++;
			}

			/*if (this.parts) {
				this.partSize = (int)(fileSize / this.noOfParts);
			} else {
				this.noOfParts = (int)(fileSize / this.partSize);
				this.noOfParts += 1;
			}*/

			File[] fileArr = new File[this.noOfParts];
			boolean success;
			for (int i = 0; i < this.noOfParts; ++i) {
				fileArr[i] = new File(targetDirectory + Constants.PATH_SEPARATOR + fileName
						+ ".part_" + i);
				success = fileArr[i].createNewFile();
			}

			FileInputStream input = new FileInputStream(this.fileToSplit);
			iBuff = new BufferedInputStream(input);
			int i = 0;

			FileOutputStream output = new FileOutputStream(fileArr[i]);
			oBuff = new BufferedOutputStream(output);

			//int bufferSize = 1024*1024;
			
			int buffSize = 8192;
			if (Constants.conf.getChunkSize() < 8192)
				buffSize = Constants.conf.getChunkSize();
			
			byte[] buffer = new byte[buffSize];
			long lcurPartSize=0;
			while (true) {
				/*if (iBuff.available() < buffSize) {
					byte[] newBuff = new byte[iBuff.available()];
					iBuff.read(newBuff);
					oBuff.write(newBuff);
					oBuff.flush();
					oBuff.close();

					break;
				}
				int r = iBuff.read(buffer);*/
				
				int r = iBuff.read(buffer);
				
				

				if (lcurPartSize >= this.partSize) {
					oBuff.flush();
					oBuff.close();
					lcurPartSize=0;
					++i;
					
					//int percent = 100 * (i + 1) / (noOfParts);
					//upload.setProgress(percent);
					
					output = new FileOutputStream(fileArr[i]);
					oBuff = new BufferedOutputStream(output);
				}
				
				if (r != -1)  {
					oBuff.write(buffer,0,r);
					lcurPartSize += r;
				}
				
				if (r < buffSize || r == -1)
				{
					
					break;
				}
					
			}
			//simple method for small files
			/*if (noOfParts==1){
				if (iBuff.available()<=bufferSize){
					byte[] buffer = new byte[iBuff.available()];
					iBuff.read(buffer);
					oBuff.write(buffer);
					iBuff.close();
					oBuff.flush();
					oBuff.close();
					return;
				}
				int numberOfBufferParts = getNumberOfBufferParts(bufferSize, iBuff.available());
				byte[] buffer = new byte[bufferSize];
				for(int p = 0; p < numberOfBufferParts;p++){
					iBuff.read(buffer);
					oBuff.write(buffer);
				}
				if (iBuff.available()>0){
					buffer = new byte[iBuff.available()];
					iBuff.read(buffer);
					oBuff.write(buffer);
				}
				iBuff.close();
				oBuff.flush();
				oBuff.close();
				upload.setProgress(100);
				return;
			} else {
				int numberOfBufferParts = getNumberOfBufferParts(bufferSize, partSize);
				for (int p=1;p<noOfParts;p++){
					byte[] buffer = new byte[bufferSize];
					for(int bp = 0; bp < numberOfBufferParts;bp++){
						iBuff.read(buffer);
						oBuff.write(buffer);
					}
					int percent = 100 * (i + 1) / (noOfParts);
					upload.setProgress(percent);
					if (numberOfBufferParts * bufferSize < partSize){
						buffer = new byte[(int)((long)(partSize-numberOfBufferParts * bufferSize))];
						iBuff.read(buffer);
						oBuff.write(buffer);;
					}
					oBuff.flush();
					oBuff.close();
					output = new FileOutputStream(fileArr[p]);
					oBuff = new BufferedOutputStream(output);
				}
				numberOfBufferParts = getNumberOfBufferParts(bufferSize, iBuff.available());
				byte[] buffer = new byte[bufferSize];
				for(int p = 0; p < numberOfBufferParts;p++){
					iBuff.read(buffer);
					oBuff.write(buffer);
				}
				if (iBuff.available()>0){
					buffer = new byte[iBuff.available()];
					iBuff.read(buffer);
					oBuff.write(buffer);
				}
				iBuff.close();
				oBuff.flush();
				oBuff.close();
				upload.setProgress(100);
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (pe != null)
					pe.setProgress(100);
				if (oBuff != null)
				{
					oBuff.flush();
					oBuff.close();
				}
				if (iBuff != null) iBuff.close();
			}
			catch (Exception e) {
			}
			
		}
	}
	
	private int getNumberOfBufferParts(int bufferSize, long partSize){
		return (int) ((long) partSize/bufferSize);
	}
}