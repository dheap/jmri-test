// OperationsTest.java

package jmri.jmrit.operations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations package
 * @author		Bob Coleman
 * @version $Revision: 1.6 $
 */
public class OperationsTest extends TestCase {

	// from here down is testing infrastructure

	public OperationsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {OperationsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.operations.OperationsTest"); // no tests in class itself
		suite.addTest(jmri.jmrit.operations.setup.OperationsSetupTest.suite());
		suite.addTest(jmri.jmrit.operations.locations.OperationsLocationsTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.OperationsRollingStockTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.cars.OperationsCarsTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.engines.OperationsEnginesTest.suite());
		suite.addTest(jmri.jmrit.operations.routes.OperationsRoutesTest.suite());
		suite.addTest(jmri.jmrit.operations.trains.OperationsTrainsTest.suite());

		// GUI tests start here
		suite.addTest(jmri.jmrit.operations.locations.OperationsLocationsGuiTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.cars.OperationsCarsGuiTest.suite());
		suite.addTest(jmri.jmrit.operations.routes.OperationsRoutesGuiTest.suite());
		suite.addTest(jmri.jmrit.operations.trains.OperationsTrainsGuiTest.suite());
		
		return suite;
	}

}
