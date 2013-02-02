// CanMessage.java

package jmri.jmrix.can;

import org.apache.log4j.Logger;
import jmri.jmrix.AbstractMRMessage;

/**
 * Base class for messages in a CANbus based message/reply protocol.
 * <P>
 * It is expected that any CAN based system will be based upon basic CANbus
 * concepts such as ID (standard or extended), Normal and RTR frames and
 * a data field.
 * <P>
 * The _dataChars[] and _nDataChars members refer to the data field, not the
 * entire message.
 *<p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately
 * set via the "extended" parameter
 *<p>
 * CBUS uses a 2-bit "Pri" field and 7-bit "ID" ("CAN ID") field, with
 * separate accessors.
 * CBUS ID is set as a layout connection preference and registered by the
 * traffic controller.
 *
 * @author      Andrew Crosland Copyright (C) 2008
 * @author      Bob Jacobsen Copyright (C) 2008, 2009, 2010
 * @version     $Revision$
 */
public class CanMessage extends AbstractMRMessage implements CanMutableFrame {
    
    // tag whether translation is needed.
    // a "native" message has been converted already
    boolean _translated = false;
    public void setTranslated(boolean translated) { _translated = translated; }
    public boolean isTranslated() { return _translated; }
    
    public CanMessage(int header){
        _header=header;
        _isExtended = false;
        _isRtr = false;
        _nDataChars = 8;
        setBinary(true);
        _dataChars = new int[8];
    }
    
    // create a new one of given length
    public CanMessage(int i, int header) {
        this(header);
        _nDataChars = (i <= 8) ? i : 8;
    }
    
    // create a new one from an array
    public CanMessage(int [] d, int header) {
        this(header);
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i];
        }
    }
    
    // create a new one from a byte array, as a service
    public CanMessage(byte [] d, int header) {
        this(header);
        _nDataChars = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = d[i]&0xFF;
        }
    }
    
    // copy one
    @SuppressWarnings("null")
	public  CanMessage(CanMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        setBinary(true);
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }
    
    // copy type
    @SuppressWarnings("null")
	public  CanMessage(CanReply m) {
        if (m == null)
            log.error("copy ctor of null message");
        _header = m._header;
        _isExtended = m._isExtended;
        _isRtr = m._isRtr;
        setBinary(true);
        _nDataChars = m.getNumDataElements();
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m.getElement(i);
    }

    /**
     * Hash on the header
     */
    public int hashCode() {
        return _header;
    }
    
    /** 
     * Note that a CanMessage and a CanReply can be tested for equality
     */
    public boolean equals(Object a) {
        if (a == null) return false;
        // check for CanFrame equality, that's sufficient
        if (a instanceof CanFrame) {
            CanFrame m = (CanFrame) a;
            if ( (_header!=m.getHeader())||(_isRtr!=m.isRtr())||(_isExtended!=m.isExtended()))
                return false;
            if ( _nDataChars != m.getNumDataElements() ) return false;
            for (int i = 0; i<_nDataChars; i++) {
                if (_dataChars[i] != m.getElement(i)) return false;
            }
            return true;
        } else return false;
   }
    
    public boolean replyExpected() { return false; }
    
    // accessors to the bulk data
    public int getNumDataElements() { return _nDataChars;}
    public void setNumDataElements(int n) { _nDataChars = n; }
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }
    
    public void setData(int [] d) {
        int len = (d.length <=8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            _dataChars[i] = d[i];
        }
    }
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array, can be directly manipulated
    public int[] getData() { return _dataChars; }
    
    
    // CAN header
    public int getHeader() { return _header; }
    public void setHeader(int h) { _header = h; }
    
    public boolean isExtended() { return _isExtended; }
    public void setExtended(boolean b) { _isExtended = b; }
    
    public boolean isRtr() { return _isRtr; }
    public void setRtr(boolean b) { _isRtr = b; }
    
    // contents (package access)
    int _header;
    boolean _isExtended;
    boolean _isRtr;
        
    static Logger log = Logger.getLogger(CanMessage.class.getName());
}

/* @(#)CanMessage.java */
