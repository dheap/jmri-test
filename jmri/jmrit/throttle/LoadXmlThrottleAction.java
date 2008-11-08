package jmri.jmrit.throttle;

import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.util.List;
import java.util.ResourceBundle;

import org.jdom.Element;

/**
 *  Load throttles from XML
 *
 * @author     Glen Oberhauser 2004
 * @version     $Revision: 1.17 $
 */
public class LoadXmlThrottleAction extends AbstractAction {
	ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.throttle.ThrottleBundle");

	/**
	 *  Constructor
	 *
	 * @param  s  Name for the action.
	 */
	public LoadXmlThrottleAction(String s) {
		super(s);
		// disable the ourselves if there is no throttle Manager
		if (jmri.InstanceManager.throttleManagerInstance() == null) {
			setEnabled(false);
		}
	}

	public LoadXmlThrottleAction() {
		this("Load Throttle");
	}

	JFileChooser fileChooser;

	/**
	 *  The action is performed. Let the user choose the file to load from. Read
	 *  XML for each ThrottleFrame.
	 *
	 * @param  e  The event causing the action.
	 */
	public void actionPerformed(ActionEvent e) {
		if (fileChooser == null) {
			fileChooser = jmri.jmrit.XmlFile.userFileChooser(rb
					.getString("PromptXmlFileTypes"), "xml");
			fileChooser.setCurrentDirectory(new File(StoreXmlThrottleAction
					.defaultThrottleDirectory()));
		}
		int retVal = fileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION) {
			return;
			// give up if no file selected
		}

		// if exising frames are open ask to destroy those or merge.
		if (ThrottleFrameManager.instance().getThrottleFrames().hasNext()) {
			Object[] possibleValues = { rb.getString("LabelMerge"),
					rb.getString("LabelReplace"), rb.getString("LabelCancel") };
			int selectedValue = JOptionPane.showOptionDialog(null, rb
					.getString("DialogMergeOrReplace"), rb
					.getString("OptionLoadingThrottles"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
					possibleValues[0]);
			if (selectedValue == JOptionPane.NO_OPTION) {
				// replace chosen - close all then load
				ThrottleFrameManager.instance()
						.requestAllThrottleFramesDestroyed();
			}
		}
		loadThrottles(fileChooser.getSelectedFile());
	}

	/**
	 *  Parse the XML file and create ThrottleFrames.
	 *  Returns true if throttle loaded successfully.
	 *
	 * @param  f  The XML file containing throttles.
	 */
	public boolean loadThrottles(java.io.File f) {
		try {
			ThrottlePrefs prefs = new ThrottlePrefs();
			Element root = prefs.rootFromFile(f);
			List throttles = root.getChildren("ThrottleFrame");
			for (java.util.Iterator i = throttles.iterator(); i.hasNext();) {
				ThrottleFrame tf = ThrottleFrameManager.instance()
						.createThrottleFrame();
				tf.setXml((Element) i.next());
				tf.setVisible(true);
			}

		} catch (org.jdom.JDOMException ex) {
			log.warn("Loading Throttles exception:" + ex);
			return false;
		} catch (java.io.IOException ex) {
			log.warn("Loading Throttles exception:" + ex);
			return false;
		}
		return true;
	}

	/**
	 * An extension of the abstract XmlFile. No changes made to that class.
	 * 
	 * @author glen
	 * @version $Revision: 1.17 $
	 */
	class ThrottlePrefs extends XmlFile {

	}

	// initialize logging
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(LoadXmlThrottleAction.class.getName());

}
