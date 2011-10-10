// SignalGroupSubTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.SignalGroup;
import jmri.Turnout;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a SignalGroup Table
 
 * Based in part on RouteTableAction.java by Bob Jacobsen
 *
 * @author	Kevin Dickerson    Copyright (C) 2010
 *
 * @version     $Revision$
 */

public class SignalGroupSubTableAction {

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
     //BeanTableFrame f;
    public SignalGroupSubTableAction(String s) {

    }
    public SignalGroupSubTableAction() { this("SignalGroup Head Edit Table");}

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalGroupTable";
    }
    
    void setoperBox(boolean mode, JComboBox box) {
        int _mode = 0;
        if (mode) _mode = 1;
        String result = jmri.util.StringUtil.getNameFromState(_mode, operValues, oper);
        box.setSelectedItem(result);
    }
    
    boolean operFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, operValues, oper);
        
        if (result<0) {
            log.warn("unexpected mode string in sensorMode: "+mode);
            throw new IllegalArgumentException();
        }
        if (result == 0) return false;
        else return true;
    }
    
    private static String[] oper = new String[]{"AND", "OR"};
    private static int[] operValues = new int[]{0x00, 0x01};

    int sensorModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in signal Appearance: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setSensorModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }
    
    int signalStateFromBox(JComboBox box) {
        SignalHead sig = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(curSignal);
        int result;
        String mode;
        if (sig!=null){
            mode = (String)box.getSelectedItem();
            result = jmri.util.StringUtil.getStateFromName(mode, sig.getValidStates(), sig.getValidStateNames());
        }
        else {
            mode = (String)box.getSelectedItem();
            result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);
        }
        
        if (result<0) {
            log.warn("unexpected mode string in signalMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
    
    void setSignalStateBox(int mode, JComboBox box) {
        SignalHead sig = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(curSignal);
        String result = jmri.util.StringUtil.getNameFromState(mode, sig.getValidStates(), sig.getValidStateNames());
        box.setSelectedItem(result);
    }
    
    

    int turnoutModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in turnoutMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setTurnoutModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }
       
    JLabel _systemName;
    JComboBox _OnAppearance;
    JComboBox _OffAppearance;
    JComboBox _SensorTurnoutOper = new JComboBox(oper);

    JmriJFrame addFrame = null;
    SignalGroupTurnoutModel _SignalGroupTurnoutModel;
    JScrollPane _SignalGroupTurnoutScrollPane;
    SignalGroupSensorModel _SignalGroupSensorModel;
    JScrollPane _SignalGroupSensorScrollPane;
    
    ButtonGroup selGroup = null;
    JRadioButton allButton = null;   
    JRadioButton includedButton = null; 
      
    JLabel nameLabel = new JLabel("Signal Head:");
    JLabel signalOnStateLabel = new JLabel("State when conditions are met");
    JLabel signalOffStateLabel = new JLabel("State when conditions are not met");
    JLabel userLabel = new JLabel("Select the conditions that must be met to set this Signal Head on");
    
    JButton updateSubButton = new JButton("Done");
    
    static String updateInst = "To change this SignalGroup, make changes above, then click 'Done'.";

    JLabel status1 = new JLabel(updateInst);
    
    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    SignalGroup curSignalGroup = null;
    String curSignal;
    SignalHead curSignalHead;

    void editSignal(SignalGroup g, String signal) {
        curSignalGroup = g;
        curSignal = signal;
        curSignalHead = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(curSignal);
        //SignalHead sig = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(curSignal);
        
        _OnAppearance = new JComboBox(curSignalHead.getValidStateNames());
        _OffAppearance = new JComboBox(curSignalHead.getValidStateNames());
        _systemName= new JLabel(signal);
        _systemName.setVisible(true);
        
        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        List<String> systemNameList = tm.getSystemNameList();
        _turnoutList = new ArrayList <SignalGroupTurnout> (systemNameList.size());
        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = tm.getBySystemName(systemName).getUserName();
            _turnoutList.add(new SignalGroupTurnout(systemName, userName));
        }
        
        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        systemNameList = sm.getSystemNameList();
        _sensorList = new ArrayList <SignalGroupSensor> (systemNameList.size());
        iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            String userName = sm.getBySystemName(systemName).getUserName();
            _sensorList.add(new SignalGroupSensor(systemName, userName));
        }
        initializeIncludedList();
        
        // Set up window
        if (addFrame==null) {
            addFrame = new JmriJFrame("Add/Edit SignalGroup", false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalGroupAddEdit", true);
            addFrame.setLocation(100,30);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addFrame.getContentPane();        
            // add system name
            JPanel ps = new JPanel(); 
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            contentPane.add(ps);
            // add user name
            JPanel pc = new JPanel();
            pc.setLayout(new FlowLayout());
            pc.add(signalOnStateLabel);
            pc.add(_OnAppearance);
            pc.add(signalOffStateLabel);
            pc.add(_OffAppearance);
            contentPane.add(pc);
            
            JPanel p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.add(userLabel);

            contentPane.add(p);
            if (InstanceManager.signalHeadManagerInstance().getSignalHead(curSignal).getClass().getName().contains("SingleTurnoutSignalHead")){
                jmri.implementation.SingleTurnoutSignalHead Signal = (jmri.implementation.SingleTurnoutSignalHead) InstanceManager.signalHeadManagerInstance().getBySystemName(curSignal);
                if ((g.getSignalHeadOnState(curSignal)==0x00) && (g.getSignalHeadOffState(curSignal)==0x00)){
                    g.setSignalHeadOnState(curSignal, Signal.getOnAppearance());
                    g.setSignalHeadOffState(curSignal, Signal.getOffAppearance());
                }
            }
            
            setSignalStateBox(g.getSignalHeadOnState(curSignal), _OnAppearance);
            setSignalStateBox(g.getSignalHeadOffState(curSignal), _OffAppearance);
            // add Turnout Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel("Show "));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton("All",true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of all Turnouts, if needed
                        if (!showAll) {
                            showAll = true;
                            _SignalGroupTurnoutModel.fireTableDataChanged();
                            _SignalGroupSensorModel.fireTableDataChanged();
                        }
                    }
                });
            includedButton = new JRadioButton("Included",false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of included Turnouts only, if needed
                        if (showAll) {
                            showAll = false;
                            initializeIncludedList();
                            _SignalGroupTurnoutModel.fireTableDataChanged();
                            _SignalGroupSensorModel.fireTableDataChanged();
                        }
                    }
                });
            py.add(new JLabel("  Turnouts and Sensors"));
            contentPane.add(py);

            // add turnout table
            p2xt = new JPanel();
            JPanel p2xtSpace = new JPanel();
            p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
            p2xtSpace.add(new JLabel("XXX"));
            p2xt.add(p2xtSpace);
            
            JPanel p21t = new JPanel();
            p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
            p21t.add(new JLabel("Please select "));
            p21t.add(new JLabel("Sensors to be "));
            p21t.add(new JLabel("included in  "));
            p21t.add(new JLabel("this Signal Group."));
            p2xt.add(p21t);
            _SignalGroupTurnoutModel = new SignalGroupTurnoutModel();
            JTable SignalGroupTurnoutTable = jmri.util.JTableUtil.sortableDataModel(_SignalGroupTurnoutModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)SignalGroupTurnoutTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(SignalGroupTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            SignalGroupTurnoutTable.setRowSelectionAllowed(false);
            SignalGroupTurnoutTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(480,80));

            ROW_HEIGHT = SignalGroupTurnoutTable.getRowHeight();
            JComboBox stateTCombo = new JComboBox();
   			stateTCombo.addItem(SET_TO_CLOSED);
			stateTCombo.addItem(SET_TO_THROWN);
            TableColumnModel SignalGroupTurnoutColumnModel = SignalGroupTurnoutTable.getColumnModel();
            TableColumn includeColumnT = SignalGroupTurnoutColumnModel.
                                                getColumn(SignalGroupTurnoutModel.INCLUDE_COLUMN);
            includeColumnT.setResizable(false);
            includeColumnT.setMinWidth(50);
            includeColumnT.setMaxWidth(60);
            TableColumn sNameColumnT = SignalGroupTurnoutColumnModel.
                                                getColumn(SignalGroupTurnoutModel.SNAME_COLUMN);
            sNameColumnT.setResizable(true);
            sNameColumnT.setMinWidth(75);
            sNameColumnT.setMaxWidth(95);
            TableColumn uNameColumnT = SignalGroupTurnoutColumnModel.
                                                getColumn(SignalGroupTurnoutModel.UNAME_COLUMN);
            uNameColumnT.setResizable(true);
            uNameColumnT.setMinWidth(210);
            uNameColumnT.setMaxWidth(260);
            TableColumn stateColumnT = SignalGroupTurnoutColumnModel.
                                                getColumn(SignalGroupTurnoutModel.STATE_COLUMN);
            stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
            stateColumnT.setResizable(false);
            stateColumnT.setMinWidth(90);
            stateColumnT.setMaxWidth(100);
            _SignalGroupTurnoutScrollPane = new JScrollPane(SignalGroupTurnoutTable);
            p2xt.add(_SignalGroupTurnoutScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xt);
            p2xt.setVisible(true);
            
            JPanel po = new JPanel(); 
            po.setLayout(new FlowLayout());
            JLabel operLabel = new JLabel("Do either the Sensors OR Turnouts have to match, or do they both have to Match");
            po.add(operLabel);
            po.add( _SensorTurnoutOper);
            contentPane.add(po);
             // add sensor table
            p2xs = new JPanel();
            JPanel p2xsSpace = new JPanel();
            p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
            p2xsSpace.add(new JLabel("XXX"));
            p2xs.add(p2xsSpace);
            
            JPanel p21s = new JPanel();
            p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
            p21s.add(new JLabel("Please select "));
            p21s.add(new JLabel(" Sensors to "));
            p21s.add(new JLabel(" be included "));
            p21s.add(new JLabel(" in this SignalGroup."));
            p2xs.add(p21s);
            _SignalGroupSensorModel = new SignalGroupSensorModel();
            JTable SignalGroupSensorTable = jmri.util.JTableUtil.sortableDataModel(_SignalGroupSensorModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)SignalGroupSensorTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(SignalGroupSensorModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            SignalGroupSensorTable.setRowSelectionAllowed(false);
            SignalGroupSensorTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480,80));
            JComboBox stateSCombo = new JComboBox();
   			stateSCombo.addItem(SET_TO_ACTIVE);
			stateSCombo.addItem(SET_TO_INACTIVE);
            TableColumnModel SignalGroupSensorColumnModel = SignalGroupSensorTable.getColumnModel();
            TableColumn includeColumnS = SignalGroupSensorColumnModel.
                                                getColumn(SignalGroupSensorModel.INCLUDE_COLUMN);
            includeColumnS.setResizable(false);
            includeColumnS.setMinWidth(50);
            includeColumnS.setMaxWidth(60);
            TableColumn sNameColumnS = SignalGroupSensorColumnModel.
                                                getColumn(SignalGroupSensorModel.SNAME_COLUMN);
            sNameColumnS.setResizable(true);
            sNameColumnS.setMinWidth(75);
            sNameColumnS.setMaxWidth(95);
            TableColumn uNameColumnS = SignalGroupSensorColumnModel.
                                                getColumn(SignalGroupSensorModel.UNAME_COLUMN);
            uNameColumnS.setResizable(true);
            uNameColumnS.setMinWidth(210);
            uNameColumnS.setMaxWidth(260);
            TableColumn stateColumnS = SignalGroupSensorColumnModel.
                                                getColumn(SignalGroupSensorModel.STATE_COLUMN);
            stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
            stateColumnS.setResizable(false);
            stateColumnS.setMinWidth(90);
            stateColumnS.setMaxWidth(100);
            _SignalGroupSensorScrollPane = new JScrollPane(SignalGroupSensorTable);
            p2xs.add(_SignalGroupSensorScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xs);
            p2xs.setVisible(true);
           
             
            // add lock control table
            /*JPanel p4 = new JPanel();
            p4.setLayout(new BoxLayout(p4, BoxLayout.Y_AXIS));
            
			// complete this panel
            Border p4Border = BorderFactory.createEtchedBorder();
            p4.setBorder(p4Border);
            contentPane.add(p4);*/
            
            // add notes panel
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(status1);
            pa.add(p1);
            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);
            // add buttons - Add SignalGroup button
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout());

            // Update SignalGroup button
            pb.add(updateSubButton);
            updateSubButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateSubPressed(e, false);
                }
            });
            updateSubButton.setToolTipText("Change this SignalGroup and leave Edit mode");

            p2xtSpace.setVisible(false);
            p2xsSpace.setVisible(false);
            updateSubButton.setVisible(true);
            contentPane.add(pb);
            addFrame.pack();
            
        }
        // set listener for window closing
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    addFrame.setVisible(false);
                    cancelEdit();
                    _SignalGroupSensorModel.dispose();
                    _SignalGroupTurnoutModel.dispose();
                }
            });
        addFrame.setVisible(true);
        setoperBox(curSignalGroup.getSensorTurnoutOper(curSignal), _SensorTurnoutOper);
        setSignalStateBox(curSignalGroup.getSignalHeadOnState(curSignal), _OnAppearance);
        setSignalStateBox(curSignalGroup.getSignalHeadOffState(curSignal), _OffAppearance);
        int setRow = 0;
        for (int i=_turnoutList.size()-1; i>=0; i--) {
            SignalGroupTurnout turnout = _turnoutList.get(i);
            String tSysName = turnout.getName();
            if (curSignalGroup.isTurnoutIncluded(curSignal, tSysName) ) {
                turnout.setIncluded(true);
                turnout.setState(curSignalGroup.getTurnoutState(curSignal, tSysName));
                setRow = i;
            } else {
                turnout.setIncluded(false);
                turnout.setState(Turnout.CLOSED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _SignalGroupTurnoutScrollPane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        _SignalGroupTurnoutModel.fireTableDataChanged();      

        for (int i=_sensorList.size()-1; i>=0; i--) {
            SignalGroupSensor sensor = _sensorList.get(i);
            String tSysName = sensor.getName();
            if (curSignalGroup.isSensorIncluded(curSignal, tSysName) ) {
                sensor.setIncluded(true);
                sensor.setState(curSignalGroup.getSensorState(curSignal, tSysName));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }
    
        status1.setText(updateInst);
        updateSubButton.setVisible(true);
}

    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        //TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
		table.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
		table.setDefaultEditor(JButton.class,buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
			.setPreferredWidth((sample.getPreferredSize().width)+4);
    }
    
    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        _includedTurnoutList = new ArrayList <SignalGroupTurnout> ();
        for (int i=0; i<_turnoutList.size(); i++) {
            if (_turnoutList.get(i).isIncluded( )) {
                _includedTurnoutList.add(_turnoutList.get(i));
            }
        }
        _includedSensorList = new ArrayList <SignalGroupSensor> ();
        for (int i=0; i<_sensorList.size(); i++) {
            if (_sensorList.get(i).isIncluded()) {
                _includedSensorList.add(_sensorList.get(i));
            }
        }
    }

    /**
     * Sets the Turnout information for adding or editting
     */
    int setTurnoutInformation(SignalGroup g) {
        for (int i=0; i<_includedTurnoutList.size(); i++) {
            SignalGroupTurnout t = _includedTurnoutList.get(i);
            g.setSignalHeadAlignTurnout(curSignalHead, t.getName(), t.getState());
        }
        return _includedTurnoutList.size();
    }

    /**
     * Sets the Sensor information for adding or editting
     */
    int setSensorInformation(SignalGroup g) {
        for (int i=0; i<_includedSensorList.size(); i++) {
            SignalGroupSensor s = _includedSensorList.get(i);
            g.setSignalHeadAlignSensor(curSignalHead, s.getName(), s.getState());
        }
        return _includedSensorList.size();
    }

    /**
     * Responds to the Update button - update to SignalGroup Table
     */
    void updateSubPressed(ActionEvent e, boolean newSignalGroup ) {
        curSignalGroup.clearSignalTurnout(curSignal);
        curSignalGroup.clearSignalSensor(curSignal);

        initializeIncludedList();
        setTurnoutInformation(curSignalGroup);
        setSensorInformation(curSignalGroup);
        curSignalGroup.setSignalHeadOnState(curSignalHead, signalStateFromBox(_OnAppearance));
        curSignalGroup.setSignalHeadOffState(curSignalHead, signalStateFromBox(_OffAppearance));
        curSignalGroup.setSensorTurnoutOper(curSignal, operFromBox(_SensorTurnoutOper));
        // add control Sensors and a control Turnout if entered in the window
        finishUpdate();
    }

    void finishUpdate() {
        // move to show all turnouts if not there
        cancelIncludedOnly();
        updateSubButton.setVisible(false);

        addFrame.setVisible(false);
    }
    
    /** 
     * Cancels edit mode
     */
    void cancelEdit() {
        // get out of edit mode
        curSignalGroup = null;
        finishUpdate();
    }
    
    /** 
     * Cancels included Turnouts only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Base table model for selecting outputs
     */
    public abstract class SignalGroupOutputModel extends AbstractTableModel implements PropertyChangeListener
    {
        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }
        
        public void dispose() {
            InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }

        public String getColumnName(int c) {return COLUMN_NAMES[c];}

        public int getColumnCount () {return 4;}

        public boolean isCellEditable(int r,int c) {
            return ( (c==INCLUDE_COLUMN) || (c==STATE_COLUMN) );
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
        
        public String getDisplayName(int r){
            if (((String) getValueAt(r, UNAME_COLUMN)!=null)||(!((String) getValueAt(r, UNAME_COLUMN)).equals(""))){
                return (String) getValueAt(r, UNAME_COLUMN);
            } else {
                return (String) getValueAt(r, SNAME_COLUMN);
            }
        }
        
    }
    

    /**
     * Table model for selecting Turnouts and Turnout State
     */
    class SignalGroupTurnoutModel extends SignalGroupOutputModel
    {
        SignalGroupTurnoutModel() {
            InstanceManager.turnoutManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount () {
            if (showAll)
                return _turnoutList.size();
            else
                return _includedTurnoutList.size();
        }
        public Object getValueAt (int r,int c) {
            ArrayList <SignalGroupTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            }
            else {
                turnoutList = _includedTurnoutList;
            }
            // some error checking
            if (r >= turnoutList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(turnoutList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return turnoutList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return turnoutList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return turnoutList.get(r).getSetToState();
                default:
                    return null;
            }
        }
        public void setValueAt(Object type,int r,int c) {
            ArrayList <SignalGroupTurnout> turnoutList = null;
            if (showAll) {
                turnoutList = _turnoutList;
            }
            else {
                turnoutList = _includedTurnoutList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    turnoutList.get(r).setIncluded(((Boolean)type).booleanValue());  
                    break;
                case STATE_COLUMN: 
                    turnoutList.get(r).setSetToState((String)type);
                    break;
            }
        }
    }

    /**
     * Set up table for selecting Sensors and Sensor State
     */
    class SignalGroupSensorModel extends SignalGroupOutputModel
    {
        SignalGroupSensorModel() {
            InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        public int getRowCount () {
            if (showAll)
                return _sensorList.size();
            else
                return _includedSensorList.size();
        }

        public Object getValueAt (int r,int c) {
            ArrayList <SignalGroupSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            }
            else {
                sensorList = _includedSensorList;
            }
            // some error checking
            if (r >= sensorList.size()){
            	log.debug("row is greater than turnout list size");
            	return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(sensorList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return sensorList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return sensorList.get(r).getUserName();
                case STATE_COLUMN:  //
                    return sensorList.get(r).getSetToState();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {
            ArrayList <SignalGroupSensor> sensorList = null;
            if (showAll) {
                sensorList = _sensorList;
            }
            else {
                sensorList = _includedSensorList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    sensorList.get(r).setIncluded(((Boolean)type).booleanValue());  
                    break;
                case STATE_COLUMN: 
                    sensorList.get(r).setSetToState((String)type);
                    break;
            }
        }
    }

    private boolean showAll = true;   // false indicates show only included Turnouts

    private static int ROW_HEIGHT;

    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
                                            rbx.getString("ColumnLabelUserName"),
                                            rbx.getString("ColumnLabelInclude"),
                                            rbx.getString("ColumnLabelSetState")};
    private static String SET_TO_ACTIVE = rbx.getString("SensorActive");
    private static String SET_TO_INACTIVE = rbx.getString("SensorInactive");
    private static String SET_TO_CLOSED = InstanceManager.turnoutManagerInstance().getClosedText();
    private static String SET_TO_THROWN = InstanceManager.turnoutManagerInstance().getThrownText();

    private static String[] sensorInputModes = new String[]{"Active", "Inactive"};
    private static int[] sensorInputModeValues = new int[]{SignalGroup.ONACTIVE, SignalGroup.ONINACTIVE};
                                            
    private static String[] signalStates = new String[]{rbx.getString("StateSignalHeadDark"), rbx.getString("StateSignalHeadRed"), rbx.getString("StateSignalHeadYellow"), rbx.getString("StateSignalHeadGreen"), rbx.getString("StateSignalHeadLunar")};
    private static int[] signalStatesValues = new int[]{SignalHead.DARK, SignalHead.RED, SignalHead.YELLOW, SignalHead.GREEN, SignalHead.LUNAR}; 

    private static String[] turnoutInputModes = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                                            InstanceManager.turnoutManagerInstance().getThrownText() };
    private static int[] turnoutInputModeValues = new int[]{SignalGroup.ONCLOSED, SignalGroup.ONTHROWN };

    private ArrayList <SignalGroupTurnout> _turnoutList;      // array of all Turnouts
    private ArrayList <SignalGroupTurnout> _includedTurnoutList; 
    
    private ArrayList <SignalGroupSensor> _sensorList;        // array of all Sensorsy
    private ArrayList <SignalGroupSensor> _includedSensorList;
    
    private abstract class SignalGroupElement {
        String _sysName;
        String _userName;
        boolean _included;
        int _setToState;

        SignalGroupElement(String sysName, String userName) {
            _sysName = sysName;
            _userName = userName;
            _included = false;
            _setToState = Sensor.INACTIVE;
        }
        String getSysName() {
            return _sysName;
        }
        String getUserName() {
            return _userName;
        }
        boolean isIncluded() {
            return _included;
        }
        void setIncluded(boolean include) {
            _included = include;
        }
        abstract String getSetToState();
        abstract void setSetToState(String state);

        int getState() {
            return _setToState;
        }
        void setState(int state) {
            _setToState = state;
        }
        String getName(){
            if (_userName!=null){
                if (!_userName.equals(""))
                    return _userName;
            }
            return _sysName;
        }
    }

    private class SignalGroupSensor extends SignalGroupElement {
        SignalGroupSensor (String sysName, String userName) {
            super(sysName, userName);
        }
        String getSetToState() {
            switch (_setToState) {
                case Sensor.INACTIVE:
                    return SET_TO_INACTIVE;
                case Sensor.ACTIVE:
                    return SET_TO_ACTIVE;
            }
            return "";
        }
        void setSetToState(String state) {
            if (SET_TO_INACTIVE.equals(state)) {
                _setToState = Sensor.INACTIVE;
            } else if (SET_TO_ACTIVE.equals(state)) {
                _setToState = Sensor.ACTIVE;
            }
        }
    }

    private class SignalGroupTurnout extends SignalGroupElement {
        SignalGroupTurnout(String sysName, String userName) {
            super(sysName, userName);
        }
        String getSetToState() {
            switch (_setToState) {
                case Turnout.CLOSED:
                    return SET_TO_CLOSED;
                case Turnout.THROWN:
                    return SET_TO_THROWN;
            }
            return "";
        }
        void setSetToState(String state) {
            if (SET_TO_CLOSED.equals(state)) {
                _setToState = Turnout.CLOSED;
            } else if (SET_TO_THROWN.equals(state)) {
                _setToState = Turnout.THROWN;
            }
        }
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalGroupSubTableAction.class.getName());
}
/* @(#)SignalGroupSubTableAction.java */
