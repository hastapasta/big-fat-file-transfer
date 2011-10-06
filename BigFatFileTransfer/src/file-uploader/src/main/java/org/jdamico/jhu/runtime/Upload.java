package org.jdamico.jhu.runtime;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Observable;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.components.Controller.WildCardFileFilter;
import org.jdamico.jhu.components.UpdateControlFile;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.SourceFile;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.utils.Constants;

// This class uploads a file from a URL.
public class Upload extends ParentEntry {

	// Max size of upload buffer.
	private static final int MAX_BUFFER_SIZE = 1024;
	
	/*public static final String STATUSES[] = { "Uploading", "Paused",
		"Complete", "Cancelled", "Error", "Splitting", "Joining", "Calculating Initial Checksum","Waiting for Upload",
		"Waiting for Checksum","Calculating Final Checksum","UPLOADED", "Downloading","Initializing",
		"Compressing Files","Uncompressing Files"};*/
	
	


	// Constructor for Upload.
	public Upload(File file, UploadManager uploadManager,String type) {
		super(file.getAbsolutePath(),uploadManager,type);
				
		
		tableModel2 = new ProcessingFilesTableModel();
		tableModel3 = new FilesTableModel();
		
		this.stage = Stage.INITIALIZING;
		this.status = ParentEntry.Status.RUNNING;
		
		
		transfer();
	}
	



	

	
	
	

	
	
	// Upload file.
	public void run() {
		

		partitioning = new FilePartition(this);
		try {
			
			
			String host = Constants.conf.getServerHost();
			int port = Constants.conf.getServerPort();// 9999;
			int partSize = Constants.conf.getChunkSize();// 500000000;
			System.out.println("Running as client [Remote Host: http://"
					+ host + ":" + port + " (" + file + ") (" + size + ")]");
			Controller control = new Controller();
			ObjConverter converter = new ObjConverter();
			
			File dir = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR);
			File[] files = dir.listFiles(control.new WildCardFileFilter("U*.xml"));
			
			Arrays.sort(files, new Comparator<File>() {
				public int compare (File o1, File o2) {
					return (int)new Long((o1).lastModified()).compareTo(new Long((o2).lastModified()));
				}
			});
			
			if ((this.getName().toUpperCase().startsWith("U") || this.getName().toUpperCase().startsWith("D")) && this.getName().toUpperCase().endsWith("XML"))	{
				
				String[] choices = {"Yes","No"};

				
				int response = JOptionPane.showOptionDialog(
                         null                       // Center in window.
                       , "<html><p>This file is detected as being an xml control file. Do you wish to load a previous transfer?</p></html>"        // Message
                       , "Load as Control FIle?"               // Title in titlebar
                       , JOptionPane.YES_NO_OPTION  // Option type
                       , JOptionPane.PLAIN_MESSAGE  // messageType
                       , null                       // Icon (none)
                       , choices                    // Button text as above.
                       , "None of your business"    // Default button's label
                     );
				 
				 switch (response) {
	                case 0: 
	                    //load an existing transfer
	                	partitioning.loadControlFile();
	                    return;
	                case 1:
	                    //republicanCount++;
	                    break;
	                case -1:
	                    //... Both the quit button (3) and the close box(-1) handled here.
	                    System.exit(0);     // It would be better to exit loop, but...
	                default:
	                    //... If we get here, something is wrong.  Defensive programming.
	                	System.out.println("Invalid dialog response");
	                    JOptionPane.showMessageDialog(null, "Unexpected response " + response);
	            }
				
			}
			
			File fileExistingControlFile = null;
			
			for (File f : files) {
				//String strFileMap = control.getFileMapXml(f.getAbsolutePath());
				
				FileMap fileMap = converter.convertToFileMap(Helper.getInstance().getStringFromFile(f.getAbsolutePath()));
				
				/*
				 * Converting to files first and then comparing. Ran into an issue where the root drive
				 * letter could be lowercase for one and uppercase for the other.
				 */
				
				
				
				
				if (fileMap != null)
					if (fileMap.getSourceFile()!=null)
						if (fileMap.getSourceFile().getTransferPath()!=null)
						{
							File f1 = new File(fileMap.getSourceFile().getTransferPath());
							File f2 = new File(this.getUrl());
							
							if (f1.equals(f2))
								fileExistingControlFile = f;
						}

			}
			 
			
			
			
			if (fileExistingControlFile != null) {
				
			
			
				/*
				 * Display dialog prompting for new or resume.
				 */
				
				String[] choices = {"Resume","New"};

				
				int response = JOptionPane.showOptionDialog(
                         null                       // Center in window.
                       , "<html><p>A control file already exists from a previous transfer. Would you like to resume the most recent previous transfer or start a new one?</p></html>"        // Message
                       , "Resume Transfer?"               // Title in titlebar
                       , JOptionPane.YES_NO_OPTION  // Option type
                       , JOptionPane.PLAIN_MESSAGE  // messageType
                       , null                       // Icon (none)
                       , choices                    // Button text as above.
                       , "None of your business"    // Default button's label
                     );
				 
				 switch (response) {
	                case 0: 
	                    //load an existing transfer
	                	partitioning.startLoad(file, partSize, host, port,fileExistingControlFile);
	                    return;
	                case 1:
	                    //republicanCount++;
	                    break;
	                case -1:
	                    //... Both the quit button (3) and the close box(-1) handled here.
	                    System.exit(0);     // It would be better to exit loop, but...
	                default:
	                    //... If we get here, something is wrong.  Defensive programming.
	                	System.out.println("Invalid dialog response");
	                    JOptionPane.showMessageDialog(null, "Unexpected response " + response);
	            }
			}
				 
			this.setUUID();
				
			File f = new File(Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + this.getUUID());
			f.mkdir();
				
			UpdateControlFile.writeControlFile(this);
			
			//control.deleteFiles(this);
			//control.deleteFilesRemote(this); 
			
			partitioning.startNew(file, partSize, host, port);
			
			

			 
			


			
				
			
		} catch (Exception e) {
			System.out.println("Unable to run client with these parameters.");
			e.printStackTrace();
		}
	}


	public void updateJoinProgress()
	{
		//uploadManager.updateJoinProgress();
		Controller control = new Controller();
		
	
			if (this.getStage() == Stage.JOINING)
			{
				
				try {
					long lCurSize = control.getRemoteFileSize(this,false,Constants.conf.getServerHost(),Constants.conf.getServerPort());
					this.setProgress((int) ((lCurSize * 100 / this.getSize())));
					this.repaint();
					
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
			
				
			}
			

		
		
	}
	
	/*public void updateSplitProgress()
	{
		uploadManager.updateSplitProgress();
		
	}*/
	

}