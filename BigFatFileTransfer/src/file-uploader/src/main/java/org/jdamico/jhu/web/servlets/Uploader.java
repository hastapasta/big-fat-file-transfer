package org.jdamico.jhu.web.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.jdamico.jhu.components.Controller;
import org.vikulin.utils.Constants;

public class Uploader extends HttpServlet { 
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
   ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		Controller control = new Controller();
		File oPartFile = null;
		String fileMapXmlFileName = null;
		Integer fileIndex = null;
		String strUUID = "";
		ServletFileUpload upload = new ServletFileUpload();
		try {
			FileItemIterator iter = upload.getItemIterator(request);
			
		/*	iter.hasNext();
			
			FileItemStream item1 = iter.next();
			
			String name1 = item1.getFieldName();
			
			InputStream is1 = item1.openStream();
			
			System.out.println();
			
			boolean b = name1.equals("filecontents");
			
			String tmp = Streams.asString(is1);*/
			
			
			
			
			
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String name = item.getFieldName();
				InputStream is = item.openStream();
				
				
				

				if (name.equals("filecontents"))
					try {
						if (fileMapXmlFileName != null) {
							
							File f = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID);
							if (!f.exists())
								f.mkdir();
							
							oPartFile = new File(
											f.getAbsolutePath()
											+ Constants.PATH_SEPARATOR
											+ item.getName());
						}
						/*
						 * If this is a control file then put it directly in the temp folder.
						 */
						else if (item.getName().substring(1,item.getName().length()).equals(strUUID + ".xml"))
						{
							
							
							oPartFile = new File(
											Constants.conf.getFileDirectory()
											+ Constants.PATH_SEPARATOR
											+ item.getName());
							
						}
						else {
							
							File f = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID);
							if (!f.exists())
								f.mkdir();
							
							oPartFile = new File(
											f.getAbsolutePath()
											+ Constants.PATH_SEPARATOR
											+ item.getName());
						}

						OutputStream out = new FileOutputStream(oPartFile);
						byte[] buf = new byte[8*1024];
						int len;
						while ((len = is.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						out.close();
						is.close();
					} catch (Exception localIOException) {
						localIOException.printStackTrace();
					}
				else if (name.equals("filemap")) {
					fileMapXmlFileName = Streams.asString(is);
				} else if (name.equals("fileindex")){
					fileIndex = Integer.parseInt(Streams.asString(is));
				} else if (name.equals("UUID")) {
					strUUID = Streams.asString(is);
				}
				
			}

		} catch (FileUploadException e) {
			System.out.println("Unable to process file upload.");
			e.printStackTrace();
		}
		if (fileMapXmlFileName == null)
			return;
	
	}
}