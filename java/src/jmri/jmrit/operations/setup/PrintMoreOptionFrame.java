// PrintMoreOptionFrame.java
package jmri.jmrit.operations.setup;

import java.awt.Dimension;
import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of additional manifest print options
 *
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 21846 $
 */
public class PrintMoreOptionFrame extends OperationsFrame {

    /**
     *
     */
    private static final long serialVersionUID = -6234971819984062486L;

    public PrintMoreOptionFrame() {
        super(Bundle.getMessage("TitlePrintMoreOptions"), new PrintMoreOptionPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight400));
    }

    //private static final Logger log = LoggerFactory.getLogger(PrintMoreOptionFrame.class);
}
