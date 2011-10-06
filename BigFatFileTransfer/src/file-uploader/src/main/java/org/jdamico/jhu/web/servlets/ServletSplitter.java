package org.jdamico.jhu.web.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.jdamico.jhu.components.Splitter;
import org.vikulin.utils.Constants;

public class ServletSplitter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//Controller control = new Controller();
				
		String strFile = request.getParameter("filename");
		String strNParts = request.getParameter("noofparts");
		int noOfParts  = Integer.parseInt(strNParts);
		long partSize = Long.parseLong(request.getParameter("partsize"));
		
		Splitter splitter = new Splitter(new File(strFile), noOfParts, partSize, null);
		splitter.split(Constants.conf.getFileDirectory());
		
		PrintWriter out = response.getWriter();
		out.println("SUCCESS");
		out.close();
		
	}
}