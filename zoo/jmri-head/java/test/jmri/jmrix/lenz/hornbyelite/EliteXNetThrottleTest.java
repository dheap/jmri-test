package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;


/**
 * EliteXNetThrottleTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.EliteXNetThrottle class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class EliteXNetThrottleTest extends TestCase {

    public void testCtor() {
       // infrastructure objects
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        EliteXNetThrottle t = new EliteXNetThrottle(new jmri.jmrix.lenz.XNetSystemConnectionMemo(tc), tc);
        Assert.assertNotNull(t);
    }

	// from here down is testing infrastructure

	public EliteXNetThrottleTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", EliteXNetThrottleTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EliteXNetThrottleTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EliteXNetThrottleTest.class.getName());

}
