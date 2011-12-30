// SpeedoConsoleFrame.java

package jmri.jmrix.bachrus;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.JComboBox;

import javax.swing.border.EtchedBorder;
import jmri.CommandStation;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.util.JmriJFrame;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgListener;


/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 * 
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision$
 */
public class SpeedoConsoleFrame extends JmriJFrame implements SpeedoListener,
                                                        ThrottleListener, 
                                                        ProgListener,
                                                        PropertyChangeListener {

    /***
     * TODO:
     *  Complete the help file
     *  Allow selection of arbitrary scale
     */

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.bachrus.BachrusBundle");

    private PowerManager pm = null;
    
    // member declarations
    protected JLabel scaleLabel = new JLabel();
    protected JTextField speedTextField = new JTextField(12);

    protected ButtonGroup speedGroup = new ButtonGroup();
    protected JRadioButton mphButton = new JRadioButton(rb.getString("MPH"));
    protected JRadioButton kphButton = new JRadioButton(rb.getString("KPH"));
    protected ButtonGroup displayGroup = new ButtonGroup();
    protected JRadioButton numButton = new JRadioButton(rb.getString("Numeric"));
    protected JRadioButton dialButton = new JRadioButton(rb.getString("Dial"));
    protected SpeedoDial speedoDialDisplay = new SpeedoDial();
    protected JRadioButton dirFwdButton = new JRadioButton(rb.getString("Fwd"));
    protected JRadioButton dirRevButton = new JRadioButton(rb.getString("Rev"));

    GraphPane profileGraphPane;
    protected JLabel profileAddressLabel = new JLabel(rb.getString("LocoAddress"));
    protected JTextField profileAddressField = new JTextField(6);
    protected JButton readAddressButton = new JButton(rb.getString("Read"));
    protected JButton trackPowerButton = new JButton(rb.getString("PowerUp"));
    protected JButton startProfileButton = new JButton(rb.getString("Start"));
    protected JButton stopProfileButton = new JButton(rb.getString("Stop"));
    protected JButton exportProfileButton = new JButton(rb.getString("Export"));
    protected JButton printProfileButton = new JButton(rb.getString("Print"));
    protected JLabel statusLabel = new JLabel(" ");

    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();

    protected String[] scaleStrings = new String [] {
        rb.getString("ScaleZ"),
        rb.getString("ScaleEuroN"),
        rb.getString("ScaleNFine"),
        rb.getString("ScaleJapaneseN"),
        rb.getString("ScaleBritishN"),
        rb.getString("Scale3mm"),
        rb.getString("ScaleTT"),
        rb.getString("Scale00"),
        rb.getString("ScaleH0"),
        rb.getString("ScaleS"),
        rb.getString("Scale048"),
        rb.getString("Scale045"),
        rb.getString("Scale043")/*,
        rb.getString("ScaleOther")*/
        };

    protected float[] scales = new float[] {
        220,
        160,
        152,
        150,
        148,
        120,
        101.6F,
        76,
        87,
        64,
        48,
        45,
        43/*,
        -1*/
        };

    protected static final int defaultScale = 8;

    protected float selectedScale = 0;
    protected int series = 0;
    protected float sampleSpeed = 0;
    protected float targetSpeed = 0;
    protected float currentSpeed = 0;
    protected float incSpeed = 0;
    protected float oldSpeed = 0;
    protected float acc = 0;
    protected float avSpeed = 0;
    protected int range = 1;
    protected float circ = 0;
    protected float count = 1;
    protected float freq;
    protected static final int DISPLAY_UPDATE = 500;
    protected static final int FAST_DISPLAY_RATIO = 3;

    /*
     * At low speed, readings arrive less often and less filtering
     * is applied to minimise the delay in updating the display
     * 
     * Speed measurement is split into 4 ranges with an overlap, tp
     * prevent "hunting" between the ranges.
     */
    protected static final int RANGE1LO = 0;
    protected static final int RANGE1HI = 9;
    protected static final int RANGE2LO = 7;
    protected static final int RANGE2HI = 31;
    protected static final int RANGE3LO = 29;
    protected static final int RANGE3HI = 62;
    protected static final int RANGE4LO = 58;
    protected static final int RANGE4HI = 9999;
    static final int[] filterLength = {0, 3, 6, 10, 20};
    protected enum DisplayType {NUMERIC, DIAL}
    protected DisplayType display = DisplayType.NUMERIC;

    /*
     * Keep track of the DCC services available
     */
    protected int dccServices;
    protected static final int BASIC = 0;
    protected static final int PROG = 1;
    protected static final int COMMAND = 2;
    protected static final int THROTTLE = 4;

    protected boolean timerRunning = false;

    protected DccSpeedProfile spFwd;
    protected DccSpeedProfile spRev;
    protected enum ProfileState {IDLE, WAIT_FOR_THROTTLE, RUNNING}
    protected ProfileState state = ProfileState.IDLE;
    protected enum ProfileDirection {FORWARD, REVERSE}
    protected ProfileDirection profileDir = ProfileDirection.FORWARD;
    protected DccThrottle throttle = null;
    protected int profileStep = 0;
    protected float profileSpeed;
    protected float profileIncrement;
    protected int profileAddress = 0;
    protected Programmer prog = null;
    protected CommandStation commandStation = null;
    protected enum ProgState {IDLE, WAIT29, WAIT1, WAIT17, WAIT18}
    protected ProgState readState = ProgState.IDLE;

    //Create the combo box, select item at index 4.
    //Indices start at 0, so 4 specifies british N.
    JComboBox scaleList = new JComboBox(scaleStrings);

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;
    String replyString;

    public SpeedoConsoleFrame() {
        super();
    }
    
    protected String title() { return rb.getString("SpeedoConsole"); }
    
    public void dispose() {
        SpeedoTrafficController.instance().removeSpeedoListener(this);
        super.dispose();
    }
    
    public void initComponents() throws Exception {
        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // What services do we have?
        dccServices = BASIC;
        if (InstanceManager.programmerManagerInstance()!=null &&
            InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()) {
            prog = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        	dccServices |= PROG;
        }
        if (false /*jmri.InstanceManager.commandStationInstance() != null*/) {
            // We'll use the command station to send explicit speed steps
            commandStation = InstanceManager.commandStationInstance();
            log.info("Using CommandStation interface for profiling");
            dccServices |= COMMAND;
        } else if (InstanceManager.throttleManagerInstance()!=null) {
            // otherwise we'll send speed commands
            log.info("Using Throttle interface for profiling");
        	dccServices |= THROTTLE;
        }
        
        if (InstanceManager.powerManagerInstance() != null) {
            pm = InstanceManager.powerManagerInstance();
            pm.addPropertyChangeListener(this);
        }

        /*
         * Setup pane for basic operations
         */
        JPanel basicPane = new JPanel(); 
        basicPane.setLayout(new BoxLayout(basicPane, BoxLayout.Y_AXIS));

        // Scale panel
        JPanel scalePanel = new JPanel();
        scalePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), rb.getString("SelectScale")));
        scalePanel.setLayout(new FlowLayout());
        
        scaleList.setToolTipText("Select the scale");
        scaleList.setSelectedIndex(defaultScale);
        selectedScale = scales[defaultScale];

        // Listen to selection of scale
        scaleList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                selectedScale = scales[cb.getSelectedIndex()];
                // *** check if -1 and enable text entry box
            }
        });

        scaleLabel.setText(rb.getString("Scale"));
        scaleLabel.setVisible(true);
        
        readerLabel.setText(rb.getString("UnknownReader"));
        readerLabel.setVisible(true);

        scalePanel.add(scaleLabel);
        scalePanel.add(scaleList);
        scalePanel.add(readerLabel);
        
        basicPane.add(scalePanel);
 
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), rb.getString("MeasuredSpeed")));
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.X_AXIS));

        // Display Panel which is a card layout with cards to show
        // numeric or dial type speed display
        final JPanel displayCards = new JPanel();
        displayCards.setLayout(new CardLayout());
 
        // Numeric speed card
        JPanel numericSpeedPanel = new JPanel();
        numericSpeedPanel.setLayout(new BoxLayout(numericSpeedPanel, BoxLayout.X_AXIS));
        Font f = new Font("", Font.PLAIN, 96);
        speedTextField.setFont(f);
        speedTextField.setHorizontalAlignment(JTextField.RIGHT);
        speedTextField.setColumns(3);
        speedTextField.setText("0.0");
        speedTextField.setVisible(true);
        speedTextField.setToolTipText(rb.getString("SpeedHere"));
        numericSpeedPanel.add(speedTextField);

        // Dial speed card
        JPanel dialSpeedPanel = new JPanel();
        dialSpeedPanel.setLayout(new BoxLayout(dialSpeedPanel, BoxLayout.X_AXIS));
        dialSpeedPanel.add(speedoDialDisplay);
        speedoDialDisplay.update(0.0F);

        // Add cards to panel
        displayCards.add(dialSpeedPanel, "DIAL");
        displayCards.add(numericSpeedPanel, "NUMERIC");
        CardLayout cl = (CardLayout)displayCards.getLayout();
        cl.show(displayCards, "DIAL");
        
        // button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        speedGroup.add(mphButton);
        speedGroup.add(kphButton);
        mphButton.setSelected(true);
        mphButton.setToolTipText(rb.getString("TTDisplayMPH"));
        kphButton.setToolTipText(rb.getString("TTDisplayKPH"));
        displayGroup.add(numButton);
        displayGroup.add(dialButton);
        dialButton.setSelected(true);
        numButton.setToolTipText(rb.getString("TTDisplayNumeric"));
        dialButton.setToolTipText(rb.getString("TTDisplayDial"));
        buttonPanel.add(mphButton);
        buttonPanel.add(kphButton);
        buttonPanel.add(numButton);
        buttonPanel.add(dialButton);
      
        speedPanel.add(displayCards);
        speedPanel.add(buttonPanel);
        
        // Listen to change of units, convert current average and update display
        mphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.setUnitsMph();
                profileGraphPane.repaint();
                speedoDialDisplay.setUnitsMph();
                speedoDialDisplay.repaint();
            }
        });
        kphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.setUnitsKph();
                profileGraphPane.repaint();
                speedoDialDisplay.setUnitsKph();
                speedoDialDisplay.repaint();
            }
        });

        // Listen to change of display
        numButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                display = DisplayType.NUMERIC;
                CardLayout cl = (CardLayout)displayCards.getLayout();
                cl.show(displayCards, "NUMERIC");
            }
        });
        dialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                display = DisplayType.DIAL;
                CardLayout cl = (CardLayout)displayCards.getLayout();
                cl.show(displayCards, "DIAL");
            }
        });

        basicPane.add(speedPanel);

        /*
         * Pane for profiling loco speed curve
         */
        JPanel profilePane = new JPanel();
        //profilePane.setLayout(new BoxLayout(profilePane, BoxLayout.Y_AXIS));
        profilePane.setLayout(new BorderLayout());

        // pane to hold address
        JPanel profileAddressPane = new JPanel();
        profileAddressPane.setLayout(new FlowLayout());
        profileAddressPane.add(profileAddressLabel);
        profileAddressPane.add(profileAddressField);
        profileAddressField.setToolTipText(rb.getString("EnterLoc"));
        profileAddressPane.add(readAddressButton);
        readAddressButton.setToolTipText(rb.getString("ReadLoco"));
        
        if ((dccServices & PROG) != PROG) {
            // User must enter address
            readAddressButton.setEnabled(false);
            profileAddressField.setText(Integer.toString(0));
        }

        profileAddressField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
                try {
                    if(!profileAddressField.getText().equals("")){
                        int userinput = Integer.parseInt(profileAddressField.getText());
                        profileAddress = userinput;
                        profileAddressField.setBackground(Color.WHITE);
                    } else {
                        profileAddress = 0;
                    }
                } catch (NumberFormatException ex) {
                    log.error("Non numeric address entered " + ex);
                    if(profileAddress==0){
                        profileAddressField.setText("");
                    } else {
                        profileAddressField.setText(Integer.toString(profileAddress));
                    }
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        // Listen to text entry
        profileAddressField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    profileAddress = Integer.parseInt(profileAddressField.getText());
                    profileAddressField.setBackground(Color.WHITE);
                } catch (NumberFormatException ex) {
                    log.error("Non numeric address entered " + ex);
                    profileAddress = 0;
                    profileAddressField.setText("0");
                }
            }
        });

        // Listen to read button
        readAddressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readAddress();
            }
        });

        profilePane.add(profileAddressPane, BorderLayout.NORTH);
        
        // pane to hold the graph
        spFwd = new DccSpeedProfile(29);       // 28 step plus step 0
        spRev = new DccSpeedProfile(29);       // 28 step plus step 0
        profileGraphPane = new GraphPane(spFwd, spRev);
        profileGraphPane.setPreferredSize(new Dimension(600, 300));
        profileGraphPane.setXLabel(rb.getString("SpeedStep"));
        profileGraphPane.setUnitsMph();

        profilePane.add(profileGraphPane, BorderLayout.CENTER);
        
        // pane to hold the buttons
        JPanel profileButtonPane = new JPanel();
        profileButtonPane.setLayout(new FlowLayout());
        profileButtonPane.add(trackPowerButton);
        trackPowerButton.setToolTipText(rb.getString("TTPower"));
        profileButtonPane.add(startProfileButton);
        startProfileButton.setToolTipText(rb.getString("TTStartProfile"));
        profileButtonPane.add(stopProfileButton);
        stopProfileButton.setToolTipText(rb.getString("TTStopProfile"));
        profileButtonPane.add(exportProfileButton);
        exportProfileButton.setToolTipText(rb.getString("TTSaveProfile"));
        profileButtonPane.add(printProfileButton);
        printProfileButton.setToolTipText(rb.getString("TTPrintProfile"));
        
        // Listen to track Power button
        trackPowerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                trackPower();
            }
        });

        // Listen to start button
        startProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startProfile();
            }
        });

        // Listen to stop button
        stopProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopProfile();
            }
        });

        // Listen to export button
        exportProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (dirFwdButton.isSelected() && dirRevButton.isSelected()) {
                    DccSpeedProfile[] sp = {spFwd, spRev};
                    DccSpeedProfile.export(sp, profileAddress, profileGraphPane.getUnits());
                } else if (dirFwdButton.isSelected()) {
                    DccSpeedProfile.export(spFwd, profileAddress, "fwd", profileGraphPane.getUnits());
                }else if (dirRevButton.isSelected()) {
                    DccSpeedProfile.export(spRev, profileAddress, "rev", profileGraphPane.getUnits());
                }
            }
        });

        // Listen to print button
        printProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Date today;
                String result;
                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
                today = new Date();
                result = formatter.format(today);
                String annotate = "Bachrus MTS-DCC "+rb.getString("ProfileFor")+" "
                                    +profileAddress+" "+rb.getString("CreatedOn")
                                    +" " + result;
                profileGraphPane.printProfile(annotate);
            }
        });

        profilePane.add(profileButtonPane, BorderLayout.SOUTH);

        // Pane to hold controls
        JPanel profileControlPane = new JPanel();
        profileControlPane.setLayout(new BoxLayout(profileControlPane, BoxLayout.Y_AXIS));
        dirFwdButton.setSelected(true);
        dirFwdButton.setToolTipText(rb.getString("TTMeasFwd"));
        dirRevButton.setToolTipText(rb.getString("TTMeasRev"));
        dirFwdButton.setForeground(Color.RED);
        dirRevButton.setForeground(Color.BLUE);
        profileControlPane.add(dirFwdButton);
        profileControlPane.add(dirRevButton);
        
        profilePane.add(profileControlPane, BorderLayout.EAST);
        
        /*
         * Create the tabbed pane and add the panes
         */
        JTabbedPane tabbedPane = new JTabbedPane();
        // make basic panel
        tabbedPane.addTab(rb.getString("Setup"), null, basicPane, "Basic Speedo Operation");

        if (((dccServices & THROTTLE) == THROTTLE)
            || ((dccServices & COMMAND) == COMMAND)) {
            tabbedPane.addTab(rb.getString("Profile"), null, profilePane, "Profile Loco");
        }

        // connect to TrafficController
        tc = SpeedoTrafficController.instance();
        tc.addSpeedoListener(this);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);

        // Create a wrapper with a status line and add the main contnnt
        JPanel statusWrapper = new JPanel();
        statusWrapper.setLayout(new BorderLayout());
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusWrapper.add(tabbedPane, BorderLayout.CENTER);
        statusWrapper.add(statusPanel, BorderLayout.SOUTH);

        getContentPane().add(statusWrapper);
        
        // pack for display
        pack();
    }
    
	public void propertyChange(PropertyChangeEvent evt) {
		setPowerStatus();
	}

    private void setPowerStatus() {
    	if (pm==null) return;
        try {
            if (pm.getPower()==PowerManager.ON) {
                trackPowerButton.setText("Power Down");
                statusLabel.setText(rb.getString("StatTOn"));
            }
            else if (pm.getPower()==PowerManager.OFF) {
                trackPowerButton.setText("Power Up");
                statusLabel.setText(rb.getString("StatTOff"));
            }
        } catch (JmriException ex) {
        }
    }

    /**
     * Handle "replies" from the hardware. In fact, all the hardware does
     * is send a constant stream of unsolicited speed updates.
     * @param l
     */
    public synchronized void reply(SpeedoReply l) {  // receive a reply message and log it
        //log.debug("Speedo reply " + l.toString());
        count = l.getCount();
        series = l.getSeries();
        if (count > 0) {
            switch (series) {
                case 4:
                    circ = 12.5664F;
                    readerLabel.setText(rb.getString("Reader40"));
                    break;
                case 5:
                    circ = 18.8496F;
                    readerLabel.setText(rb.getString("Reader50"));
                    break;
                case 6:
                    circ = 50.2655F;
                    readerLabel.setText(rb.getString("Reader60"));
                    break;
                default:
                    speedTextField.setText(rb.getString("ReaderErr"));
                    log.error("Invalid reader type");
                    break;
            }

            // Update speed
            calcSpeed();
        }
        if (timerRunning == false) {
            // first reply starts the timer
            startReplyTimer();
            startDisplayTimer();
            startFastDisplayTimer();
            timerRunning = true;
        } else {
            // subsequnet replies restart it
            replyTimer.restart();
        }
    }

    /*
     * Calculate the scale speed in KPH
     */
    protected void calcSpeed() {
        if (series > 0) {
            // Scale the data and calculate kph
            try {
                freq = 1500000/count;
                sampleSpeed = (freq/24)*circ*selectedScale*3600/1000000;
            } catch (ArithmeticException ae) {
                log.error("Exception calculating sampleSpeed " + ae);
            }
            avFn(sampleSpeed);
            log.debug("New sample: "+sampleSpeed+" Average: "+avSpeed);
            log.debug("Acc: "+acc+" range: "+range);
            switchRange();
        }
    }

    // Averaging function used for speed is
    // S(t) = S(t-1) - [S(t-1)/N] + speed
    // A(t) = S(t)/N
    //
    // where S is an accumulator, N is the length of the filter (i.e.,
    // the number of samples included in the rolling average), and A is
    // the result of the averaging function.
    //
    // Re-arranged
    // S(t) = S(t-1) - A(t-1) + speed
    // A(t) = S(t)/N
    protected void avFn(float speed) {
        acc = acc - avSpeed + speed;
        avSpeed = acc/filterLength[range];
    }

    // Clear out the filter
    protected void avClr() {
        acc = 0;
        avSpeed = 0;
    }
    
    // When we switch range we must compensate the current accumulator
    // value for the longer filter.
    protected void switchRange() {
        switch (range) {
            case 1:
                if (sampleSpeed > RANGE1HI) {
                    range++;
                    acc = acc*filterLength[2]/filterLength[1];
                }
                break;
            case 2:
                if (sampleSpeed < RANGE2LO){
                    range--;
                    acc = acc*filterLength[1]/filterLength[2];
                }
                else if (sampleSpeed > RANGE2HI) {
                    range++;
                    acc = acc*filterLength[3]/filterLength[2];
                }
                break;
            case 3:
                if (sampleSpeed < RANGE3LO){
                    range--;
                    acc = acc*filterLength[2]/filterLength[3];
                }
                else if (sampleSpeed > RANGE3HI) {
                    range++;
                    acc = acc*filterLength[4]/filterLength[3];
                }
                break;
            case 4:
                if (sampleSpeed < RANGE4LO) {
                    range--;
                    acc = acc*filterLength[3]/filterLength[4];
                }
                break;
        }
    }

    /*
     * Display the speed
     */
    protected void showSpeed() {
        float speedForText = currentSpeed;
        if (mphButton.isSelected()) {
            speedForText = Speed.kphToMph(speedForText);
        }
        if (series > 0) {
            if ((currentSpeed < 0) || (currentSpeed > 999)) {
                log.error("Calculated speed out of range: " + currentSpeed);
                speedTextField.setText("999");
            } else {
                // Final smoothing as applied by Bachrus Console. Don't update display
                // unless speed has changed more than 2%
                if ((currentSpeed > oldSpeed*1.02) || (currentSpeed < oldSpeed*0.98)) {
                    speedTextField.setText(MessageFormat.format("{0,number,##0.0}", speedForText));
                    speedTextField.setHorizontalAlignment(JTextField.RIGHT);
                    oldSpeed = currentSpeed;
                    speedoDialDisplay.update(currentSpeed);
                }
            }
        }
    }

    // Allows user to power up and give time for sound decoder
    // startup sequence before running a profile
    protected void trackPower() {
        try {
            if (pm.getPower() != PowerManager.ON) {
                pm.setPower(PowerManager.ON);
            } else {
                stopProfile();
                pm.setPower(PowerManager.OFF);
            }
        } catch (JmriException e) {
            log.error("Exception during power on: "+e.toString());
        }
    }
    
    protected synchronized void startProfile() {
        if (profileAddress > 0) {
            if (dirFwdButton.isSelected() || dirRevButton.isSelected()) {
                if (state == ProfileState.IDLE) {
                    profileTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            profileTimeout();
                        }
                    });
                    profileTimer.setRepeats(false);
                    // Request a throttle
                    state = ProfileState.WAIT_FOR_THROTTLE;
                    // Request a throttle
                    statusLabel.setText(rb.getString("StatReqThrottle"));
                    spFwd.clear();
                    spRev.clear();
                    if (dirFwdButton.isSelected()) {
                        profileDir = ProfileDirection.FORWARD;
                    } else {
                        profileDir = ProfileDirection.REVERSE;
                    }
                    profileGraphPane.repaint();
                    profileTimer.start();
                    log.info("Requesting throttle");
                    jmri.InstanceManager.throttleManagerInstance().requestThrottle(profileAddress, this);
                }
            }
        } else {
            // Must have a non-zero address
            profileAddressField.setBackground(Color.RED);
            log.error("Attempt to profile loco address 0");
        }
    }

    protected synchronized void stopProfile() {
        if (state != ProfileState.IDLE) {
            tidyUp();
            state = ProfileState.IDLE;
            log.info("Profiling stopped by user");
        }
    }
    
    public synchronized void notifyThrottleFound(DccThrottle t) {
        profileTimer.stop();
        throttle = t;
        log.info("Throttle aquired, starting profiling");
        throttle.setSpeedStepMode(DccThrottle.SpeedStepMode28);
        if (throttle.getSpeedStepMode() != DccThrottle.SpeedStepMode28) {
            log.error("Failed to set 28 step mode");
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            //throttle.release();
            return;
        }
        // turn on power
        try {
            pm.setPower(PowerManager.ON);
        } catch (JmriException e) {
            log.error("Exception during power on: "+e.toString());
        }
        state = ProfileState.RUNNING;
        // Start at step 0 with 28 step packets
        profileSpeed = 0.0F;
        profileStep = 0;
        profileIncrement = throttle.getSpeedIncrement();
        throttle.setSpeedSetting(profileSpeed);
        if (profileDir == ProfileDirection.FORWARD) {
            throttle.setIsForward(true);
            statusLabel.setText(rb.getString("StatCreateFwd"));
        } else {
            throttle.setIsForward(false);
            statusLabel.setText(rb.getString("StatCreateRev"));
        }
        // using profile timer to trigger each next step
        profileTimer.setRepeats(true);
        profileTimer.start();
    }

    public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason){
    }


    javax.swing.Timer replyTimer = null;
    javax.swing.Timer displayTimer = null;
    javax.swing.Timer fastDisplayTimer = null;
    javax.swing.Timer profileTimer = null;

	// Once we receive a speedoReply we expect them regularly, at
    // least once every 4 seconds
    protected void startReplyTimer() {
        replyTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                replyTimeout();
            }
        });
        replyTimer.setRepeats(true);     // refresh until stopped by dispose
        replyTimer.start();
    }

    /**
     * Internal routine to reset the speed on a timeout
     */
    synchronized protected void replyTimeout() {
        //log.debug("Timed out - display speed zero");
        targetSpeed = 0;
        avClr();
        oldSpeed = 0;
        showSpeed();
    }

	// A timer is used to update the target display speed
    protected void startDisplayTimer() {
        displayTimer = new javax.swing.Timer(DISPLAY_UPDATE, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayTimeout();
            }
        });
        displayTimer.setRepeats(true);     // refresh until stopped by dispose
        displayTimer.start();
    }

	// A timer is used to update the display at faster rate
    protected void startFastDisplayTimer() {
        fastDisplayTimer = new javax.swing.Timer(DISPLAY_UPDATE/FAST_DISPLAY_RATIO, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fastDisplayTimeout();
            }
        });
        fastDisplayTimer.setRepeats(true);     // refresh until stopped by dispose
        fastDisplayTimer.start();
    }

    /**
     * Internal routine to update the target speed for display
     */
    synchronized protected void displayTimeout() {
        //log.info("Display timeout");
        targetSpeed = avSpeed;
        incSpeed = (targetSpeed - currentSpeed)/FAST_DISPLAY_RATIO;
    }

    /**
     * Internal routine to update the displayed speed
     */
    synchronized protected void fastDisplayTimeout() {
        //log.info("Display timeout");
        if (Math.abs(targetSpeed - currentSpeed) < Math.abs(incSpeed)) {
            currentSpeed = targetSpeed;
        } else {

            currentSpeed += incSpeed;
        }
        if (currentSpeed < 0.01F) {
            currentSpeed = 0.0F;
        }
        showSpeed();
    }

    /**
     * timeout requesting a throttle
     */
    synchronized protected void throttleTimeout() {
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, this);
        state = ProfileState.IDLE;
        log.error("Timeout waiting for throttle");

    }

    /**
     * Time to change to next speed increment
     */
    protected synchronized void profileTimeout() {
        if (state == ProfileState.WAIT_FOR_THROTTLE) {
            tidyUp();
            log.error("Timeout waiting for throttle");
        } else if (state == ProfileState.RUNNING) {
            if (profileDir == ProfileDirection.FORWARD) {
                spFwd.setPoint(profileStep, avSpeed);
                statusLabel.setText((rb.getString("Fwd")+" step: "+profileStep));
            } else {
                spRev.setPoint(profileStep, avSpeed);
                statusLabel.setText((rb.getString("Rev")+" step: "+profileStep));
            }
            profileGraphPane.repaint();
            if (profileStep == 29) {
                if ((profileDir == ProfileDirection.FORWARD)
                        && dirRevButton.isSelected()) {
                    // Start reverse profile
                    profileDir = ProfileDirection.REVERSE;
                    throttle.setIsForward(false);
                    profileStep = 0;
                    avClr();
                    statusLabel.setText(rb.getString("StatCreateRev"));
                } else {
                    tidyUp();
                    statusLabel.setText(rb.getString("StatDone"));
                }
            } else {
                if (profileStep == 28) {
                    profileSpeed = 0.0F;
                } else {
                    profileSpeed += profileIncrement;
                }
                throttle.setSpeedSetting(profileSpeed);
                profileStep += 1;
                // adjust delay as we get faster and averaging is quicker
                profileTimer.setDelay(7000 - range*1000);
                //profileTimer.setDelay(20000);
                //log.info("Step " + profileStep + " Set speed: "+profileSpeed);
            }
        } else {
            log.error("Unexpected profile timeout");
            profileTimer.stop();
        }
    }

    protected void tidyUp() {
        profileTimer.stop();
        // turn off power
        try {
            pm.setPower(PowerManager.OFF);
        } catch (JmriException e) {
            log.error("Exception during power off: "+e.toString());
        }
        if (throttle != null) {
            throttle.setSpeedSetting(0.0F);
            //jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, this);
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            //throttle.release();
            throttle = null;
        }
        state = ProfileState.IDLE;
    }
    
    protected void readAddress() {
        readState = ProgState.WAIT29;
        statusLabel.setText(rb.getString("ProgRd29"));
        startRead(29);
    }

    protected void startRead(int cv) {
        try {
            prog.readCV(cv, this);
        } catch (ProgrammerException e) {
            log.error("Exception reading CV " + cv + " " + e);
        }
    }

    public void programmingOpReply(int value, int status) {
        if (status == 0) {
            switch(readState) {
                case IDLE:
                    log.debug("unexpected reply in IDLE state");
                    break;

                case WAIT29:
                    // Check extended address bit
                    if ((value & 0x20) == 0) {
                        readState = ProgState.WAIT1;
                        statusLabel.setText(rb.getString("ProgRdShort"));
                        startRead(1);
                    } else {
                        readState = ProgState.WAIT17;
                        statusLabel.setText(rb.getString("ProgRdExtended"));
                        startRead(17);
                    }
                    break;

                case WAIT1:
                    profileAddress = value;
                    profileAddressField.setText(Integer.toString(profileAddress));
                    profileAddressField.setBackground(Color.WHITE);
                    readState = ProgState.IDLE;
                    break;

                case WAIT17:
                    profileAddress = value;
                    readState = ProgState.WAIT18;
                    startRead(18);
                    break;

                case WAIT18:
                    profileAddress = (profileAddress&0x3f)*256 + value;
                    profileAddressField.setText(Integer.toString(profileAddress));
                    profileAddressField.setBackground(Color.WHITE);
                    statusLabel.setText(rb.getString("ProgRdComplete"));
                    readState = ProgState.IDLE;
                    break;

            }
        } else {
            // Error during programming
            log.error("Status not OK during read: " + status);
            profileAddressField.setText("Error");
            statusLabel.setText(rb.getString("ProgRdError"));
            readState = ProgState.IDLE;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeedoConsoleFrame.class.getName());
    
}
