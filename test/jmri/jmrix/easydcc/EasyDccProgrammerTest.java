/**
 * EasyDccProgrammerTest.java
 *
 * Description:	    JUnit tests for the EasyDccProgrammer class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.easydcc;

import jmri.*;

import java.util.*;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.easydcc.EasyDccProgrammer;

public class EasyDccProgrammerTest extends TestCase {

	public void testWriteSequence() throws JmriException {
		// infrastructure objects
		EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
		EasyDccListenerScaffold l = new EasyDccListenerScaffold();

		EasyDccProgrammer p = new EasyDccProgrammer();

		// and do the write
		p.writeCV(10, 20, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
		Assert.assertEquals("mode message contents", "M",
			((EasyDccMessage)(t.outbound.elementAt(0))).toString());
		// reply from programmer arrives
		EasyDccReply r = new EasyDccReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check write message sent
		Assert.assertEquals("write message sent", 2, t.outbound.size());
		Assert.assertEquals("write message contents", "P010 020",
			((EasyDccMessage)(t.outbound.elementAt(1))).toString());
		// reply from programmer arrives
		r = new EasyDccReply();
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check "leave prog mode" message sent
		Assert.assertEquals("normal mode message sent", 3, t.outbound.size());
		Assert.assertEquals("normal mode message contents", "X",
			((EasyDccMessage)(t.outbound.elementAt(2))).toString());
		// reply from programmer arrives
		r = new EasyDccReply();
		t.sendTestReply(r);
		Assert.assertEquals(" listener invoked", 1, rcvdInvoked);
		Assert.assertEquals(" got data value back", 20, rcvdValue);
	}

	public void testReadSequence() throws JmriException {
		log.error("expect next message: ERROR - Creating too many EasyDccProgrammer objects");
		// infrastructure objects
		EasyDccInterfaceScaffold t = new EasyDccInterfaceScaffold();
		EasyDccListenerScaffold l = new EasyDccListenerScaffold();

		EasyDccProgrammer p = new EasyDccProgrammer();

		// and do the read
		p.readCV(10, l);
		// check "prog mode" message sent
		Assert.assertEquals("mode message sent", 1, t.outbound.size());
		Assert.assertEquals("mode message contents", "M",
			((EasyDccMessage)(t.outbound.elementAt(0))).toString());
		// reply from programmer arrives
		EasyDccReply r = new EasyDccReply("**** PROGRAMMING MODE - MAIN TRACK NOW DISCONNECTED ****");
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);


		// check "read command" message sent
		Assert.assertEquals("read message sent", 2, t.outbound.size());
		Assert.assertEquals("read message contents", "R010",
			((EasyDccMessage)(t.outbound.elementAt(1))).toString());
		// reply from programmer arrives
		r = new EasyDccReply();
		r.setElement(0, '0');
		r.setElement(1, '2');
		r.setElement(2, '0');
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener not invoked", 0, rcvdInvoked);

		// check "leave prog mode" message sent
		Assert.assertEquals("normal mode message sent", 3, t.outbound.size());
		Assert.assertEquals("normal mode message contents", "X",
			((EasyDccMessage)(t.outbound.elementAt(2))).toString());
		// reply from programmer arrives
		r = new EasyDccReply();
		t.sendTestReply(r);
		Assert.assertEquals(" programmer listener invoked", 1, rcvdInvoked);
		Assert.assertEquals(" value read", 20, rcvdValue);
	}

	// internal class to simulate a EasyDccListener
	class EasyDccListenerScaffold implements jmri.ProgListener {
		public EasyDccListenerScaffold() {
			rcvdInvoked = 0;;
			rcvdValue = 0;
			rcvdStatus = 0;
		}
		public void programmingOpReply(int value, int status) {
			rcvdValue = value;
			rcvdStatus = status;
			rcvdInvoked++;
		}
	}
	int rcvdValue;
	int rcvdStatus;
	int rcvdInvoked;

	// service internal class to handle transmit/receive for tests
	class EasyDccInterfaceScaffold extends EasyDccTrafficController {
		public EasyDccInterfaceScaffold() {
		}

		// override some EasyDccInterfaceController methods for test purposes

		public boolean status() { return true;
		}

		/**
	 	* record messages sent, provide access for making sure they are OK
	 	*/
		public Vector outbound = new Vector();  // public OK here, so long as this is a test class
		public void sendEasyDccMessage(EasyDccMessage m, jmri.jmrix.easydcc.EasyDccListener l) {
			if (this.log.isDebugEnabled()) this.log.debug("sendEasyDccMessage ["+m+"]");
			// save a copy
			outbound.addElement(m);
			lastSender = l;
		}

		// test control member functions

		/**
		 * forward a message to the listeners, e.g. test receipt
		 */
		protected void sendTestMessage (EasyDccMessage m) {
			// forward a test message to Listeners
			if (this.log.isDebugEnabled()) this.log.debug("sendTestMessage    ["+m+"]");
			notifyMessage(m, null);
			return;
		}
		protected void sendTestReply (EasyDccReply m) {
			// forward a test message to Listeners
			if (this.log.isDebugEnabled()) this.log.debug("sendTestReply    ["+m+"]");
			notifyReply(m);
			return;
		}

		/*
		* Check number of listeners, used for testing dispose()
		*/
		public int numListeners() {
			return cmdListeners.size();
		}

	}

	// from here down is testing infrastructure

	public EasyDccProgrammerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {EasyDccProgrammerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EasyDccProgrammerTest.class);
		return suite;
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccProgrammerTest.class.getName());

}
