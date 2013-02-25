// StoreXmlConfigAction.java

package jmri.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.ResourceBundle;

/**
 * Store the JMRI configuration information as XML.
 * <P>
 * Note that this does not store preferences, tools or user information
 * in the file.  This is not a complete store!
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlConfigAction extends LoadStoreBaseAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public StoreXmlConfigAction() {
        this("Store configuration ...");
    }

    public StoreXmlConfigAction(String s) {
        super(s);
    }

    static public File getFileName(JFileChooser fileChooser) {
        fileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        return getFileCustom(fileChooser);
    }

    /**
     * Do the filename handling:
     *<OL>
     *<LI>rescan directory to see any new files
     *<LI>Prompt user to select a file
     *<LI>adds .xml extension if needed
     *<LI>if that file exists, check with user
     *</OL>
     * Returns null if selection failed for any reason
     */
    public static File getFileCustom(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(null,null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
        
        File file = fileChooser.getSelectedFile();
        if (fileChooser.getFileFilter() != fileChooser.getAcceptAllFileFilter()){
        	// append .xml to file name if needed
        	String fileName = file.getAbsolutePath();
        	String fileNameLC = fileName.toLowerCase();
        	if (!fileNameLC.endsWith(".xml")){
        		fileName = fileName+".xml";
        		file = new File(fileName);
        	}
        }
        if (log.isDebugEnabled()) log.debug("Save file: "+file.getPath());
        // check for possible overwrite
        if (file.exists()) {
            int selectedValue = JOptionPane.showConfirmDialog(null,
                                                                 "File "+file.getName()+" already exists, overwrite it?",
                                                                 "Overwrite file?",
                                                                 JOptionPane.OK_CANCEL_OPTION);
            if (selectedValue != JOptionPane.OK_OPTION) return null;
        }
        return file;
    }
    
    public void actionPerformed(ActionEvent e) {
        File file = getFileName(configFileChooser);
        if (file==null) return;
        
        // and finally store
        boolean results = InstanceManager.configureManagerInstance().storeConfig(file);
        System.out.println(results);
        log.debug(results?"store was successful":"store failed");
        if (!results){
        	JOptionPane.showMessageDialog(null,
        			rb.getString("StoreHasErrors")+"\n"
        			+rb.getString("StoreIncomplete")+"\n"
        			+rb.getString("ConsoleWindowHasInfo"),
        			rb.getString("StoreError"),	JOptionPane.ERROR_MESSAGE);
        }
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(StoreXmlConfigAction.class.getName());
}
