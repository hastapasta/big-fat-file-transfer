package org.jdamico.jhu.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdamico.jhu.components.Controller;
import org.mortbay.log.Log;
import org.vikulin.utils.Constants;

public class ConsolidationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("In ConsolidationServlet.java doGet()");
		System.out.println("Request parameter " + request.getParameter("fileXml"));
		
		String fileXml = Constants.conf.getFileDirectory()
				+ Constants.PATH_SEPARATOR + request.getParameter("fileXml");
		Controller control = new Controller();
		
		try {
			control.consolidateFile(fileXml);
			response.getOutputStream().write(("SUCCESS").getBytes());
		} catch (Exception e) {
			System.err
					.println("Unable to call consolidation file process at remote host.");
			e.printStackTrace();
			response.getOutputStream().write(e.getMessage().getBytes());
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}