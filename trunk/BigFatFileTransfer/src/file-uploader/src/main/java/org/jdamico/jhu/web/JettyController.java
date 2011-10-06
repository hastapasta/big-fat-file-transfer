package org.jdamico.jhu.web;


import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;

public class JettyController implements Runnable {

	
	private Server server = null;
	private Thread t = null;
	public static ServletHandler handler = null; 
	
	public JettyController(int port){
		
		
		
		handler = new ServletHandler(); 
		
		server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });
	 
		server.setHandler(handler);

		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.CheckFiles",				"/CheckFiles");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.ConsolidationServlet",		"/ConsolidationServlet");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.Uploader",					"/Uploader");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.ServletSplitter",					"/Splitter");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.Downloader",					"/Downloader");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.ProgressValue",					"/ProgressValue");
		handler.addServletWithMapping("org.jdamico.jhu.web.servlets.Uncompress",					"/Uncompress");

		
	}
	
	public void init() {
		t = new Thread(this);
		t.start();
	}

	public int stopServer() {
		t = new Thread(this);
		t.interrupt();
		return 1;
	}

	public void run() {
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.out.println("Unable to start (JHU) http server.");
		}
	}
}
