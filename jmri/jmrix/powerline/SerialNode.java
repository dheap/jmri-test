// SerialNode.java

package jmri.jmrix.powerline;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;

/**
 * Models a serial node.
 * <P>
 * Nodes are numbered ala their address, from 0 to 255.
 * Node number 1 carries sensors 1 to 999, node 2 1001 to 1999 etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision: 1.4 $
 */
public class SerialNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     * Note this is less than a current SUSIC motherboard can have,
     * but should be sufficient for all reasonable layouts.
     * <P>
     * Must be less than,
     * {@link SerialSensorManager#SENSORSPERNODE}
     */
    static final int MAXSENSORS = 16;
    static final int MAXTURNOUTS = 32;
    
    // class constants
    
    // board types
    public static final int DAUGHTER = 0;  // also default
    public static final int CABDRIVER = 1;

    public static final String[] boardNames = new String[]{"Daughter", "CabDriver"};
    public static final int[] outputBits = new int[]{32, 32};
    public static final int[] inputBits = new int[]{16, 16};
    
    // node definition instance variables (must persist between runs)
    public int nodeAddress = 0;                 // Node address, 0-127 allowed
    protected int nodeType = DAUGHTER;          // See above

    // operational instance variables  (should not be preserved between runs)
    protected boolean needSend = true;          // 'true' if something has changed in the outputByte array since
                                                //    the last send to the hardware node
    protected boolean[] outputArray = new boolean[MAXTURNOUTS+1]; // current values of the output bits for this node
    protected boolean[] outputBitChanged = new boolean[MAXTURNOUTS+1];
    
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS+1];
    protected int[] sensorLastSetting = new int[MAXSENSORS+1];
    protected int[] sensorTempSetting = new int[MAXSENSORS+1];

    /**
     * Assumes a node address of 0, and a node type of 0 (IO24)
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode() {
        this (0,DAUGHTER);
    }

    /**
     * Creates a new SerialNode and initialize default instance variables
     *   address - Address of node on serial bus (0-255)
     *   type - a type constant from the class
     */
    public SerialNode(int address, int type) {
        // set address and type and check validity
        setNodeAddress (address);
        setNodeType (type);
        // set default values for other instance variables
        // clear the Sensor arrays
        for (int i = 0; i<MAXSENSORS+1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
        }
        // clear all output bits
        for (int i = 0; i<MAXTURNOUTS+1; i++) {
            outputArray[i] = false;
            outputBitChanged[i] = false;
        }
        // initialize other operational instance variables
        needSend = true;
        hasActiveSensors = false;
        // register this node
        SerialTrafficController.instance().registerSerialNode(this);
    }

    	    	
    /**
     * Public method setting an output bit.
     *    Note:  state = 'true' for 0, 'false' for 1
     *           bits are numbered from 1 (not 0)
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // validate that this bit number is defined
        if (bitNumber > outputBits[nodeType] ) {
            warn("Output bit out-of-range for defined node: "+bitNumber);
            return;
        }
        // update the bit
        boolean oldBit = outputArray[bitNumber];
        outputArray[bitNumber] = state;
        
        // check for change, necessitating a send
        if (oldBit != outputArray[bitNumber]) {
            needSend = true;
            outputBitChanged[bitNumber] = true;
        }
    }

    /**
     * Public method to return state of Sensors.
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean getSensorsActive() { return hasActiveSensors; }

    /**
     * Public method to return state of needSend flag.
     */
    public boolean mustSend() { return needSend; }

    /**
     * Public to reset state of needSend flag.
     * Can only reset if there are no bytes that need to be
     * sent
     */
    public void resetMustSend() { 
        for (int i = 0; i < outputBits[nodeType]; i++) {
            if (outputBitChanged[i]) return;
        }
        needSend = false; 
    }
    /**
     * Public to set state of needSend flag.
     */
    public void setMustSend() { needSend = true; }

    /**
     * Public method to return node type
     *   Current types are:
     *      SMINI, USIC_SUSIC,
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Public method to set node type.
     */
    public void setNodeType(int type) {
        nodeType = type;
        switch (nodeType) {
            default:
                log.error("Unexpected nodeType in setNodeType: "+nodeType);
                // use IO-48 as default
            case DAUGHTER:
            case CABDRIVER:
                break;
        }
    }

    /**
     * Public method to return the node address.
     */
    public int getNodeAddress() {
        return (nodeAddress);
    }

    /**
     * Public method to set the node address.
     *   address - node address set in dip switches (0 - 255)
     */
    public void setNodeAddress(int address) {
        if ( (address >= 0) && (address < 128) ) {
            nodeAddress = address;
        }
        else {
            log.error("illegal node address: "+Integer.toString(address));
            nodeAddress = 0;
        }
    }


    /**
     * Public Method to create an Initialization packet (SerialMessage) for this node.
     * There are currently no devices that need an init message, so this
     * returns null.
     */
    public SerialMessage createInitPacket() {
        return null;
    }
    
    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public SerialMessage createOutPacket() {
        // now, for testing, instead of sending m (the packet) we send null
        return null;        
    }

    boolean warned = false;

    void warn(String s) {
    	if (warned) return;
    	warned = true;
    	log.warn(s);
    }

    /**
     * Use the contents of the poll reply to mark changes
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        try {
            //get all input in one bit string
            int inputBits = (l.getElement(0)&0xFF)+((l.getElement(1)&0xF)<<8);
            
            for (int i=0; i<=lastUsedSensor; i++) {
                if (sensorArray[i] == null) continue; // skip ones that don't exist
                boolean value = ((inputBits&1)!=0);
                inputBits = inputBits>>1;
                if ( value ) {
                    // bit set, considered ACTIVE
                    if (    ( (sensorTempSetting[i] == Sensor.ACTIVE) || 
                                (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                            ( sensorLastSetting[i] != Sensor.ACTIVE) ) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.ACTIVE;
                } else {
                    // bit reset, considered INACTIVE
                    if (    ( (sensorTempSetting[i] == Sensor.INACTIVE)  || 
                                (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                            ( sensorLastSetting[i] != Sensor.INACTIVE) ) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.INACTIVE;
                }
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }

    /**
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS.
     * @param s - Sensor object
     * @param i - 0 to MAXSENSORS number of sensor's input bit on this node
     */
    public void registerSensor(Sensor s, int i) {
        // validate the sensor ordinal
        if ( (i<0) || (i> (inputBits[nodeType] - 1)) || (i>MAXSENSORS) ) {
            log.error("Unexpected sensor ordinal in registerSensor: "+Integer.toString(i+1));
            return;
        }
        hasActiveSensors = true;
        if (sensorArray[i] == null) {
            sensorArray[i] = s;
            if (lastUsedSensor<i) {
                lastUsedSensor = i;
            }
        }
        else {
            // multiple registration of the same sensor
            log.warn("multiple registration of same sensor: CS"+
                    Integer.toString((nodeAddress*SerialSensorManager.SENSORSPERNODE) + i + 1) );
        }
    }

    int timeout = 0;
    /**
     *
     * @return true if initialization required
     */
    boolean handleTimeout(AbstractMRMessage m) {
        timeout++;
        // normal to timeout in response to init, output
        if (m.getElement(1)!=0x50) return false;
        
        // see how many polls missed
        if (log.isDebugEnabled()) log.warn("Timeout to poll for addr="+nodeAddress+": consecutive timeouts: "+timeout);
        
        if (timeout>5) { // enough, reinit
            // reset timeout count to zero to give polls another try
            timeout = 0;
            // reset poll and send control so will retry initialization
            setMustSend();
            return true;   // tells caller to force init
        } 
        else return false;
    }
    void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
