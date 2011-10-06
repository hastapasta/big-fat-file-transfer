package org.jdamico.jhu.runtime;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.prefs.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.SwingUtilities;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdamico.jhu.components.Controller;
import org.jdamico.jhu.components.FilePartition;
import org.jdamico.jhu.components.MultipleOperation;
import org.jdamico.jhu.components.UpdateControlFile;
import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.utils.Helper;
import org.jdamico.jhu.xml.ObjConverter;
import org.vikulin.runtime.Configuration;
import org.vikulin.utils.Constants;



//The Upload Manager.
public class UploadManager extends JFrame implements Observer {
	
	private static final Logger log = Logger.getLogger(UploadManager.class);

//	public static Set<String> uFileName = Collections.synchronizedSet(new HashSet<String>());
	
	private static final long serialVersionUID = 6193105081704707641L;

	// Add upload text field.
	private JTextField addTextField;

	// Upload table's data model.
	private TransfersTableModel tableModel1;
	

	// Table listing uploads.
	private JTable table1;
	private JTable table2;
	private JTable table3;

	// These are the buttons for managing the selected download.
	private JButton pauseButton, loadButton;
	private JButton cancelButton, clearButton;
	
	JMenuItem uploadMenu1;
	JMenuItem downloadMenu1;
	JMenu uploadsMenu;
	JMenu downloadsMenu;
	
	JPanel filesPanel;
	JPanel processingPanel;

	// Currently selected upload.
	private ParentEntry selectedParentEntry;

	// Flag for whether or not table selection is being cleared.
	private boolean clearing;

	private File[] selectedFiles;
	
	private JFrame dialogFrame;
	private JFrame messageFrame;
	
	private int nProcessTableRow;
	
	private Preferences prefs;
	
	private PopUpDemo menu;
	private PopUpDemo3 menu3;

	// Constructor for Upload Manager.
	public UploadManager() {
		// Set application title.
		setTitle("My Big Fat File Transfer Client Manager v" + Configuration.getVersion());
		
		prefs = Preferences.systemRoot().node(this.getClass().getName());
		
		
		

		// Set window size.
		setSize(640, 480);

		// Handle window closing events.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});

		// Set up file menu.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		uploadsMenu = new JMenu("Recent Uploads");
		downloadsMenu = new JMenu("Recent Downloads");
		
		uploadMenu1 = new JMenuItem(prefs.get("MRUPLOAD1", ""));
		downloadMenu1 = new JMenuItem(prefs.get("MRDOWNLOAD1", ""));
		
		JMenuItem optionsMenu = new JMenuItem("Options",KeyEvent.VK_O);
		
		uploadMenu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionUploadMRU(e);
			}
		});
		
		downloadMenu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionDownloadMRU(e);
			}
		});
		
		optionsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOptionsDialog(e);
			}
		});
		
		uploadsMenu.setMnemonic(KeyEvent.VK_U);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExit();
			}
		});
		
		downloadsMenu.add(downloadMenu1);
		uploadsMenu.add(uploadMenu1);
	
		
		fileMenu.add(uploadsMenu);
		fileMenu.add(downloadsMenu);
		fileMenu.add(optionsMenu);
		fileMenu.add(fileExitMenuItem);
		
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		// Set up add panel.
		JPanel addPanel = new JPanel();
		
		JButton addLocalFile = new JButton("File");
		addLocalFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddLocalFile();
			}
		});
		
		JButton addServerFile = new JButton("Select File For Download");
		addServerFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddServerFile();
			}
		});

		addPanel.add(addLocalFile);
		//addPanel.add(addServerFile);

		addTextField = new JTextField(30);
		addPanel.add(addTextField);
	
		
		JButton addUpload = new JButton("Upload");
		addUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddTransfer(true);
			}
		});
		addPanel.add(addUpload);
		
		JButton addDownload = new JButton("Download");
		addDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddTransfer(false);
			}
		});
		addPanel.add(addDownload);
		
		/*
		 * Set up secondary file transfer button panel
		 */
		
		JPanel actionPanel1 = new JPanel();
		
		JButton join = new JButton("Join");
		join.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionJoin();
			}
		});
		
		actionPanel1.add(join);
		
		JButton updateControlFile = new JButton("Update Control File");
		updateControlFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionUpdateControlFile();
			}
		});
		
		actionPanel1.add(updateControlFile);
		/*
		 * Set up processing button panel
		 */
		
		
		
		
		/*JButton uploadProcess = new JButton("Upload");
		uploadProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionUpload();
			}
		});*/
		
		JButton uncompressProcess = new JButton("Uncompress");
		uncompressProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionUncompress();
			}
		});
		
		
		
	
		


		// Set up Uploads table.
		tableModel1 = new TransfersTableModel();
		table1 = new JTable(tableModel1);
		table1.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						tableSelectionChanged(e);
					}
				});
		// Allow only one row at a time to be selected.
		table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		

		// Set up ProgressBar as renderer for progress column.
		ProgressRenderer renderer1 = new ProgressRenderer(0, 100);
		renderer1.setStringPainted(true); // show progress text
		table1.setDefaultRenderer(JProgressBar.class, renderer1);

		// Set table's row height large enough to fit JProgressBar.
		table1.setRowHeight((int) renderer1.getPreferredSize().getHeight());
		

		// Set up downloads panel.
		JPanel downloadsPanel = new JPanel();
		downloadsPanel.setBorder(BorderFactory.createTitledBorder("Transfers"));
		downloadsPanel.setLayout(new BorderLayout());
		downloadsPanel.add(new JScrollPane(table1), BorderLayout.CENTER);
		
		/*
		 * 
		 * Add processing table.
		 */
		
		ProcessingFilesTableModel tableModel2 = new ProcessingFilesTableModel();
		table2 = new JTable(tableModel2);
		

		// Set up ProgressBar as renderer for progress column.
		ProgressRenderer renderer2 = new ProgressRenderer(0, 100);
		ColoredTableCellRenderer stringrenderer2 = new ColoredTableCellRenderer();
		renderer2.setStringPainted(true); // show progress text
		table2.setDefaultRenderer(JProgressBar.class, renderer2);
		table2.setDefaultRenderer(ChecksumString.class,stringrenderer2);
		
		// Set table's row height large enough to fit JProgressBar.
		table2.setRowHeight((int) renderer2.getPreferredSize().getHeight());
		
		/*
		 * Add files table
		 */
		
		ProcessingFilesTableModel tableModel3 = new ProcessingFilesTableModel();
		table3 = new JTable(tableModel3);
		

		// Set up ProgressBar as renderer for progress column.
		ProgressRenderer renderer3 = new ProgressRenderer(0, 100);
		ColoredTableCellRenderer stringrenderer3 = new ColoredTableCellRenderer();
		renderer3.setStringPainted(true); // show progress text
		table3.setDefaultRenderer(JProgressBar.class, renderer3);
		table3.setDefaultRenderer(ChecksumString.class,stringrenderer3);
		
		// Set table's row height large enough to fit JProgressBar.
		table3.setRowHeight((int) renderer3.getPreferredSize().getHeight());
		
		table3.addMouseListener( new MouseListener()
		{
			public void mouseEntered( MouseEvent e ) {
				
			}
			public void mouseExited( MouseEvent e ) {
				
			}
			public void mouseReleased( MouseEvent e ) {
				
			}
			public void mousePressed( MouseEvent e ) {
				
			}
			public void mouseClicked( MouseEvent e )
			{
				// Left mouse click
				if ( SwingUtilities.isLeftMouseButton( e ) )
				{
					// Do something
				}
				// Right mouse click
				else if ( SwingUtilities.isRightMouseButton(e) )
				{
					// get the coordinates of the mouse click
					Point p = e.getPoint();
		 
					// get the row index that contains that coordinate
					int rowNumber = table3.rowAtPoint( p );
		 
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = table3.getSelectionModel();
		 
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					System.out.println("row " + rowNumber);
					
					menu3 = new PopUpDemo3(rowNumber);  
					menu3.show(e.getComponent(), e.getX(), e.getY());  
				}
			}
		});
		
		table2.addMouseListener( new MouseListener()
		{
			public void mouseEntered( MouseEvent e ) {
				
			}
			public void mouseExited( MouseEvent e ) {
				
			}
			public void mouseReleased( MouseEvent e ) {
				
			}
			public void mousePressed( MouseEvent e ) {
				
			}
			public void mouseClicked( MouseEvent e )
			{
				// Left mouse click
				if ( SwingUtilities.isLeftMouseButton( e ) )
				{
					// Do something
				}
				// Right mouse click
				else if ( SwingUtilities.isRightMouseButton(e) )
				{
					// get the coordinates of the mouse click
					Point p = e.getPoint();
		 
					// get the row index that contains that coordinate
					int rowNumber = table2.rowAtPoint( p );
		 
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = table2.getSelectionModel();
		 
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					System.out.println("row " + rowNumber);
					
					menu = new PopUpDemo(rowNumber);  
					menu.show(e.getComponent(), e.getX(), e.getY());  
				}
			}
		});
		
		filesPanel = new JPanel();
		filesPanel.setBorder(BorderFactory.createTitledBorder("Individual Files"));
		filesPanel.setLayout(new BorderLayout());
		filesPanel.add(new JScrollPane(table3), BorderLayout.CENTER);
		
		processingPanel = new JPanel();
		processingPanel.setBorder(BorderFactory.createTitledBorder("Processing Files"));
		processingPanel.setLayout(new BorderLayout());
		processingPanel.add(new JScrollPane(table2), BorderLayout.CENTER);
		
		
	
		
		/*
		 * End processing table.
		 */


		// Set up buttons panel.
		JPanel messagePanel = new JPanel();
		messagePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
		messagePanel.setLayout(new BorderLayout());
		//messagePanel.add(new JScrollPane(table2), BorderLayout.CENTER);
		JTextArea textArea = new JTextArea(6,20);	
		JScrollPane scrollingArea = new JScrollPane(textArea);

		messagePanel.add(scrollingArea);
		// pauseButton = new JButton("Pause");
		// pauseButton.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// actionPause();
		// }
		// });
		// pauseButton.setEnabled(false);
		// buttonsPanel.add(pauseButton);
		// resumeButton = new JButton("Resume");
		// resumeButton.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// actionResume();
		// }
		// });
		// resumeButton.setEnabled(false);
		// buttonsPanel.add(resumeButton);
		
		
		

		// Add panels to display.
		//getContentPane().setLayout(new BorderLayout());
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		/*getContentPane().add(addPanel, BorderLayout.NORTH);
		getContentPane().add(downloadsPanel, BorderLayout.CENTER);
		getContentPane().add(treePanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);*/
		
		
		
		
		
		
		//getContentPane().add(downloadsPanel);
		//JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,downloadsPanel,filesPanel);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		downloadsPanel.setPreferredSize(new Dimension(300, 500));
		splitPane.add(downloadsPanel);
		
		splitPane.add(filesPanel);
		splitPane.setResizeWeight(.5);
		//getContentPane().add(splitPane);
		//getContentPane().add(filesPanel);
		//JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,processingPanel,messagePanel);
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane2.add(processingPanel);
		splitPane2.add(messagePanel);
		splitPane2.setResizeWeight(.75);
		//getContentPane().add(splitPane2);
		JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		splitPane3.add(splitPane);
		splitPane3.add(splitPane2);
		splitPane3.setResizeWeight(.6);
		
		getContentPane().add(addPanel);
		getContentPane().add(splitPane3);
		addPanel.setAlignmentX(addPanel.LEFT_ALIGNMENT); 
		splitPane3.setAlignmentX(splitPane3.LEFT_ALIGNMENT);
		
		
		//getContentPane().add(processingPanel);
		//getContentPane().add(messagePanel);

	}
	

	// Exit this program.
	private void actionExit() {
		System.exit(0);
	}
	
	private void actionUploadMRU(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		addTextField.setText(source.getText());
		actionAddTransfer(true);
		//System.out.println("");
	}
	
	public Preferences getPrefs() {
		return this.prefs;
	}
	
	private void actionDownloadMRU(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		addTextField.setText(source.getText());
		actionAddTransfer(false);
		//System.out.println("");
	}
	
	private void actionOptionsDialog(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		
		JDialog jdialog = new OptionsDialog(this);
		jdialog.setVisible(true);
		
	}
	
	
	
	/*public void displayLoadDialog(boolean upload) {
		dialogFrame = new JFrame();
		
		JButton newButton = new JButton("New");
		
		
		JButton loadButton = new JButton("Load");
		if (upload == true) {
			newButton.addActionListener(new newUploadAction());
			loadButton.addActionListener(new loadUploadAction());
		}
		else {
			newButton.addActionListener(new newDownloadAction());
			loadButton.addActionListener(new loadDownloadAction());
		}
			
		
		//dialogFrame.setUndecorated(true);
		
		JPanel addPanel = new JPanel();
		
		JPanel addPanel2 = new JPanel();
		
		addPanel.add(newButton);
		addPanel.add(loadButton);
		
		String text = "<html><body style='width: 200px;'><p>A previous control file exists for this file. ";
		text += "Would you like to initiate a new transfer or load the previous one?</p></body></html>";
		
		JLabel testLabel = new JLabel(text);
		
		//testLabel.setPreferredSize(new Dimension(1, 1)); 
		
		addPanel2.add(testLabel);

		
		Container content = dialogFrame.getContentPane();
		

		//content.setBackground(Color.white);
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS)); 
	
		content.add(addPanel2);
		content.add(addPanel);

		
		dialogFrame.setSize(400,175);
		dialogFrame.setVisible(true);
		
	}*/
	
	

	// Add a new upload.
	private void actionAddTransfer(boolean bUpload) {
		
		File f = new File(addTextField.getText());
		if (!f.exists()) {
			
			JOptionPane.showMessageDialog(
					null                       // Center in window.
                  , "<html><p>Please enter a valid path.</p></html>"        // Message
                  , "Invalid Path"               // Title in titlebar
                  , JOptionPane.ERROR_MESSAGE  // messageType
                  , null                       // Icon (none)
                  );
			
			
			
			
			
		}
		else {
			
			
			
		
		
		
		
	
			if (bUpload == true) {
				prefs.put("MRUPLOAD1", addTextField.getText());
				try {
					prefs.flush();
				}
				catch (BackingStoreException bse) {
					System.out.println("Issue writing preferences");
					System.out.println(bse.getMessage());
					bse.printStackTrace();
				}
				//uploadsMenu.removeAll();
				uploadMenu1.setText(prefs.get("MRUPLOAD1", ""));
				//uploadMenu1 = new JMenuItem(prefs.get("MRUPLOAD1", ""));
				//uploadsMenu.add(uploadMenu1);
				addNewUploadTransfer(new File(addTextField.getText()));
			}			
			else {
				prefs.put("MRDOWNLOAD1", addTextField.getText());
				try {
					prefs.flush();
				}
				catch (BackingStoreException bse) {
					System.out.println("Issue writing preferences");
					System.out.println(bse.getMessage());
					bse.printStackTrace();
				}
				
				downloadMenu1.setText(prefs.get("MRDOWNLOAD1", ""));
				//downloadMenu1 = new JMenuItem(prefs.get("MRDOWNLOAD1", ""));
				addNewDownloadTransfer(addTextField.getText());
			}
		}
		
			
			
		
		/*if (bUpload == true)
		{
			File file = verifyUri(addTextField.getText());
			
			
			
			if (file != null)
			{
			
				String strControlFile = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + 
					file.getName() + ".xml";
				
				prefs.put("MRUPLOAD1", file.getAbsolutePath());
				uploadMenu1 = new JMenuItem(prefs.get("MRUPLOAD1", ""));
					
				
				
				if (new File(strControlFile).exists())
				{
					displayLoadDialog(true);
					
		
				}
				else
				{	
					addNewUploadTransfer(file,false);
				}
			}
		}
		else {
			
			Controller control = new Controller();
			
			try {
				if (false == control.verifyRemoteFile(addTextField.getText(), Constants.conf.getServerHost(), Constants.conf.getServerPort())) {
					this.message("File does not exist on server. Please verify name and path.");
					return;
				}
				else {
							
					String strControlFile = Constants.conf.getFileDirectory() + Constants.PATH_SEPARATOR + 
					addTextField.getText() + ".xml";
					
					prefs.put("MRDOWNLOAD1", addTextField.getText());
					downloadMenu1 = new JMenuItem(prefs.get("MRDOWNLOAD1", ""));
					
					if (new File(strControlFile).exists())
					{
						displayLoadDialog(false);
						
			
					}
					else
					{	
						addNewDownloadTransfer(addTextField.getText(),false);
					}
				}
			}	
			catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
					
		}*/
			
	}
	
	public void addNewDownloadTransfer(String strFile) {
		tableModel1.addTransfer(new Download(strFile, this, "Download"));
		
		addTextField.setText(""); // reset add text field
		
		/*
		 * Automatically select the most recently inserted row.
		 */
		table1.setRowSelectionInterval(tableModel1.getRowCount()-1,tableModel1.getRowCount()-1);
		
		
	}
	
	
	public void addNewUploadTransfer(File file) {
		tableModel1.addTransfer(new Upload(file, this, "Upload"));
		
		addTextField.setText(""); // reset add text field
		
		/*
		 * Automatically select the most recently inserted row.
		 */
		table1.setRowSelectionInterval(tableModel1.getRowCount()-1,tableModel1.getRowCount()-1);
		
		
	}
	
	// Add a new upload.
	/*private void actionAddMultipleUpload(File[] selectedFiles, boolean onlyFilesSelection) {
		if (this.selectedFiles==null || this.selectedFiles.length==0){
			JOptionPane.showMessageDialog(this, "Selected files are missed",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else
		if (selectedFiles!=null && selectedFiles.length>0)	
			for(File fileToUpload : selectedFiles){
				if (fileToUpload.isDirectory() && !onlyFilesSelection){
					actionAddMultipleUpload(fileToUpload.listFiles(), true);
				} else {
					if (fileToUpload.isFile())
						tableModel1.addTransfer(new Upload(fileToUpload, this, "Upload"));
				}
			}
		else
			JOptionPane.showMessageDialog(this, "There are no files to upload",
					"Warning", JOptionPane.WARNING_MESSAGE);
	}*/

	// Add a new file.
	private void actionAddLocalFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			System.out.println(selectedFile.getName());
			addTextField.setText(selectedFile.getAbsolutePath());
		}
	}
	
	private void actionAddServerFile() {
		/*
		 * Code to select a remote file goes here
		 */
		
		/*JFileChooser fileChooser = new JFileChooser();
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			System.out.println(selectedFile.getName());
			addTextField.setText(selectedFile.getAbsolutePath());
		}*/
	}
	
	// Add a new file.
	/*private void actionAddMultipleFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			selectedFiles = fileChooser.getSelectedFiles();
			System.out.println(Arrays.deepToString(selectedFiles));
			//perform splitting and upload here
			actionAddMultipleUpload(selectedFiles, false);
		}
	}*/

	// Verify file to upload.
	private File verifyUri(String uri) {

		File file = null;
		try {
			file = new File(uri);
		} catch (Exception e) {
			return null;
		}

		return file;
	}

	// Called when table row selection changes.
	private void tableSelectionChanged(ListSelectionEvent e) {
		/*
		 * Unregister from receiving notifications from the last selected
		 * upload.
		 */
		if (selectedParentEntry != null)
			selectedParentEntry.deleteObserver(UploadManager.this);
		
		/*
		 * If not in the middle of clearing a upload, set the selected upload
		 * and register to receive notifications from it.
		 */
		if (!clearing) {
			selectedParentEntry = tableModel1.getTransfer(table1.getSelectedRow());
			selectedParentEntry.addObserver(UploadManager.this);
			//updateButtons();
		}
		
	
		
		table3.setModel(selectedParentEntry.tableModel3);
		table2.setModel(selectedParentEntry.tableModel2);
	
	}

	// Pause the selected upload.
	private void actionPause() {
		selectedParentEntry.pause();
		//updateButtons();
	}

	// load the selected upload.
	/*private void actionload() {
		selectedParentEntry.load();
		updateButtons();
	}*/

	// Cancel the selected upload.
	private void actionCancel() {
		selectedParentEntry.cancel();
		//updateButtons();
	}

	// Clear the selected upload.
	private void actionClear() {
		clearing = true;
		tableModel1.clearTransfer(table1.getSelectedRow());
		clearing = false;
		selectedParentEntry = null;
		//updateButtons();
	}
	
	private void actionServerChecksumAll(int table) {
		MultipleOperation mo;
		if (table == 3) {
			mo = new MultipleOperation(selectedParentEntry,true,false);
	
		}
		else {
			mo = new MultipleOperation(selectedParentEntry,false,false);
			
			
			
		}
		//mo.run();
		
	}
	
	private void actionClientChecksumAll(int table) {
		MultipleOperation mo;
		
		
		if (table == 3) {
			mo = new MultipleOperation(selectedParentEntry,true,true);
		}
		else {
			mo = new MultipleOperation(selectedParentEntry,false,true);
			
		}
		//mo.run();
		
	}
	
	
	
	private void actionServerChecksum(int row,int table) {
		
		if (table == 2) {
			ProcessEntry pEntry = selectedParentEntry.tableModel2.getProcessEntry(row);
			pEntry.run(ProcessEntry.Actions.SERVERCHECKSUM);
		}
		else {
			FileEntry fe = selectedParentEntry.tableModel3.getFileEntry(row);
			fe.run(ProcessEntry.Actions.SERVERCHECKSUM);
			
		}
		
	}
	
	private void actionClientChecksum(int row, int table) {
		
		if (table == 2) {
			ProcessEntry pEntry = selectedParentEntry.tableModel2.getProcessEntry(row);
			pEntry.run(ProcessEntry.Actions.CLIENTCHECKSUM);
		}
		else {
			FileEntry fe = selectedParentEntry.tableModel3.getFileEntry(row);
			fe.run(ProcessEntry.Actions.CLIENTCHECKSUM);
			
		}
		
	}
	
	private void actionUncompress() {
		
		ProcessEntry pEntry = selectedParentEntry.tableModel2.getProcessEntry(0);
		
		pEntry.run(ProcessEntry.Actions.UNCOMPRESSING);
		
	}
	
	private void actionUpload(int row) {
		
		System.out.println("the row " + row);
		ProcessEntry pEntry = selectedParentEntry.tableModel2.getProcessEntry(row);
		
		pEntry.run(ProcessEntry.Actions.UPLOADING);
		
		
		
	}
	
	
	
	private void actionUpdateControlFile() {
		
		UpdateControlFile ucf = new UpdateControlFile(selectedParentEntry,this);
		
		ucf.update();

	}
	
	private void actionJoin() {
		
		ProcessEntry pEntry = selectedParentEntry.tableModel2.getProcessEntry(0);
		
		pEntry.run(ProcessEntry.Actions.JOIN);
		
		
		
		
	}
	


	/*
	 * Update each button's state based off of the currently selected upload's
	 * status.
	 */
/*	private void updateButtons() {
		if (selectedParentEntry != null) {
			int stage = selectedParentEntry.getStage();
			switch (status) {
			case BaseEntity.Stage.SPLITTING:
				// pauseButton.setEnabled(false);
				// resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(false);
				break;
			case Upload.UPLOADING:
				// pauseButton.setEnabled(true);
				// resumeButton.setEnabled(false);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Upload.PAUSED:
				// pauseButton.setEnabled(false);
				// resumeButton.setEnabled(true);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case Upload.ERROR:
				// pauseButton.setEnabled(false);
				// resumeButton.setEnabled(true);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
				break;
			case Upload.JOINING:
				// pauseButton.setEnabled(false);
				// resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(false);
				break;
			default: // COMPLETE or CANCELLED or JOINNG
				// pauseButton.setEnabled(false);
				// resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
			}
		} else {
			// No upload is selected in table.
			// pauseButton.setEnabled(false);
			// resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}*/

	/*
	 * Update is called when a Upload notifies its observers of any changes.
	 */
	public void update(Observable o, Object arg) {
		// Update buttons if the selected upload has changed.
		/*if (selectedParentEntry != null && selectedParentEntry.equals(o))
			updateButtons();*/
		filesPanel.setBorder(BorderFactory.createTitledBorder("Individual Files (" + table3.getRowCount() + ")"));
		processingPanel.setBorder(BorderFactory.createTitledBorder("Processing Files (" + table2.getRowCount() + ")"));
	}

	// Run the Upload Manager.
	public static void main(String[] args) {
		Configuration.setIsClient(true); 
		UploadManager manager = new UploadManager();
		manager.setVisible(true);
		
		
		/*Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
		    public void eventDispatched(AWTEvent event) {
		    	
		    	//Filter out certain events
		    
		    	if (!event.toString().contains("MOUSE_MOVED") &&
		    		!event.toString().contains("MOUSE_ENTERED") &&
		    		!event.toString().contains("MOUSE_EXITED") &&
		    		!event.toString().contains("ANCESTOR_MOVED"))
		    		System.out.println("Event:" + event);
		    	
		    	

		    }
		}, -1);*/
		


	}
	

	
	
		public int dialog(String strMessage,String strTitle, String[] choices) {
			//String[] choices = {"Yes","No"};

			
			int response = JOptionPane.showOptionDialog(
                     null                       // Center in window.
                   , strMessage        // Message
                   , strTitle              // Title in titlebar
                   , JOptionPane.YES_NO_OPTION  // Option type
                   , JOptionPane.PLAIN_MESSAGE  // messageType
                   , null                       // Icon (none)
                   , choices                    // Button text as above.
                   , "None of your business"    // Default button's label
                 );
			return response;
			 
			 /*switch (response) {
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
            }*/
		}
	
		
		
		
		public void message(String message) {
			this.messageFrame = new JFrame();
			
			
			
			JButton newButton = new JButton("OK");
			newButton.addActionListener(new newMessageAction());
			
			JPanel addPanel1 = new JPanel();
			JPanel addPanel2 = new JPanel();
			
			
			
			JLabel testLabel = new JLabel("<html><body style='width: 200px;'><p>" + message + "</p></body></html>");
			
			Container content = this.messageFrame.getContentPane();
			content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS)); 
			
			addPanel1.add(testLabel);
			addPanel2.add(newButton);
		
			content.add(addPanel1);
			content.add(addPanel2);
						
			this.messageFrame.setSize(400,200);
			this.messageFrame.setVisible(true);
			//dialogFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						
		}
		
		
	
	
	
	class newUploadAction implements ActionListener{
		
		   public void actionPerformed(ActionEvent e){
		   //JOptionPane.showMessageDialog(dialogFrame,"New Upload");
			   dialogFrame.setVisible(false); //you can't see me!
			   dialogFrame.dispose(); 
			   
		   File file = verifyUri(addTextField.getText());
		   
		  // addNewUploadTransfer(file,false);
		   
		 //  	tableModel1.addUpload(new Upload(file, UploadManager.this, false));
		   	
		

			//addTextField.setText(""); // reset add text field
		   }
	}
	
	class loadUploadAction implements ActionListener{
		   public void actionPerformed(ActionEvent e){
		   //JOptionPane.showMessageDialog(dialogFrame,"Resume Upload");
			   dialogFrame.setVisible(false); //you can't see me!
			   dialogFrame.dispose(); 
			   
		   File file = verifyUri(addTextField.getText());
		 //  	tableModel1.addUpload(new Upload(file, UploadManager.this, true));
			//addTextField.setText(""); // reset add text field
		   //addNewUploadTransfer(file,true);
		   }
	}
	
	class newDownloadAction implements ActionListener{
		
		   public void actionPerformed(ActionEvent e){
	
			   dialogFrame.setVisible(false); //you can't see me!
			   dialogFrame.dispose(); 
			   
		   // addNewDownloadTransfer(addTextField.getText(),false);
		   
		
		
		   }
	}
	
	class loadDownloadAction implements ActionListener{
		   public void actionPerformed(ActionEvent e){
		   
			   dialogFrame.setVisible(false); //you can't see me!
			   dialogFrame.dispose(); 
			   
		   
		   //addNewDownloadTransfer(addTextField.getText(),true);
		   }
	}
	
	
	class newMessageAction implements ActionListener{
		   public void actionPerformed(ActionEvent e){
		 
			   messageFrame.setVisible(false); //you can't see me!
			   messageFrame.dispose(); 
						
		   }
	}
	
	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		
		public PopUpDemo(int nRowNumber){
			
			boolean bEnabled = true;
			
			if (UploadManager.this.selectedParentEntry.getStatus() == ParentEntry.Status.RUNNING)
				bEnabled = false;
			
			anItem = new JMenuItem("Local Checksum");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener4(nRowNumber));
			add(anItem);
			anItem = new JMenuItem("Remote Checksum");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener2(nRowNumber));
			add(anItem);			
			anItem = new JMenuItem("Local Checksum (All)");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener10(nRowNumber));
			add(anItem);
			anItem = new JMenuItem("Remote Checksum (All)");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener11(nRowNumber));
			add(anItem);
				
			
			
			if (nRowNumber !=0 ) {
				anItem = new JMenuItem("Upload");
				anItem.setEnabled(bEnabled);
				anItem.addActionListener(new CustomActionListener1(nRowNumber));
				add(anItem);  
				
			}
			else {
				anItem = new JMenuItem("Join");
				anItem.setEnabled(bEnabled);
				anItem.addActionListener(new CustomActionListener7(nRowNumber));
				/*anItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
								}
				});*/
				add(anItem);  
				anItem = new JMenuItem("Uncompress");
				anItem.setEnabled(bEnabled);
				anItem.addActionListener(new CustomActionListener3(nRowNumber));
				/*anItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
								}
				});*/
				add(anItem);  
			}
			
			/*anItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionUpload();				}
			});*/
			 
			}
		} 
	
	class PopUpDemo3 extends JPopupMenu {
		JMenuItem anItem;
		
		public PopUpDemo3(int nRowNumber){
			
			boolean bEnabled = true;
			
			if (UploadManager.this.selectedParentEntry.getStatus() == ParentEntry.Status.RUNNING)
				bEnabled = false;
			
			anItem = new JMenuItem("Local Checksum");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener5(nRowNumber));
			add(anItem);
			anItem = new JMenuItem("Remote Checksum");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener6(nRowNumber));
			add(anItem);
			anItem = new JMenuItem("Local Checksum (All)");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener8(nRowNumber));
			add(anItem);
			anItem = new JMenuItem("Remote Checksum (All)");
			anItem.setEnabled(bEnabled);
			anItem.addActionListener(new CustomActionListener9(nRowNumber));
			add(anItem);
				
			
			
			
			
			/*anItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionUpload();				}
			});*/
			 
			}
		} 
	
	class PopClickListener extends MouseInputAdapter {
		public void mousePressed(MouseEvent e){ 
			if (e.isPopupTrigger())   
				doPop(e);   
			}    
		public void mouseReleased(MouseEvent e){    
			if (e.isPopupTrigger())    
				doPop(e);   
			}     
		private void doPop(MouseEvent e){   
			PopUpDemo menu = new PopUpDemo(0);  
			menu.show(e.getComponent(), e.getX(), e.getY());  
			}
		} 
	
	class CustomActionListener1 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener1(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionUpload(nRowNumber);		
		}
	}
	
	class CustomActionListener2 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener2(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionServerChecksum(nRowNumber,2);		
		}
	}
	
	class CustomActionListener3 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener3(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionUncompress();		
		}
	}
	
	class CustomActionListener4 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener4(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionClientChecksum(nRowNumber,2);		
		}
	}
	
	class CustomActionListener5 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener5(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionClientChecksum(nRowNumber,3);		
		}
	}
	
	class CustomActionListener6 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener6(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionServerChecksum(nRowNumber,3);		
		}
	}
	
	class CustomActionListener7 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener7(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionJoin();	
		}
	}
	
	class CustomActionListener8 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener8(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionClientChecksumAll(3);	
		}
	}
	
	class CustomActionListener9 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener9(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionServerChecksumAll(3);	
		}
	}
	
	class CustomActionListener10 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener10(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionClientChecksumAll(2);	
		}
	}
	
	class CustomActionListener11 implements ActionListener {
		int nRowNumber;
		
		public  CustomActionListener11(int input) { 
			super();
			nRowNumber = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			actionServerChecksumAll(2);	
		}
	}
		
	
	
	
	
	


}

 
	 