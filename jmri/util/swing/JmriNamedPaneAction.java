// JmriNamedPaneAction.java

 package jmri.util.swing;

import javax.swing.*;
import java.awt.event.ActionEvent;

import jmri.util.swing.*;

/**
 * Action to create and load a JmriPanel from just its name.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.2 $
 */
 
public class JmriNamedPaneAction extends JmriAbstractAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public JmriNamedPaneAction(String s, WindowInterface wi, String paneClass) {
    	super(s, wi);
    	this.paneClass = paneClass;
    }
    
 	public JmriNamedPaneAction(String s, Icon i, WindowInterface wi, String paneClass) {
    	super(s, i, wi);
    	this.paneClass = paneClass;
    }
    
    /**
     * Original constructor for compatibility with
     * older menus. Assumes SDI GUI.
     */
 	public JmriNamedPaneAction(String s, String p) {
    	this(s, new jmri.util.swing.sdi.JmriJFrameInterface(), p);
    }
     
    String paneClass;
    
    public jmri.util.swing.JmriPanel makePanel() {
        try {
            JmriPanel p = (JmriPanel)Class.forName(paneClass).newInstance();
            p.setWindowInterface(wi);
            p.initComponents();
            return p;
        } catch (Exception ex) {
            log.warn("could not load pane class: "+paneClass+" due to:"+ex);
            ex.printStackTrace();
            return null;
        }      
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmriNamedPaneAction.class.getName());
}

/* @(#)JmriAbstractAction.java */
