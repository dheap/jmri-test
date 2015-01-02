// SchemaTest.java

package jmri.jmrit.display.configurexml;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;

//import jmri.InstanceManager;

/**
 * Checks of JMRI XML Schema
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision$
 */
public class SchemaTest extends jmri.configurexml.SchemaTestBase {
    

    // from here down is testing infrastructure

    public SchemaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SchemaTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.display.configurexml.SchemaTest");

        validateDirectory(suite, "java/test/jmri/jmrit/display/configurexml/verify");
        validateDirectory(suite, "java/test/jmri/jmrit/display/configurexml/load");
        
        return suite;
    }

    static Logger log = Logger.getLogger(SchemaTest.class.getName());
}
