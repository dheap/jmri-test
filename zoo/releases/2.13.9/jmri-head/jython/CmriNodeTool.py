#
# Find and enable/disable CMRI card polling, interactive
#
# Author: Ken Cameron, copyright 2009
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision$
##

import jmri
import javax.swing

# setup interface to manage CMRI nodes
class CmriNodeTool(jmri.jmrit.automat.AbstractAutomaton) :
    nodeList = []
    nodeAddrList = []
    checkYList = []
    scriptFrame = None
    maxNodeAddr = 128

    def init(self) :
        print "init()"
        i = 0
        while i < self.maxNodeAddr :
            x = (i * 1000) + 1
            txt = "CT" + x.toString()
            #print "looking for node " + i.toString() + " using name " + txt
            node = jmri.jmrix.cmri.serial.SerialAddress.getNodeFromSystemName(txt)
            if (node != None) :
                print "found node for " + txt
                self.nodeList.append(node)
                self.nodeAddrList.append(i)
            i = i + 1
        return

    # handle the checkbox
    # the label gives the index for each board in the array
    def whenCheckbox(self, event) :
        ptr = event.getSource()
        if (ptr.isSelected() == 1) :
            self.nodeList[int(ptr.getLabel())].setSensorsActive(True)
        else :
            self.nodeList[int(ptr.getLabel())].setSensorsActive(False)
        return

    def setup(self) :
        self.scriptFrame = javax.swing.JFrame("CMRI Node Controls")
        self.scriptFrame.contentPane.setLayout(javax.swing.BoxLayout(self.scriptFrame.contentPane, javax.swing.BoxLayout.Y_AXIS))
        gLayout = java.awt.GridBagLayout()
        gConstraints = java.awt.GridBagConstraints()
        pane2 = javax.swing.JPanel()
        pane2Border = javax.swing.BorderFactory.createEtchedBorder()
        pane2Titled = javax.swing.BorderFactory.createTitledBorder(pane2Border, "CMRI Node Polling")
        pane2.setBorder(pane2Titled)
        pane2.setLayout(gLayout)
        gConstraints.gridx = 0
        gConstraints.gridy = 0
        gConstraints.gridwidth = 1
        gConstraints.gridheight = 1
        gConstraints.ipadx = 12
        gConstraints.ipady = 3
        gConstraints.insets = java.awt.Insets(3, 3, 3, 3)

        pane2.add(javax.swing.JLabel("Node\nAddr"), gConstraints)
        gConstraints.gridx = gConstraints.gridx + 1
        pane2.add(javax.swing.JLabel("Polling\nEnabled"), gConstraints)
        gConstraints.gridx = 0
        gConstraints.gridy = gConstraints.gridy + 1
        
        i = 0
        while i < len(self.nodeAddrList) :
            pane2.add(javax.swing.JLabel(self.nodeAddrList[i].toString()), gConstraints)
            gConstraints.gridx = gConstraints.gridx + 1
            box = javax.swing.JCheckBox()
            box.setToolTipText("Enable/Disable Polling on node " + self.nodeAddrList[i].toString())
            box.setSelected(self.nodeList[i].getSensorsActive())    
            box.actionPerformed = self.whenCheckbox
            box.setLabel(i.toString())
            pane2.add(box, gConstraints)
            gConstraints.gridx = 0
            gConstraints.gridy = gConstraints.gridy + 1
            i = i + 1

        # Put contents in frame and display
        print "setup: packing frame"
        self.scriptFrame.contentPane.add(pane2)
        self.scriptFrame.pack()
        self.scriptFrame.show()
        return

# create one of these
print "Creating CMRI Node Tool"
a = CmriNodeTool()
#print "Calling init() for CMRI Node Tool"
a.init()
#print "Calling setup() for CMRI Node Tool"
a.setup()
