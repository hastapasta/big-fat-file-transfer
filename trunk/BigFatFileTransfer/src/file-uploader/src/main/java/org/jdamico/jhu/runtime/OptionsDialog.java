package org.jdamico.jhu.runtime;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.vikulin.runtime.Configuration;

public class OptionsDialog extends JDialog
{
	private UploadManager um;
	private JTextField textfield3, textfield4, textfield5,textfield6;
	private Preferences prefs;
	
	
  public OptionsDialog(UploadManager um)
  {
    JTabbedPane tabs;
    JPanel p1, p2;
    
    this.prefs = um.getPrefs();
    
    

    //set up basic GUI
    p1 = makeGameOptions();
    p2 = makeFileOptions();
    tabs = new JTabbedPane();
    tabs.addTab("Client", p1 );
    tabs.addTab("Server", p2 );

    getContentPane().add(tabs);
    pack();
  }

  private JPanel makeGameOptions()
  {
    JPanel ans;
    JLabel l3,l4,l5,l6;
    JSpinner spinner;
    
    JComboBox cb;
    JButton cancelButton, okButton;

    //create components...
    ans = new JPanel( new GridLayout( 5,2 ) );

    l3 = new JLabel ("Server Port");
    l4 = new JLabel ("Server Host");
    l5 = new JLabel ("Chunk Size (bytes)");
    l6 = new JLabel ("Processing Directory");
    

    
    
    /*spinner = new JSpinner();
    cb = new JComboBox( new String[]{ "Play by the rules", "No rules" } );*/
    textfield3 = new JTextField(prefs.get("serverport", "9999"));
    textfield4 = new JTextField(prefs.get("serverhost","localhost"));
    textfield5 = new JTextField(prefs.get("chunksize","5000000000"));
    textfield6 = new JTextField(prefs.get("clientprocessdir","C:\temp\tmpclient"));
    
    cancelButton = new JButton("Cancel");
    okButton = new JButton("Ok");
    
    okButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			actionOk();
		}
	});
    
    
    
    
    //.. and add them

    ans.add(l3);
    ans.add(textfield3);
    ans.add(l4);
    ans.add(textfield4);
    ans.add(l5);
    ans.add(textfield5);
    ans.add(l6);
    ans.add(textfield6);
    ans.add(cancelButton);
    ans.add(okButton);

    return ans;
  }

  private JPanel makeFileOptions()
  {
    JPanel ans;
    JCheckBox check;
    JLabel l1;
    JSlider slider;

    //create components...
    ans = new JPanel( new GridLayout( 3, 1 ) );
    check = new JCheckBox( "Autosave files" );
    l1 = new JLabel( "File size limit" );
    slider = new JSlider( 0, 5 );

    //and add
    ans.add( check );
    ans.add( l1 );
    ans.add( slider );

    return ans;
  }
  
  private void actionOk() {
		
	  	this.prefs.put("serverhost",textfield4.getText());
	  	this.prefs.put("serverport",textfield3.getText());
	  	this.prefs.put("chunksize",textfield5.getText());
	  	this.prefs.put("clientprocessdir",textfield6.getText());
	  	
	  	/*Configuration conf = Configuration.getInstance();
	  	conf.setServerHost(textfield4.getText());
	  	conf.setServerPort(Integer.parseInt(textfield3.getText()));
	  	conf.setChunkSize(Integer.parseInt(textfield5.getText()));
	  	conf.setClientProcessDir(textfield6.getText());*/
	  	
		//updateButtons();
	}

 /* public static void main( String args[])
  {
    //show the dialog
    JDialog d = new OptionsDialog();
    d.show();
  }*/
} 
