// AwtHandler.java

package jmri.util.exceptionhandler;

/**
 * Class to log exceptions that rise to the top of 
 * the AWT event processing loop.
 *
 * Using code must install this with
<pre>
  System.setProperty("sun.awt.exception.handler", jmri.util.AwtHandler.class.getName());
</pre>
 * @author Bob Jacobsen  Copyright 2010
 * @version $Revision: 1.1 $
 */

public class AwtHandler {

    public void handle(Throwable t) {
        log.error("Unhandled AWT Exception: "+t, t);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AwtHandler.class.getName());
}