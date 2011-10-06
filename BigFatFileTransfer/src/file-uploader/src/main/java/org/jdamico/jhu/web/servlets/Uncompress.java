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

public class Uncompress extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
					
			String strFile = request.getParameter("filename");
			String strUUID = request.getParameter("UUID");
			boolean bControlFileProgress = false;
			if (request.getParameter("controlfileprogress") != null)
				if (request.getParameter("controlfileprogress").toUpperCase().equals("TRUE"))
					bControlFileProgress = true;
					
							
			Controller control = new Controller();
			//String result = control.getFileMapXml(fileXml);
			File f = new File(strFile);
			String result = control.uncompressLocalZip(f.getName(),strUUID,null,bControlFileProgress);
			PrintWriter out = response.getWriter();
			out.println(result);
			out.close();
		
		
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}