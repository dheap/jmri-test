// LoadFileTest.java

package jmri.jmrit.display.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;

/**
 * Test upper level loading of a file
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision: 1.1 $
 */
public class LoadFileTest extends jmri.configurexml.LoadFileTestBase {

    public void testLoadStoreCurrent() throws Exception {
        // load manager
        java.io.File inFile = new java.io.File("java/test/jmri/jmrit/display/configurexml/ScaledIconTest.xml");
        
        // load file
        InstanceManager.configureManagerInstance()
            .load(inFile);
    
        // store file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"temp");
        java.io.File outFile = new java.io.File(XmlFile.prefsDir()+"temp/ScaledIconTest.xml");
        InstanceManager.configureManagerInstance()
            .storeUser(outFile);
        
        // compare files, except for certain special lines
        BufferedReader inFileStream = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(
                        new java.io.File("java/test/jmri/jmrit/display/configurexml/ScaledIconTest.xml"))));
        BufferedReader outFileStream = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(outFile)));
        String inLine;
        String outLine;
        while ( (inLine = inFileStream.readLine())!=null && (outLine = outFileStream.readLine())!=null) {
            if (!inLine.startsWith("  <!--Written by JMRI version")
                && !inLine.startsWith("  <timebase")   // time changes from timezone to timezone
                && !inLine.startsWith("<?xml-stylesheet")   // Linux seems to put attributes in different order
                && !inLine.contains("IMCURRENTTIME"))   // time changes
                    Assert.assertEquals(inLine, outLine);
        }
    }

    public void testValidateOne() {
        validate(new java.io.File("java/test/jmri/jmrit/display/configurexml/ScaledIconTest.xml"));
    }


    // from here down is testing infrastructure

    public LoadFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LoadFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoadFileTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadFileTest.class.getName());

}
