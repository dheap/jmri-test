// SerialListener.java

package jmri.jmrix.cmri.serial;


/**
 * Listener interface to be notified about serial C/MRI traffic
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public interface SerialListener extends java.util.EventListener{
    public void message(SerialMessage m);
    public void reply(SerialReply m);
}


/* @(#)SerialListener.java */
