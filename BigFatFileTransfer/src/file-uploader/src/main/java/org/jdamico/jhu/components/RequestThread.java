package org.jdamico.jhu.components;

import org.jdamico.jhu.utils.Helper;

import com.sun.net.httpserver.HttpExchange;

public class RequestThread extends Thread {
	
	private int timeout;
	private String url;
	
	HttpExchange he;              
	
	public RequestThread(String url,int timeout) {
		
		this.timeout = timeout;
		this.url = url;
		
		
	}
	
	public void run() {
		
		String strText = Helper.getInstance().getStringFromUrl(url);
		
		
	}

}
