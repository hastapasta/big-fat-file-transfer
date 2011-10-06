package org.jdamico.jhu.web.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdamico.jhu.components.Controller;
import org.mortbay.log.Log;
import org.vikulin.utils.Constants;

public class ProgressValue extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		
		
		
		String strFile = Constants.conf.getFileDirectory()
				+ Constants.PATH_SEPARATOR + request.getParameter("UUID")
				+ Constants.PATH_SEPARATOR + request.getParameter("filename");
		//Controller control = new Controller();
		
		ServletOutputStream op = response.getOutputStream();
		FileInputStream input = null;
		int length;
		File f = new File(strFile);
		if (f.exists() == true)
		{	
			BufferedInputStream iBuff = null;
		
			byte[] bbuf = new byte[2048];
			
		
			input = new FileInputStream(strFile);
			iBuff = new BufferedInputStream(input);
			
			while ((iBuff != null) && ((length = iBuff.read(bbuf)) != -1))
	        {
	            op.write(bbuf,0,length);
	        }
		}
		else
			op.write("0".getBytes());
		
		
		
		
		
		if (input != null)
			input.close();
		op.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}