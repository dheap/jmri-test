// SerialDriverAdapter.java

package jmri.jmrix.mrc.serialdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.mrc.MrcPortController;
import jmri.jmrix.mrc.MrcTrafficController;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * Implements SerialPortAdapter for the MRC system.  This connects
 * an MRC command station via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class SerialDriverAdapter extends MrcPortController  implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;
    
    public SerialDriverAdapter() {
        setManufacturer(jmri.jmrix.DCCManufacturerList.MRC);
        options.put("CabAddress", new Option("Cab Address:", validOption1));
        adaptermemo = new MrcSystemConnectionMemo();
    }

    public String openPort(String portName, String appName)  {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            }
            catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(currentBaudNumber(getCurrentBaudRate()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
                      +" "+activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            int count = serialStream.available();
            log.debug("input stream shows "+count+" bytes available");
            while ( count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName+" port opened at "
                         +activeSerialPort.getBaudRate()+" baud, sees "
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
                         +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
            ex.printStackTrace();
            return "Unexpected error while opening port "+portName+": "+ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an serial command
     * station connected to this port
     */
    public void configure() {
        MrcTrafficController tc = new MrcTrafficController(); 
        adaptermemo.setMrcTrafficController(tc);
        tc.setAdapterMemo(adaptermemo);
        tc.setCabNumber(Integer.parseInt(getOptionState("CabAddress")));
        tc.connectPort(this);
        
        adaptermemo.configureManagers();

        jmri.jmrix.mrc.ActiveFlag.setActive();
    }

    // base class methods for the MrcPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Get an array of valid baud rates. 
     */
    public String[] validBaudRates() {
        return new String[]{"38,400 bps"};
    }

    /**
     * Return array of valid baud rates as integers.
     */
    public int[] validBaudNumber() {
        return new int[]{38400};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;
    
    protected String [] validOption1 = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    
    
    
    public MrcSystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    
    static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}

