package org.jdamico.jhu.web.servlets;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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

public class Downloader extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		Controller control = new Controller();
		File oPartFile = null;
		String fileMapXmlFileName = null;
		Integer fileIndex = null;*/
		
		
		
		ServletOutputStream op = response.getOutputStream();
		ServletContext context  = getServletConfig().getServletContext();
		String strFileName = request.getParameter("filename");
		
		boolean bControlFile = false;
		if (request.getParameter("controlfile") != null)
			if (request.getParameter("controlfile").toUpperCase().equals("TRUE"))
				bControlFile = true;
				
		if (bControlFile) {
			strFileName = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + strFileName;
		}
			
		/*String mimetype="";
		try {
			mimetype = context.getMimeType(strFileName);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}*/
		File f = new File(strFileName);
		int length = 0;
		int BUFSIZE = 2048;
		
	    
	        /*File                f        = new File(filename);
	        int                 length   = 0;
	        ServletOutputStream op       = resp.getOutputStream();
	        ServletContext      context  = getServletConfig().getServletContext();
	        String              mimetype = context.getMimeType( filename );*/

	        //
	        //  Set the response and go!
	        //
	        //
	       // response.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
			response.setContentType("application/octet-stream");
	        response.setContentLength( (int)f.length() );
	        response.setHeader( "Content-Disposition", "attachment; filename=\"" + strFileName + "\"" );

	        //
	        //  Stream to the requester.
	        //
	        byte[] bbuf = new byte[BUFSIZE];
	        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));

	        while ((in != null) && ((length = in.read(bbuf)) != -1))
	        {
	            op.write(bbuf,0,length);
	        }

	        in.close();
	        op.flush();
	        op.close();
	}
	   
	
}