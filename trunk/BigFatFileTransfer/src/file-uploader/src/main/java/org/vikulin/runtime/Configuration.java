package org.vikulin.runtime;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Configuration {

	private static final Logger log = Logger.getLogger(Configuration.class);
	private PropertiesConfiguration configuration;
	private String clientProcessDirectory;
	private String serverUncompressDirectory;
	private String serverProcessDirectory;
	private int serverPort;
	private String serverHost;
	private int chunkSize;
	private static boolean client = false;
	private static final String strVersion = "2.1.10";
	private static final String strDefaultUncompressFolderName = "uncompress";
	
	public static void setIsClient(boolean client) {
		Configuration.client = client;
	}

	private static Configuration _instance = null;

	private Configuration() {
	}

	public static synchronized Configuration getInstance() {
		if (_instance == null) {
			_instance = new Configuration();
			_instance.readConfiguration();
		}
		return _instance;
	}

	public void readConfiguration() {
		System.out.println("here 1");
		configuration = new PropertiesConfiguration();
		System.out.println("here 2");
		URL config = null;
		try {
			
			String strConfFile = System.getProperty( "service.conffile" );
			
			if (Configuration.client == false)
			{
				System.out.println("Using serverconf.properties");
				if (strConfFile == null)
				{
					config = new URL("file:../conf/serverconf.properties");
				}
				else
				{
					config = new URL("file:" + strConfFile + "/serverconf.properties");
				}
				

			}
			else
			{
				System.out.println("Using clientconf.properties");
				if (strConfFile == null)
				{
					config = new URL("file:../conf/clientconf.properties");
				}
				else
				{
					config = new URL("file:" + strConfFile + "/clientconf.properties");
				}
			}
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		configuration.setDelimiterParsingDisabled(true);
		log.info("---------------------- Server info ----------------------");
		try {
			configuration.load(config);
			log.info(String.format("Properties file %s", config.toString()));
		} catch (Exception e1) {
			if (config == null) {
				config = Configuration.class.getClassLoader().getResource(
						"conf.properties");
			}
			try {
				configuration.load(config);
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
			log.info(String.format("Properties file %s", config.toString()));
		}
		clientProcessDirectory = configuration.getString("file.dir",
				System.getProperty("java.io.tmpdir"));
		if (clientProcessDirectory != null)
			clientProcessDirectory = clientProcessDirectory.replace("\"", "");
		log.info("File dir = " + getFileDirectory());

		serverUncompressDirectory = configuration.getString("uncompress.dir");
		if (serverUncompressDirectory != null)
			serverUncompressDirectory = serverUncompressDirectory.replace("\"","");
		log.info("Uncompress dir = " + serverUncompressDirectory);
		
		serverPort = configuration.getInt("server.port");
		log.info("Server port = " + serverPort);

		serverHost = configuration.getString("server.host", "localhost");
		log.info("Server host = " + serverHost);

		chunkSize = configuration.getInt("chunk.size");
		log.info("Chunk size = " + chunkSize);

		log.info("---------------------------------------------------------");
	}

	public String getFileDirectory() {
		return (clientProcessDirectory.endsWith("\\") ? clientProcessDirectory
				.substring(0, clientProcessDirectory.length() - 1)
				: clientProcessDirectory);
	}
	
	public String getServerFileDirectory() {
		return (serverProcessDirectory.endsWith("\\") ? serverProcessDirectory
				.substring(0, serverProcessDirectory.length() - 1)
				: serverProcessDirectory);
	}
	

	public int getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(int input) {
		serverPort = input;
	}
	
	public void setServerHost(String input) {
		serverHost = input;
	}
	
	public void setChunkSize(int input) {
		
		chunkSize = input;
	}
	
	public void setClientProcessDir(String input) {
		clientProcessDirectory = input;
	}
	
	public void setServerProcessDir(String input) {
		serverProcessDirectory = input;
	}
	
	public void setServerUncompressDir(String input) {
		serverUncompressDirectory = input;
	}
	
	public static String getVersion() {
		return strVersion;
	}
	
	/*public static String getUncompressFolderName() {
		return strUncompressFolderName;
	}*/
	
	public String getUncompressFolder () {
		return serverUncompressDirectory;
	}
	
	public String getDefaultUncompressFolderName () {
		return strDefaultUncompressFolderName;
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getChunkSize() {
		return chunkSize;
	}

}
