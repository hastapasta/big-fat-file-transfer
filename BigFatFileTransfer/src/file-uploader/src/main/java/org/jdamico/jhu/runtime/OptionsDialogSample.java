package org.jdamico.jhu.runtime;

import java.awt.*;
import javax.swing.*;

public class OptionsDialogSample extends JDialog
{
  public OptionsDialogSample()
  {
    JTabbedPane tabs;
    JPanel p1, p2;

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
    JLabel l1, l2,l3,l4;
    JSpinner spinner;
    JComboBox cb;

    //create components...
    ans = new JPanel( new GridLayout( 4, 4 ) );
    l1 = new JLabel( "Speed" );
    l2 = new JLabel( "Rules" );
    l3 = new JLabel ("Server Port");
    l4 = new JLabel ("Server Host");
    
    
    spinner = new JSpinner();
    cb = new JComboBox( new String[]{ "Play by the rules", "No rules" } );
    
    //.. and add them
    ans.add(l1);
    ans.add(spinner);
    ans.add(l2);
    ans.add(l3);
    ans.add(l4);
    ans.add(cb);

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

 /* public static void main( String args[])
  {
    //show the dialog
    JDialog d = new OptionsDialog();
    d.show();
  }*/
} 
