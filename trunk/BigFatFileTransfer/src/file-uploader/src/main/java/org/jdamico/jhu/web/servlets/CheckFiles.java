package org.jdamico.jhu.web.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdamico.jhu.components.Controller;
import org.mortbay.log.Log;
import org.vikulin.utils.Constants;

public class CheckFiles extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		
		if (request.getParameter("filename")!= null)
		{
			
			String strFile = request.getParameter("filename");
			String strUUID = request.getParameter("UUID");
			if (request.getParameter("useprocessdir") != null)
				if (request.getParameter("useprocessdir").toUpperCase().equals("TRUE"))
					strFile = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR +  strUUID + Constants.PATH_SEPARATOR + strFile;
					
			Controller control = new Controller();
			//String result = control.getFileMapXml(fileXml);
			File f = new File(strFile);
			String result = control.getFileSize(f.getName(),f.getParent()) + "";
			PrintWriter out = response.getWriter();
			out.println(result);
			out.close();
		
		}
		else if (request.getParameter("deletefilename")!=null)
		{
			String strFile = request.getParameter("deletefilename");
			String strUUID = request.getParameter("UUID");
			String strControlFile = request.getParameter("controlfile");
			String strUncompressFile = request.getParameter("uncompressdir");
			Controller control = new Controller();
			
			if (strUncompressFile != null && strUncompressFile.toUpperCase().equals("TRUE"))
				control.deleteFilesLocal(Constants.conf.getUncompressFolder() + Constants.PATH_SEPARATOR
						+ strFile);
			else if (strControlFile != null && strControlFile.toUpperCase().equals("TRUE"))
				control.deleteFilesLocal(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR
						+ strFile);
			else
				control.deleteFilesLocal(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR
						+ strUUID + Constants.PATH_SEPARATOR
						+ strFile);
				
		
			PrintWriter out = response.getWriter();
			out.println("SUCCESS");
			out.close();			
		}
		else if (request.getParameter("md5filename") != null)
		{
			String status="SUCCESS";
			String strFile = request.getParameter("md5filename");
			String strUUID = request.getParameter("UUID");
			boolean bControlFileProgress=false;
			if (request.getParameter("controlfileprogress") != null)
				if (request.getParameter("controlfileprogress").toUpperCase().equals("TRUE"))
					bControlFileProgress=true;
			
			boolean bUseProcessDir=false;
			if (request.getParameter("useprocessdir") != null)
				if (request.getParameter("useprocessdir").toUpperCase().equals("TRUE"))
					bUseProcessDir=true;
			boolean bUseUncompressDir=false;
			if (request.getParameter("useuncompressdir") != null)
				if (request.getParameter("useuncompressdir").toUpperCase().equals("TRUE"))
					bUseUncompressDir=true;
			
			System.out.println("Retrieving server checksum");
			System.out.println("strFile: " + strFile);
			System.out.println("bUseProcessDir: " + bUseProcessDir);
			System.out.println("bUseUncompressDir: " + bUseUncompressDir);
			
			Controller control = new Controller();
			String strMD5 = "";
			try {
				String strFullPath;
				if (bUseUncompressDir == true) {
					strFullPath = Constants.conf.getUncompressFolder() + Constants.PATH_SEPARATOR + strFile;
					/*strMD5 = control.getFileMD5(Constants.conf.getUncompressFolder() + Constants.PATH_SEPARATOR + strFile,
							strUUID,null,bControlFileProgress);*/
				}
				else if (bUseProcessDir == true)
					strFullPath = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR + strFile;
					/*strMD5 = control.getFileMD5(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strUUID + Constants.PATH_SEPARATOR + strFile,
							strUUID,null,bControlFileProgress);*/
				else
					strFullPath = strFile;
				
				strFullPath = strFullPath.replace("\\",Constants.PATH_SEPARATOR);
				strFullPath = strFullPath.replace("/",Constants.PATH_SEPARATOR);
				
				System.out.println("Retreiving checksum for file: " + strFullPath);
				
				strMD5 = control.getFileMD5(strFullPath,strUUID,null,bControlFileProgress);
				
				
				
					
			}
			catch (Exception e) {
				status="FAILED";
			}
			finally {
				if (strMD5.isEmpty())
					status="FAILED";
				PrintWriter out = response.getWriter();
				out.println(strMD5 + "," + status);
				out.close();
			}
		}
		else if (request.getParameter("totalsizefilename") != null)
		{
			String strFile = Constants.conf.getFileDirectory()
			+ Constants.PATH_SEPARATOR + request.getParameter("totalsizefilename");
			String strUUID = request.getParameter("UUID");
			Controller control = new Controller();
			//String result = control.getFileMapXml(fileXml);
			File f = new File(strFile);
			String result = control.getTotalProcessingFileSize(f.getName(),strUUID)+"";
			if (result == null || result.isEmpty()) {
				System.out.println("Issue with retrieving total processing file size.");
			}
			PrintWriter out = response.getWriter();
			out.println(result);
			out.close();
			
		}
		else if (request.getParameter("verifyfilename") != null)
		{
			String filename = request.getParameter("verifyfilename");
			
			File f = new File(filename);
			
			PrintWriter out = response.getWriter();
			if (f.exists())
				out.println("TRUE");
			else
				out.println("FALSE");
			out.close();
			
			
			
		}
		else
		{
			String fileXml = Constants.conf.getFileDirectory()
					+ Constants.PATH_SEPARATOR + request.getParameter("fileXml");
			Controller control = new Controller();
			String result = control.getFileMapXml(fileXml);
			PrintWriter out = response.getWriter();
			out.println(result);
			out.close();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}