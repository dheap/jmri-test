/**
 * MrcMonPane.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version		$Revision: 22942 $
 * @author		kcameron Copyright (C) 2011
 * 	copied from SerialMonPane.java
 * @author		Daniel Boudreau Copyright (C) 2012
 *  added human readable format
 */

package jmri.jmrix.mrc.mrcmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.mrc.*;
import jmri.jmrix.mrc.swing.*;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;


public class MrcMonPanel extends jmri.jmrix.AbstractMonPane implements MrcListener, MrcPanelInterface{

	private static final long serialVersionUID = 6106790197336170348L;

	public MrcMonPanel() {
        super();
    }
    
    //MrcMonBinary mrcMon = new MrcMonBinary();
    
    public String getHelpTarget() { return null; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("MRC_");
    	}
		x.append(": ");
    	x.append("Command Monitor");
        return x.toString(); 
    }

    public void dispose() {
        // disconnect from the MrcTrafficController
        try {
            memo.getMrcTrafficController().removeMrcListener(this);
        } catch (java.lang.NullPointerException e){
            log.error("Error on dispose " + e.toString());
        }
        // and unwind swing
        super.dispose();
    }
    
    public void init() {}
    
    MrcSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof MrcSystemConnectionMemo ) {
            initComponents((MrcSystemConnectionMemo) context);
        }
    }
    
    JCheckBox excludePoll = new JCheckBox("Exclude Poll Messages");
    
    public void initComponents(MrcSystemConnectionMemo memo) {
        add(excludePoll);
        this.memo = memo;
        // connect to the MrcTrafficController
        try {
            memo.getMrcTrafficController().addMrcListener(this);
        } catch (java.lang.NullPointerException e){
            log.error("Unable to start the MRC Command monitor");
            JOptionPane.showMessageDialog(null, "An Error has occured that prevents the MRC Command Monitor from being loaded.\nPlease check the System Console for more information", "No Connection", JOptionPane.WARNING_MESSAGE);
        }
    }

    public synchronized void message(MrcMessage m) {  // receive a message and log it
	    String raw = "";
	    for (int i=0;i<m.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
        }
            nextLine("cmd: \""+m.toString()+"\"\n", raw);
	}
    
    MrcReply previousPollMessage;
    
	public synchronized void reply(MrcReply r) {  // receive a reply message and log it
        String raw = "";
        if(excludePoll.isSelected() && r.isPollMessage() && r.getElement(0) && r.getElement(1)==0x01){
            //Do not show poll messages for other devices
            previousPollMessage = r;
            return;
        } else if (previousPollMessage!=null) { //Only show the previous poll message if there is subsequent traffic resulting from the poll
            if(r.getElement(0)==0x00 && r.getElement(1)==0x00 && r.getElement(1)==0x00 && r.getElement(2)==0x00){
                previousPollMessage = null;
                return;
            }
            for (int i=0;i<previousPollMessage.getNumDataElements(); i++) {
                if (i>0) raw+=" ";
                raw = jmri.util.StringUtil.appendTwoHexFromInt(previousPollMessage.getElement(i)&0xFF, raw);
            }
            nextLine("msg: \""+previousPollMessage.toString()+"\"\n", raw);
            raw = "";
            previousPollMessage = null;
        }
	    
	    for (int i=0;i<r.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(r.getElement(i)&0xFF, raw);
        }
	        
        nextLine("msg: \""+r.toString()+"\"\n", raw);
	}
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.mrc.swing.MrcNamedPaneAction {

		private static final long serialVersionUID = -7644336249246783644L;

		public Default() {
            super("Mrc Command Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MrcMonPanel.class.getName(), 
                jmri.InstanceManager.getDefault(MrcSystemConnectionMemo.class));
        }
    }

	static Logger log = LoggerFactory.getLogger(MrcMonPanel.class.getName());

}


/* @(#)MonAction.java */
