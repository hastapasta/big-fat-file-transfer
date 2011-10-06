package org.jdamico.jhu.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.jdamico.jhu.dataobjects.NetworkInterfaceObject;


public class Helper {
	private static Helper INSTANCE = null;
	public static Helper getInstance(){
		if(INSTANCE == null) INSTANCE = new Helper();
		return INSTANCE;
	}
	
	public String formatDecimalCurrency(Float value, String format) {
		
		NumberFormat formatter = new DecimalFormat(format);  

		String f = formatter.format(value); 
		return f;
	}
	
	public ArrayList<NetworkInterfaceObject> getMyIPs()	throws Exception {
		
		ArrayList<NetworkInterfaceObject> ifacesArray = new ArrayList<NetworkInterfaceObject>();
		
		String ifaceName = null;
		String ifaceIPv6 = null;
		String ifaceIPv4 = null;
		
		Enumeration<NetworkInterface> enumNetworkInterface = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
		while(enumNetworkInterface.hasMoreElements()) {
			
			
			NetworkInterface ni = enumNetworkInterface.nextElement();
			ifaceName = ni.getName();
			
			Enumeration<InetAddress> enumInetAddress = ni.getInetAddresses();
			while(enumInetAddress.hasMoreElements()) {
				try{
					InetAddress ia = enumInetAddress.nextElement();
					ifaceIPv6 = ia.toString().replaceAll("/", "");
					ia = enumInetAddress.nextElement();
					ifaceIPv4 = ia.toString().replaceAll("/", "");
				}catch(NoSuchElementException e){
					ifaceIPv4 = ifaceIPv6;
					ifaceIPv6 = null;
				}
				
			}
			
			ifacesArray.add(new NetworkInterfaceObject(ifaceName, ifaceIPv6, ifaceIPv4));
			
			LoggerManager.getInstance().logAtDebugTime(this.getClass().getName(), ifaceName+" : ["+ifaceIPv6+","+ifaceIPv4+"]");
		}
		
		return ifacesArray;
	}

	public boolean isReady(Date now, Date last, int interval) {
		
		boolean ret = false;
		
		Calendar calNow = Calendar.getInstance();
	    calNow.setTime(now);
	    
	    Calendar calLast = Calendar.getInstance();
	    calLast.setTime(last);
	    
	    long millisecondsNow = calNow.getTimeInMillis();
	    long millisecondsLast = calLast.getTimeInMillis();
	    long diff = millisecondsNow - millisecondsLast;
	    long diffSeconds = diff / 1000;
	    
	    if(diffSeconds >= interval) ret = true;
	    
	    LoggerManager.getInstance().logAtDebugTime(this.getClass().getName(), "Last: "+date2String(last)+" | Now: "+date2String(now)+" | Diff: "+diffSeconds+"");
	    
	    System.out.println("Last: "+date2String(last)+" | Now: "+date2String(now)+" | Diff: "+diffSeconds+"");
	    
	    
		return ret;
	}
	
	public String date2String(Date date){
		Format formatter = new SimpleDateFormat("yyyyMMMdd_HH:mm:ss");
		String stime = formatter.format(date);
		return stime;
	}

	public String date2String(Date date, String format){
		Format formatter = new SimpleDateFormat(format);
		String stime = formatter.format(date);
		return stime;
	}
	
	public String getCurrentDateTimeFormated(){
		
		return date2String(new Date());
		
	}
	
	public String[] getMyIPsByStringArray() throws Exception{
		ArrayList<NetworkInterfaceObject> myIps = getMyIPs();
		String[] ips = new String[myIps.size()];
		for(int i=0; i<myIps.size(); i++){
			ips[i] = myIps.get(i).getIfaceIPv4();
		}
		return ips;
	}
	
	public String getStringFromFile(String fileName){
		/*
		 * TODO: improve exception handling and close file
		 */
		StringBuffer sb = new StringBuffer();
		
		try {
		    BufferedReader in = new BufferedReader(new FileReader(fileName));
		    String str;
		    while ((str = in.readLine()) != null) {
		        sb.append(str);
		    }
		    in.close();
		} catch (IOException e) {
			System.out.println("File not found: "+fileName);
		}
		
		return sb.toString();
	}
	
	public String getStringFromUrl(String urlStr){
		StringBuffer sb = new StringBuffer();
		try {
		    // Create a URL for the desired page
		    URL url = new URL(urlStr);

		    // Read all the text returned by the server
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String str;
		    while ((str = in.readLine()) != null) {
		        sb.append(str);
		    }
		    in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return sb.toString();
	}
	
}
