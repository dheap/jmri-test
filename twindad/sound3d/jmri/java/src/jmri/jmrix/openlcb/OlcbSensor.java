// OlcbSensor.java

package jmri.jmrix.openlcb;

import jmri.implementation.AbstractSensor;
import jmri.Sensor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Extend jmri.AbstractSensor for OpenLCB controls.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2008, 2010, 2011
 * @version         $Revision$
 */
public class OlcbSensor extends AbstractSensor implements CanListener {

    OlcbAddress addrActive;    // go to active state
    OlcbAddress addrInactive;  // go to inactive state

    public OlcbSensor(String prefix, String address, TrafficController tc) {
        super(prefix+"S"+address);
        this.tc = tc;
        init(address);
    }
    
    TrafficController tc;

    /**
     * Common initialization for both constructors.
     * <p>
     * 
     */
    private void init(String address) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+address);
            return;
        }
        switch (v.length) {
            case 1:
                addrActive = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrInactive = new OlcbAddress("-"+address);
                } else if (address.startsWith("-")) {
                    addrInactive = new OlcbAddress("+"+address);
                } else {
                    log.error("can't make 2nd event from systemname "+address);
                    return;
                }
                break;
            case 2:
                addrActive = v[0];
                addrInactive = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Sensor system name: "+address);
                return;
        }
        // connect
        tc.addCanListener(this);
    }

    /**
     * Request an update on status by sending CBUS message.
     * <p>
     * There is no known way to do this, so the request is
     * just ignored.
     */
    public void requestUpdateFromLayout() {
    }

    /**
     * User request to set the state, which means that we broadcast that to
     * all listeners by putting it out on CBUS.
     * In turn, the code in this class should use setOwnState to handle
     * internal sets and bean notifies.
     * @param s
     * @throws JmriException
     */
    public void setKnownState(int s) throws jmri.JmriException {
        CanMessage m;
        if (s==Sensor.ACTIVE) {
            m = addrActive.makeMessage();
            tc.sendCanMessage(m, this);
            setOwnState(Sensor.ACTIVE);
        } else if (s==Sensor.INACTIVE) {
            m = addrInactive.makeMessage();
            tc.sendCanMessage(m, this);
            setOwnState(Sensor.INACTIVE);
        }
    }

    /**
     * Track layout status from messages being sent to CAN
     * @param f
     */
    public void message(CanMessage f) {
        if (addrActive.match(f)) {
            setOwnState(Sensor.ACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(Sensor.INACTIVE);
        }
    }

    /**
     * Track layout status from messages being received from CAN
     * @param f
     */
    public void reply(CanReply f) {
        if (addrActive.match(f)) {
            setOwnState(Sensor.ACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(Sensor.INACTIVE);
        }
    }

    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbSensor.class.getName());

}


/* @(#)OlcbSensor.java */
