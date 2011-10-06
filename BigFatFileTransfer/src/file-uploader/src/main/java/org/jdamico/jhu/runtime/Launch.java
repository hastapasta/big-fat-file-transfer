package org.jdamico.jhu.runtime;

import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.web.JettyController;
import org.mortbay.log.Log;
import org.vikulin.runtime.Configuration;
import org.vikulin.utils.Constants;

public class Launch {
	public static void main(String[] args) {
		Configuration.setIsClient(false); 
		System.out.println("Version: " + Configuration.getVersion());
		long heapSize = Runtime.getRuntime().totalMemory();
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		long heapFreeSize = Runtime.getRuntime().freeMemory();

		System.out.println("JavaHttpUploader (0.7) HSC: "
				+ Helper.getInstance().formatDecimalCurrency(
						Float.valueOf((float) heapSize), "####,###,###")
				+ " HSM: "
				+ Helper.getInstance().formatDecimalCurrency(
						Float.valueOf((float) heapMaxSize), "####,###,###")
				+ " HSF: "
				+ Helper.getInstance().formatDecimalCurrency(
						Float.valueOf((float) heapSize), "####,###,###")
				+ " HSM: "
				+ Helper.getInstance().formatDecimalCurrency(
						Float.valueOf((float) heapFreeSize), "####,###,###"));

		int port = Constants.conf.getServerPort();
		System.out.println("Running as server [Remote Port: " + port + "]");
		Log.setLog(null);
		JettyController jController = new JettyController(port);
		jController.init();

	}
}