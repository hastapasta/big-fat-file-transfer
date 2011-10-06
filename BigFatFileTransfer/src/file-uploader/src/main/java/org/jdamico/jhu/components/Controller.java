package org.jdamico.jhu.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpExchange;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.PartialFile;
//import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.runtime.Download;
import org.jdamico.jhu.runtime.BaseEntity;
import org.jdamico.jhu.runtime.FileEntry;
import org.jdamico.jhu.runtime.ParentEntry;
import org.jdamico.jhu.runtime.ProcessEntry;
import org.jdamico.jhu.runtime.Upload;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.runtime.Configuration;
import org.vikulin.utils.Constants;


public class Controller {
	private static final Logger log = Logger.getLogger(Controller.class);
	public void writeFileMapXML(FileMap fileMap, String folder)
			throws IOException {
		
		/*
		 * <?xml version="1.0" encoding="UTF-8"?> <filemap> <sourcefile name=""
		 * md5="" offset=""/> <pfiles> <pfile name="" md5="" uploaded=""/>
		 * </pfiles> </filemap>
		 */
		
		
		

		String sourceFileName = fileMap.getSourceFile().getName();
		
		log.info("Writing control file to location: " + folder + Constants.PATH_SEPARATOR + sourceFileName + ".xml");

		BufferedWriter out = new BufferedWriter(new FileWriter(folder
				+ Constants.PATH_SEPARATOR + sourceFileName + ".xml"));
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<filemap>\n" + "<sourcefile name=\"" + sourceFileName
				+ "\" transferpath=\"" + fileMap.getSourceFile().getTransferPath()
				+ "\" md5=\"" + fileMap.getSourceFile().getMd5()
				+ "\" aftermd5=\"" + fileMap.getSourceFile().getAfterMd5()			
				//+ "\" offset=\"" + fileMap.getSourceFile().getOffset()
				+ "\" uuid=\"" + fileMap.getSourceFile().getUUID()
				+ "\" clientversion=\"" + Configuration.getVersion()
				+ "\" totalsize=\"" + fileMap.getSourceFile().getTotalSize()
				+ "\" zipsize=\"" + fileMap.getSourceFile().getZipSize()
				+ "\" stage=\"" + fileMap.getSourceFile().getStage()
				+ "\"/>\n" + "<pfiles>\n");
		
		if (fileMap.getPartialFileList() != null) {
			for (int j = 0; j < fileMap.getPartialFileList().length; j++) {
				out.write("<pfile name=\""
						+ fileMap.getPartialFileList()[j].getName() 
						+ "\" md5=\""
						+ fileMap.getPartialFileList()[j].getMd5()
						+ "\" aftermd5=\""
						+ fileMap.getPartialFileList()[j].getAfterMd5()
						+ "\" size=\""
						+ fileMap.getPartialFileList()[j].getSize()
						+ "\" uploaded=\""
						+ fileMap.getPartialFileList()[j].isTransferred() + "\"/>\n");
			}
		}
		
		out.write("</pfiles>\n" + "<nfiles>\n");
		
		if (fileMap.getNativeFileList() != null) {
			for (int j = 0; j < fileMap.getNativeFileList().length; j++) {
				out.write("<nfile name=\""
						+ fileMap.getNativeFileList()[j].getName() 
						+ "\" md5=\""
						+ fileMap.getNativeFileList()[j].getMd5()
						+ "\" size=\""
						+ fileMap.getNativeFileList()[j].getSize()
						+ "\" aftermd5=\""
						+ fileMap.getNativeFileList()[j].getAfterMd5() + "\"/>\n");
			}
		}
		
		

		 out.write("</nfiles>\n" + "</filemap>");

		out.close();

	}

	public byte[] convertByteArray2byteArray(Byte[] source) {
		Byte[] ByteElement = source;
		byte[] byteElement = new byte[ByteElement.length];
		for (int k = 0; k < ByteElement.length; k++) {
			byteElement[k] = ByteElement[k];
		}
		return byteElement;
	}

	public Byte[] convertbyteArray2ByteArray(byte[] source) {
		byte[] byteElement = source;
		Byte[] ByteElement = new Byte[byteElement.length];
		for (int k = 0; k < byteElement.length; k++) {
			ByteElement[k] = byteElement[k];
		}
		return ByteElement;
	}

	public byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[1024];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;

		long heapMaxSize = Runtime.getRuntime().maxMemory();

		try {
			bytes = new byte[(int) length];
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
				offset += numRead;
			if (offset < bytes.length)
				throw new IOException("Could not completely read file "
						+ file.getName());
		} catch (OutOfMemoryError e) {

			if (heapMaxSize > 1000000000)
				System.out
						.println("There is no enough heap size memory to build part of this file. Check if you have any other java program running at same time.");
			else
				System.out
						.println("There is no enough heap size memory to build part of this file. Increase your heap size memory using. Example -Xmx1024M.");
			System.exit(1);
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public byte[] createChecksumOld(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}
	
	public byte[] createChecksum(String filename, String strUUID, BaseEntity fe, boolean bControlFileProgress) throws Exception {
		InputStream fis = new FileInputStream(filename);
		/*
		 * OFP - made changes to this function.
		 */
		
		
		long length = (new File(filename)).length();
		
		System.out.println("Processing checksum for file " + filename);
		byte[] buffer = new byte[2048];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		long lCount = 0;
		do {
			numRead = fis.read(buffer);
			lCount += 2048;
			
			if (lCount % 614400000 == 0)
				System.out.println(". ");
			else if (lCount % 20480000 == 0)
			{
				System.out.print(". ");
				if (fe != null)
					fe.setProgress((int) (lCount * 100/length));
				else if (bControlFileProgress == true)
				{
					
				
					
					writeProgressFile(filename,strUUID,(lCount * 100/length));
					

					
					
					
				}
			}
							
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		if (bControlFileProgress == true)
		{
			
			writeProgressFile(filename,strUUID,100);
			
		}
		
		return complete.digest();
		
	}
	
	public void createLocalZip(String filename,ProcessEntry pe) {
		ZipOutputStream out=null;
		try {
			
			File fileOutput = new File (filename);
			out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileOutput)));
			long totallength=0;
			long size = pe.getParentEntry().getSize();
			
			String strStartDirectory;
			if (pe.getParentEntry().isDirectory())
				strStartDirectory=pe.getParentEntry().getUrl();
			else
				strStartDirectory=pe.getParentEntry().getFolder();
			
			
			
			for (int i=0;i<pe.getParentEntry().tableModel3.getRowCount();i++) {
				FileEntry fe = pe.getParentEntry().tableModel3.getFileEntry(i);
				File fileInput = new File (fe.getUrl());
				FileInputStream input = new FileInputStream(fileInput);
				BufferedInputStream in = new BufferedInputStream(input);
	
				String strRelativeDir = fe.getUrl().substring(strStartDirectory.length()+1,fe.getUrl().length());
				
			    ZipEntry entry = new ZipEntry(strRelativeDir);
			
		        out.putNextEntry(entry);

				
				byte[] bbuf = new byte[2048];
				int length=0;
			
				
				
				
				 while ((in != null) && ((length = in.read(bbuf)) != -1)) {
					 
			            out.write(bbuf,0,length);
			            totallength += length;
			            pe.setProgress((int) (totallength*100/size));
			            
				 }
				 
				 in.close();
				
				
			}
			
			
			
			
			

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			try {if (out!=null) out.close();} catch(IOException ioe){}
		}
		
		
	}
	
	public String uncompressRemoteZip(BaseEntity be, boolean bControlFileProgress) throws Exception {
		String url = "http://" + Constants.conf.getServerHost() + ":" + Constants.conf.getServerPort() + "/Uncompress?filename=" + URLEncoder.encode(be.getName(),"UTF-8");
		url += "&controlfileprogress=" + bControlFileProgress;
		url += "&UUID=" + be.getUUID();
			
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		if (strText != null)
			return(strText);
		else
			return("");
		
		
		
		
	}
	
	public String uncompressLocalZip(String filename,String strUUID,ParentEntry pe,boolean bControlFileProgress) {
		final int BUFFER = 2048;
		BufferedOutputStream out  = null;
		ZipInputStream zis = null;
		long totallength = 0;
		long size = 0;
		String parentFolder;
		
		if (Constants.conf.getUncompressFolder() == null || Constants.conf.getUncompressFolder().isEmpty()) {
			parentFolder = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR;
		}
		else {
			parentFolder = Constants.conf.getUncompressFolder() + Constants.PATH_SEPARATOR;
		}

		try {
			
		    FileInputStream fis = new FileInputStream(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR + filename);
		    zis = new  ZipInputStream(new BufferedInputStream(fis));
		    ZipEntry entry;
		    
		    long progressincrement = 10000000;
		    long currentprogress = progressincrement;
		    
		    if (pe !=null)
		    	size = pe.getSize();
		    
		    
		    while((entry = zis.getNextEntry()) != null) {
		    	System.out.println("Extracting: " +entry);
		        int count;
		        byte data[] = new byte[BUFFER];
		        // write the files to the disk
		        
		       // File fileOutput = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR + Constants.conf.getUncompressFolderName() + Constants.PATH_SEPARATOR + entry.getName());
		        String strFile = parentFolder + entry.getName();
		        
		        /*
		         * This isn't very elegant but it covers both cases.
		         */
		        strFile = strFile.replace("\\", Constants.PATH_SEPARATOR);
		        strFile = strFile.replace("/", Constants.PATH_SEPARATOR);
		        
		        File fileOutput = new File(strFile);
		        new File(fileOutput.getParent()).mkdirs();
		        
		        
		        //fileOutput.mkdirs();
		        
		        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(fileOutput));
		        
		        out = new BufferedOutputStream(fos, BUFFER);
		        while ((count = zis.read(data, 0, BUFFER)) != -1) {
		               out.write(data, 0, count);
		               totallength += count;
		              if (totallength > currentprogress) {
		            	  
		            	  currentprogress += progressincrement;
		            	 
		            	  
			               if (pe!=null) {
			            	   pe.setProgress((int)(totallength*100/size));
			               }
			               else if (bControlFileProgress==true) {
			            	  
			            	   
			            	   /*File f = new File(filename);
			            	   File f1 = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR 
			            			   + strUUID + Constants.PATH_SEPARATOR + f.getName() + ".txt");*/
			            	  
			            	   writeProgressFile(filename,strUUID,totallength);
			            	
	
			            	   
			               }
		               }
		    }
		    out.flush();
	
		}
		
	 }
	 catch(Exception e) {
		e.printStackTrace();
		return("FAILED");
	 }
	 finally {
		try {if ( out!= null) out.close();} catch (IOException ioe) {}
		try {if ( zis!= null) zis.close();} catch (IOException ioe) {}
		
	 }
	 
	 /*
	  * Write out the progress one last time so the client will caculate it as 100.
	  */
	 
	 if (pe!=null) {
  	   pe.setProgress((int)(totallength*100/size));
     }
     else if (bControlFileProgress==true) {
  
  	  
  	   writeProgressFile(filename,strUUID,totallength);
  	

  	   
     }

	return("SUCCESS");		
		
		
		
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public String MD5(byte[] data) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {

		String text = convertToHex(data);

		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		byte[] md5hash = new byte[32];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		md5hash = md.digest();
		return convertToHex(md5hash);
	}

	public byte[] createChecksum(byte[] fileByte) throws Exception {

		MessageDigest complete = MessageDigest.getInstance("MD5");
		complete.digest(fileByte);
		return complete.digest();
	}

	public String getFileMD5(String fileName, String strUUID, BaseEntity fe, boolean bControlFileProgress) throws Exception {
		
		
		byte[] b = createChecksum(fileName,strUUID,fe,bControlFileProgress);
		return MD5(b);
		
	}
	
	public String getRemoteFileMD5(String strFileName, String strUUID, boolean bUseProcessDir, boolean bUseUncompressDir, boolean bControlFileProgress) throws Exception {
		String url = "http://" + Constants.conf.getServerHost() + ":" + Constants.conf.getServerPort() + "/CheckFiles?md5filename=" + URLEncoder.encode(strFileName,"UTF-8");
		url += "&useprocessdir=" + bUseProcessDir;
		url += "&useuncompressdir=" + bUseUncompressDir;
		url += "&controlfileprogress=" + bControlFileProgress;
		url += "&UUID=" + strUUID;
		
		System.out.println(url);
	
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		if (strText != null)
			return(strText);
		else
			return("");
	}
	
	public String remoteFileSplit(String fileName, String strFolder, int noOfParts, long partSize,
			boolean parts,Download download) throws Exception {
		String url = "http://" + Constants.conf.getServerHost() + ":" + Constants.conf.getServerPort() + "/Splitter?";
		url += "filename=" + URLEncoder.encode(strFolder + Constants.PATH_SEPARATOR + fileName,"UTF-8");
		url += "&noofparts=" + URLEncoder.encode(noOfParts +"","UTF-8");
		url += "&partsize=" + URLEncoder.encode(partSize + "","UTF-8");
		
		//HttpExchange he = new HttpExchange();
		
		
		
		RequestThread rt = new RequestThread(url,1000);
		rt.start();
		
		return("");
		
		//String strText = Helper.getInstance().getStringFromUrl(url);
		
		/*if (strText != null)
			return(strText);
		else
			return("");*/
	}

	public String getFileMD5(byte[] fileByte) throws Exception {
		byte[] b = createChecksum(fileByte);

		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public String extractExtensionFromFileName(String fileName) {
		return fileName.substring(fileName.length() - 4);
	}
	
	public boolean downloadFiles(String fileName, String host,int port,boolean bControlFile) throws Exception {
		boolean ret = false;

			
		URL url;
		int BUFSIZE = 2048;
		int length;
		String strURL;
		
		strURL = "http://" + host + ":" + port + "/Downloader?filename=" + URLEncoder.encode(fileName,"UTF-8");
		strURL += "&controlfile=" + bControlFile; 
		
		url = new URL(strURL);
		

        URLConnection uconn = url.openConnection();
        BufferedInputStream in = new BufferedInputStream(uconn.getInputStream());
        String inputLine;
        
        File f = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + fileName);
        
        DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
        byte[] bbuf = new byte[BUFSIZE];
        
        
        while ((in != null) && ((length = in.read(bbuf)) != -1))
            out.write(bbuf,0,length);
        
        in.close();
        
        return true;

		
	}

	public boolean uploadFiles(File partialElementFile, String fileMapFile, String host,
			int port, Upload upload, Integer fileIndex) throws Exception {

		boolean ret = false;

		String url = "http://" + host + ":" + port + "/Uploader";

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		if (!partialElementFile.exists()) {
			System.out.println("Local file does not exist: " + partialElementFile.getAbsolutePath());
			upload.error("Local file does not exist: " + url);
		}

		FileBody bin = new FileBody(partialElementFile);
		MultipartEntity reqEntity = new MultipartEntity();
		
		if (fileMapFile != null) {
			StringBody filemap = new StringBody(fileMapFile);
			StringBody index = new StringBody(fileIndex+"");
			reqEntity.addPart("filemap", filemap);
			reqEntity.addPart("fileindex", index);
		}
		
		reqEntity.addPart("UUID",new StringBody(upload.getUUID() + ""));
		reqEntity.addPart("filecontents", bin);
		

	
		httppost.setEntity(reqEntity);

		HttpResponse response = null;

		try {
			response = httpclient.execute(httppost);
			
			switch (response.getStatusLine().getStatusCode()) {
			case HttpServletResponse.SC_OK:
				ret = true;
				break;
			case HttpServletResponse.SC_LENGTH_REQUIRED:
				ret = false;
				ObjConverter converter = new ObjConverter();
				FileMap fileMap = converter
				.convertToFileMap(Helper.getInstance()
						.getStringFromFile(
								Constants.conf.getFileDirectory()
										+ Constants.PATH_SEPARATOR
										+ fileMapFile));
				String[] corruptList =  response.getStatusLine().getReasonPhrase().split("&");
				String message="The following parts are corrupted:\n";
				for (int i=0;i<corruptList.length;i++){
					message = message+fileMap.getPartialFileList()[Integer.parseInt(corruptList[i])].getName()+"\n";
				}
				upload.error(message);
			default:
				break;
			}
			
		} catch (ClientProtocolException e) {
			System.out.println("Remote host is unreachable: " + url);
			upload.error("Remote host is unreachable: " + url);
		} catch (IOException ioe) {
			System.out.println("Remote host is unreachable: " + url);
			upload.error("Remote host is unreachable: " + url);
		}
		return ret;

	}

	public PartialFile[] checkSentFiles(String fileName, String remoteHost,
			int port) throws Exception {
		String urlStr = "http://" + remoteHost + ":" + port
				+ "/CheckFiles?fileXml=" + URLEncoder.encode(fileName, "UTF-8");
		String xml = Helper.getInstance().getStringFromUrl(urlStr);

		PartialFile[] pFileArray = null;

		if (xml != null && xml.length() > 20) {
			ObjConverter converter = new ObjConverter();
			FileMap fileMap = converter.convertToFileMap(xml);
			pFileArray = fileMap.getPartialFileList();
		}

		return pFileArray;
	}
	
	public PartialFile[] checkLocalSentFiles(String fileName) throws Exception {
		
		int BUFSIZE = 2048;
		byte[] bbuf = new byte[BUFSIZE];
		File f = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + fileName);
		int length;
		
	    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
	    
	    String xml="";
	    
	    while ((in != null) && ((length = in.read(bbuf)) != -1))
        {
            xml += new String(bbuf,0,length);
        }


		PartialFile[] pFileArray = null;

		if (xml != null && xml.length() > 20) {
			ObjConverter converter = new ObjConverter();
			FileMap fileMap = converter.convertToFileMap(xml);
			pFileArray = fileMap.getPartialFileList();
		}

		return pFileArray;
	}
	
	/*
	 * This was how the remote control file originally was updated. After each file was uploaded, the remote control
	 * file would be updated again (I removed that code). This code can eventually be removed. 
	 * 
	 * Now the control file is uploaded with a generic uploadFiles() call.
	 */

	/*public String updateFileMap(String fileMapXmlFileName, File oPartFile, Integer fileIndex) throws Exception {
		ObjConverter converter = new ObjConverter();
		String corruptedPartsList = "";
		FileMap fileMap = converter
				.convertToFileMap(Helper.getInstance()
						.getStringFromFile(
								Constants.conf.getFileDirectory()
										+ Constants.PATH_SEPARATOR
										+ fileMapXmlFileName));
//		for (int i = 0; i < fileMap.getPartialFileList().length; i++) {
			PartialFile partFile = fileMap.getPartialFileList()[fileIndex];
//			File e = new File(Constants.conf.getServerFileDirectory()
//					+ Constants.PATH_SEPARATOR
//					+ partFile.getName());

			String uploadedMd5 = null;

			if (oPartFile.exists()) {
				uploadedMd5 = getFileMD5(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + fileMap.getSourceFile().getUUID() + Constants.PATH_SEPARATOR + oPartFile.getName(),
						fileMap.getSourceFile().getUUID()+"",null,false);
				partFile.setUploaded(true);
				//check MD5
				if (!uploadedMd5.equals(partFile.getMd5())) {
					log.error("Error! "
							+ partFile + " | "
							+ uploadedMd5);
					corruptedPartsList=corruptedPartsList+fileIndex+"&";					
				} else {
					log.info(partFile.getName() + " | "+uploadedMd5+" [ok]");
				}
			}
//		}
		
		writeFileMapXML(fileMap, Constants.conf.getFileDirectory());
		if (corruptedPartsList.length()>0) return corruptedPartsList;
		else
			return null;
	}*/

	public String getFileMapXml(String fileXml) {
		return Helper.getInstance().getStringFromFile(fileXml);
	}
	
	public void deleteFilesLocal(String strFileName)	{
		
	
		File f = new File (strFileName);
		
		if (f.isDirectory())
			/*
			 * This will recursively delete everything under the directory passed in.
			 */
			deleteDir(f,false);
		else
			f.delete();
		
	}
	
	public boolean deleteFilesRemote(String fileName,String strUUID,boolean bControlFile,boolean bUncompressDir)	{
		
		String url = "http://" + Constants.conf.getServerHost() + ":" + Constants.conf.getServerPort() + "/CheckFiles?deletefilename=" + fileName;
		url += "&UUID=" + strUUID;
		url += "&controlfile=" + bControlFile; 
		url += "&uncompressdir=" + bUncompressDir;
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		if (strText.toUpperCase().equals("SUCCESS"))
			return true;
		else
			return false;
		
		
		
	}
	
	public long getFileSize(String file,String folder) {
		
		if (folder == null)
			folder = Constants.conf.getFileDirectory();
		
		File tmpFile = new File(folder + Constants.PATH_SEPARATOR + file);
		return(tmpFile.length());
	}
	
	public long getTotalProcessingFileSize(String strName, String strUUID) {
		
	
		File dir  = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR);
		File[] files = dir.listFiles(new WildCardFileFilter(strName + ".part*"));
		
		long lTotalSize = 0;
		
		for (File f : files)
		{
			lTotalSize += f.length();
		}
		
		return lTotalSize;
	}
	
	public long getRemoteTotalProcessingFileSize(BaseEntity be, String host, int port) throws Exception {
		String url = "http://" + host + ":" + port + "/CheckFiles?totalsizefilename=" + be.getName();
		url += "&UUID=" + be.getUUID();
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		
		return(Long.parseLong(strText));

	}
	
	public long getRemoteProgressValue(BaseEntity be, String host, int port) throws Exception {
		String url = "http://" + host + ":" + port + "/ProgressValue?filename=progress.txt";
		url += "&UUID=" + be.getUUID();
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		/*
		 * This is very kludgy right now. Occasionally a blank value is read - not sure why, maybe a 
		 * file i/o conflict issue, i.e. reading and writing at the same time - so in that case parseLong
		 * will throw an exception and the progress value won't be update. This actually works fine for now
		 * but at some point I'd like to change this over to something cleaner.
		 */
		
		return Long.parseLong(strText);
		
	}
	
	
	public int getLocalProgressValue(String fileName) {
		
		/*
		 * May also have to put a system.gc() call in this method.
		 */
	
		File f = new File(fileName);
		try {
			if (f.exists() == true)
			{	
				BufferedInputStream iBuff = null;
			
				byte[] bbuf = new byte[2048];
				int length;
				String s = "";
				FileInputStream input = new FileInputStream(fileName);
				
				
				iBuff = new BufferedInputStream(input);
				
				while ((iBuff != null) && ((length = iBuff.read(bbuf)) != -1))
		        {
		            s += new String(bbuf,0,length);
		        }
				return (Integer.parseInt(s));
			}
			else
				return 0;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return 0;
		}
	}
	
	
	public long getRemoteFileSize(BaseEntity be, boolean bUseProcessDir, String host, int port) throws Exception {
		String url = "http://" + host + ":" + port + "/CheckFiles?filename=" + URLEncoder.encode(be.getName(),"UTF-8");
		url += "&useprocessdir=" + bUseProcessDir;
		url += "&UUID=" + be.getUUID();
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		if (strText != null)
			return(Long.parseLong(strText));
		else
			return(0);
	}
	
	public boolean verifyRemoteFile(String fileName, String host, int port) throws Exception {
		String url = "http://" + host + ":" + port + "/CheckFiles?verifyfilename=" + URLEncoder.encode(fileName,"UTF-8");
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		if (strText != null && strText.toUpperCase().equals("TRUE"))
			return(true);
		else
			return(false);
	}

	public String consolidateFileAtRemoteHost(String fileName, String host,
			int port, ParentEntry pe) throws UnsupportedEncodingException {
		String urlStr = "http://" + host + ":" + port
				+ "/ConsolidationServlet?fileXml=" +  URLEncoder.encode(fileName, "UTF-8");
		
		System.out.println("JOIN url:" + urlStr);
		String responseMessage = Helper.getInstance().getStringFromUrl(urlStr);
		if (!responseMessage.contains("SUCCESS")) {
			pe.error(responseMessage);
		}
		//String[] response = responseMessage.split(",");
		return(responseMessage);
	}

	public void consolidateFile(String fileMapXml) throws Exception{
		ObjConverter converter = new ObjConverter();
		

		System.out.println("fileMapXml: " + fileMapXml);
		
		
		FileMap fileMap = converter.convertToFileMap(Helper.getInstance()
				.getStringFromFile(fileMapXml));
		
		if (fileMap == null) {
			System.out.println("FileMap is null");
		}
		
		Joiner joiner = new Joiner();
		System.out.println("Here 1");
		
		File firstFile = new File(
				Constants.conf.getFileDirectory()
						+ Constants.PATH_SEPARATOR
						+ fileMap.getSourceFile().getUUID()
						+ Constants.PATH_SEPARATOR
						+ fileMap.getPartialFileList()[0].getName());
		//joiner.join(firstFile, true);
		System.out.println("About to call join function.");
		joiner.join(firstFile, false);
		/*String uploadedMd5 = getFileMD5(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + fileMap.getSourceFile().getName(),null,false);
		if (uploadedMd5.equals(fileMap.getSourceFile().getMd5())) {
//			System.out.println("Consolidation worked!");
			log.info(fileMap.getSourceFile().getName() + " | "+uploadedMd5 + " [ok]");
			File rmF = new File(Constants.conf.getFileDirectory()
					+ Constants.PATH_SEPARATOR
					+ fileMap.getSourceFile().getName() + ".xml");
			rmF.delete();
			return(uploadedMd5);
		} else {
			throw new Exception("Assembled file ("
					+ fileMap.getSourceFile().getName() + ") is corrupted!");
		}*/
	}
	
	 public String reverseString(String source) {
		    int i, len = source.length();
		    StringBuffer dest = new StringBuffer(len);

		    for (i = (len - 1); i >= 0; i--)
		      dest.append(source.charAt(i));
		    return dest.toString();
		  }
	 
	 private void writeProgressFile(String strName, String strUUID, long value) {
			File f = new File(strName);
			File f1 = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR + "progress.txt");
			BufferedOutputStream output=null;
		
			try {
				//output = new BufferedOutputStream(new FileOutputStream(f1,false), "UTF-8");
				output = new BufferedOutputStream(new FileOutputStream(f1,false));
				
				String str = value + "";
				output.write(str.getBytes());
				
			}
			catch (Exception e) {
				System.out.println("Issue writing out progress file.");
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			}
			finally {
				try {if (output!=null) output.close(); } catch (Exception e) {}
				/*
				 * The purpose of this system.gc call is to try to elminate this error message:
				 * 
				 * c:\temp\tmpserver\ddc88271-1206-46c6-b3f3-88aafec07b7e\progress.txt (The requested operation cannot
				 * be performed on a file with a user-mapped section open)
				 * 
				 * Which has something to do with windows not freeing up the buffer. According to this thread:
				 * http://answers.yahoo.com/question/index?qid=20090927164148AAOdJmE
				 * 
				 * calling System.gc(); might free up the buffer but there is no guarantee.
				 */
				System.gc();
			}
		
	
		
		 
	 }
	 
	 public static boolean deleteDir(File dir, boolean bDeleteTopDir) {
		    if (dir.isDirectory()) {
		        String[] children = dir.list();
		        for (int i=0; i<children.length; i++) {
		            boolean success = deleteDir(new File(dir, children[i]),true);
		            if (!success) {
		                return false;
		            }
		        }
		    }

		    // The directory is now empty so delete it
		    if (bDeleteTopDir == true)
		    	return dir.delete();
		    else
		    	return true;
		}

	
	public class WildCardFileFilter implements FileFilter
	{
	    private String _pattern;
	 
	    public WildCardFileFilter(String pattern)
	    {
	        _pattern = pattern.replace("*", ".*").replace("?", ".");
	    }
	 
	    public boolean accept(File file)
	    {
	    	return Pattern.compile(_pattern).matcher(file.getName()).find();
	    }
	}
	
	 
}