// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperationManager;
import jmri.TurnoutOperation;
import jmri.jmrit.turnoutoperations.TurnoutOperationFrame;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;
import jmri.NamedBeanHandle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.util.JmriJFrame;
import jmri.util.ConnectionNameFromSystemName;

/**
 * Swing action to create and register a
 * TurnoutTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2004, 2007
 * @version     $Revision$
 */

public class TurnoutTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public TurnoutTableAction(String actionName) { 
        super(actionName);
        
        // disable ourself if there is no primary turnout manager available
        if (turnManager==null) {
            setEnabled(false);
        }
        
        //This following must contain the word Global for a correct match in the abstract turnout
        defaultThrownSpeedText = ("Use Global " + turnManager.getDefaultThrownSpeed()); 
        defaultClosedSpeedText = ("Use Global " + turnManager.getDefaultClosedSpeed());
        //This following must contain the word Block for a correct match in the abstract turnout
        useBlockSpeed = "Use Block Speed";
        
        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);
        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);
        java.util.Vector<String> _speedMap = jmri.implementation.SignalSpeedMap.getMap().getValidSpeedNames();
        for(int i = 0; i<_speedMap.size(); i++){
            if (!speedListClosed.contains(_speedMap.get(i))){
                speedListClosed.add(_speedMap.get(i));
            }
            if (!speedListThrown.contains(_speedMap.get(i))){
                speedListThrown.add(_speedMap.get(i));
            }
        }
    }
    
    public TurnoutTableAction() { this("Turnout Table");}
    
    String closedText;
    String thrownText;
    String defaultThrownSpeedText;
    String defaultClosedSpeedText;
    String useBlockSpeed = "Use Block Speed";
    String bothText = "Both";
    String cabOnlyText = "Cab only";
    String pushbutText = "Pushbutton only";
    String noneText = "None";
    String[] lockOperations = {bothText, cabOnlyText, pushbutText, noneText};
    private java.util.Vector<String> speedListClosed = new java.util.Vector<String>();
    private java.util.Vector<String> speedListThrown = new java.util.Vector<String>();
    protected TurnoutManager turnManager = InstanceManager.turnoutManagerInstance();

    @Override
    public void setManager(Manager man) {
        turnManager = (TurnoutManager) man;
    }
    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Turnouts
     */
    protected void createModel() {
        // store the terminology
        closedText = turnManager.getClosedText();
        thrownText = turnManager.getThrownText();

        
        // create the data model object that drives the table;
        // note that this is a class creation, and very long
        m = new BeanTableDataModel() {
        	static public final int INVERTCOL = NUMCOLUMN;
            static public final int LOCKCOL = INVERTCOL+1;
            static public final int KNOWNCOL = LOCKCOL+1;
            
            static public final int MODECOL = KNOWNCOL+1;
            static public final int SENSOR1COL = MODECOL+1;
            static public final int SENSOR2COL = SENSOR1COL+1;
            static public final int OPSONOFFCOL = SENSOR2COL+1;
            static public final int OPSEDITCOL = OPSONOFFCOL+1;
            static public final int LOCKOPRCOL = OPSEDITCOL+1;
            static public final int LOCKDECCOL = LOCKOPRCOL+1;
            static public final int STRAIGHTCOL = LOCKDECCOL+1;
            static public final int DIVERGCOL = STRAIGHTCOL+1;
    
            // show only lock columns, no feedback columns
            static public final int xLOCKOPRCOL = LOCKCOL+1;
            static public final int xLOCKDECCOL = xLOCKOPRCOL+1;
            
            static public final int xSTRAIGHTCOL = xLOCKDECCOL+1;
            static public final int xDIVERGCOL = xSTRAIGHTCOL+1;
            
            static public final int ySTRAIGHTCOL = OPSEDITCOL+1;
            static public final int yDIVERGCOL = ySTRAIGHTCOL+1;
            
            static public final int zSTRAIGHTCOL = LOCKCOL+1;
            static public final int zDIVERGCOL = zSTRAIGHTCOL+1;
                
                
            @Override
    		public int getColumnCount(){ 
                if(showLock && showFeedback && showTurnoutSpeed)
                    return DIVERGCOL+1;
                if (showLock && showFeedback)
                        return LOCKDECCOL+1;
                if(showLock && showTurnoutSpeed)
                    return xDIVERGCOL+1;
                if(showFeedback && showTurnoutSpeed)
                    return yDIVERGCOL+1;
    		    if (showFeedback)
    		        return OPSEDITCOL+1;
    		    if (showLock)
    		    	return xLOCKDECCOL+1;
    		    if(showTurnoutSpeed)
                    return zDIVERGCOL+1;
                else
    		        return LOCKCOL+1;
     		}
    		
            @Override
    		public String getColumnName(int col) {
                if (col==INVERTCOL) return "Inverted";
                else if (col==LOCKCOL) return "Locked";
                else if (col==KNOWNCOL && showFeedback) return "Feedback";
                else if (col==MODECOL && showFeedback) return "Mode";
                else if (col==SENSOR1COL && showFeedback) return "Sensor 1";
                else if (col==SENSOR2COL && showFeedback) return "Sensor 2";
                else if (col==OPSONOFFCOL && showFeedback) return "Automate";
                else if (col==OPSEDITCOL && showFeedback) return "";
                else if ((col==LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) return "Lock Mode";
                else if ((col==LOCKDECCOL || col==xLOCKDECCOL) && showLock) return "Decoder";
                else if ((col==DIVERGCOL || col==xDIVERGCOL || col==yDIVERGCOL || col==zDIVERGCOL) && showTurnoutSpeed) return "Thrown Speed";
                else if ((col==STRAIGHTCOL|| col==xSTRAIGHTCOL || col==ySTRAIGHTCOL || col==zSTRAIGHTCOL) && showTurnoutSpeed) return "Closed Speed";
                else if (col==VALUECOL) return "Cmd";  // override default title

                else return super.getColumnName(col);
            }
            
            @Override
    		public Class<?> getColumnClass(int col) {
                if (col==INVERTCOL) return Boolean.class;
                else if (col==LOCKCOL) return Boolean.class;
                else if (col==KNOWNCOL && showFeedback) return String.class;
                else if (col==MODECOL && showFeedback) return JComboBox.class;
                else if (col==SENSOR1COL && showFeedback) return String.class;
                else if (col==SENSOR2COL && showFeedback) return String.class;
                else if (col==OPSONOFFCOL && showFeedback) return JComboBox.class;
                else if (col==OPSEDITCOL && showFeedback) return JButton.class;
                else if ((col==LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) return JComboBox.class;
                else if ((col==LOCKDECCOL || col==xLOCKDECCOL) && showLock) return JComboBox.class;
                else if ((col==DIVERGCOL || col==xDIVERGCOL || col==yDIVERGCOL || col==zDIVERGCOL) && showTurnoutSpeed) return JComboBox.class;
                else if ((col==STRAIGHTCOL|| col==xSTRAIGHTCOL || col==ySTRAIGHTCOL || col==zSTRAIGHTCOL) && showTurnoutSpeed) return JComboBox.class;
                else return super.getColumnClass(col);
            }
            
            @Override
    		public int getPreferredWidth(int col) {
                if (col==INVERTCOL) return new JTextField(6).getPreferredSize().width;
                else if (col==LOCKCOL) return new JTextField(6).getPreferredSize().width;
                else if (col==KNOWNCOL && showFeedback) return new JTextField(10).getPreferredSize().width;
                else if (col==MODECOL && showFeedback) return new JTextField(10).getPreferredSize().width;
                else if (col==SENSOR1COL && showFeedback) return new JTextField(5).getPreferredSize().width;
                else if (col==SENSOR2COL && showFeedback) return new JTextField(5).getPreferredSize().width;
                else if (col==OPSONOFFCOL && showFeedback) return new JTextField(14).getPreferredSize().width;
                else if (col==OPSEDITCOL && showFeedback) return new JTextField(7).getPreferredSize().width;
                else if ((col==LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) return new JTextField(10).getPreferredSize().width;
                else if ((col==LOCKDECCOL || col==xLOCKDECCOL) && showLock) return new JTextField(10).getPreferredSize().width;
                else if ((col==DIVERGCOL || col==xDIVERGCOL || col==yDIVERGCOL || col==zDIVERGCOL) && showTurnoutSpeed) return new JTextField(14).getPreferredSize().width;
                else if ((col==STRAIGHTCOL|| col==xSTRAIGHTCOL || col==ySTRAIGHTCOL || col==zSTRAIGHTCOL) && showTurnoutSpeed) return new JTextField(14).getPreferredSize().width;
                else return super.getPreferredWidth(col);
            }
            
            @Override
    		public boolean isCellEditable(int row, int col) {
                String name = sysNameList.get(row);
                TurnoutManager manager = turnManager;
                Turnout t = manager.getBySystemName(name);
                if (col==INVERTCOL) return t.canInvert();
                else if (col == LOCKCOL)return t.canLock(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT);
                else if (col==KNOWNCOL && showFeedback) return false;
                else if (col==MODECOL && showFeedback) return true;
                else if (col==SENSOR1COL && showFeedback) return true;
                else if (col==SENSOR2COL && showFeedback) return true;
                else if (col==OPSONOFFCOL && showFeedback) return true;
                else if (col==OPSEDITCOL && showFeedback) return t.getTurnoutOperation()!=null;
                else if ((col==LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) return true;
                else if ((col==LOCKDECCOL || col==xLOCKDECCOL) && showLock) return true;
                else if ((col==DIVERGCOL || col==xDIVERGCOL || col==yDIVERGCOL || col==zDIVERGCOL) && showTurnoutSpeed) return true;
                else if ((col==STRAIGHTCOL|| col==xSTRAIGHTCOL || col==ySTRAIGHTCOL || col==zSTRAIGHTCOL) && showTurnoutSpeed) return true;
                else return super.isCellEditable(row,col);
            }

            @Override
    		public Object getValueAt(int row, int col) {
     			// some error checking
    			if (row >= sysNameList.size()){
    				log.debug("row is greater than name list");
    				return "error";
    			}
                String name = sysNameList.get(row);
                TurnoutManager manager = turnManager;
                Turnout t = manager.getBySystemName(name);
                if (t == null){
                    log.debug("error null turnout!");
                    return "error";
                }
                if (col==INVERTCOL) {
                    boolean val = t.getInverted();
                    return Boolean.valueOf(val);
                } else if (col==LOCKCOL){
                    boolean val = t.getLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT);
                    return Boolean.valueOf(val);
                } else if (col==KNOWNCOL && showFeedback) {
                    if (t.getKnownState()==Turnout.CLOSED) return closedText;
                    if (t.getKnownState()==Turnout.THROWN) return thrownText;
                    if (t.getKnownState()==Turnout.INCONSISTENT) return "Inconsistent";
                    else return "Unknown";
                } else if (col==MODECOL && showFeedback) {
                    JComboBox c = new JComboBox(t.getValidFeedbackNames());
                    c.setSelectedItem(t.getFeedbackModeName());
                    c.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if (col==SENSOR1COL && showFeedback) {
                    NamedBeanHandle s = t.getFirstNamedSensor();
                    if (s!=null) return s.getName();
                    else return "";
                } else if (col==SENSOR2COL && showFeedback) {
                    NamedBeanHandle s = t.getSecondNamedSensor();
                    if (s!=null) return s.getName();
                    else return "";
                } else if (col==OPSONOFFCOL && showFeedback) {
                    return makeAutomationBox(t);
                } else if (col==OPSEDITCOL && showFeedback) {
                    return AbstractTableAction.rb.getString("EditTurnoutOperation");
                } else if ((col == LOCKDECCOL || col==xLOCKDECCOL) && showLock) {
                    JComboBox c = new JComboBox(t.getValidDecoderNames());
                    c.setSelectedItem (t.getDecoderName());
                    c.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if ((col == LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) {
                    JComboBox c = new JComboBox(lockOperations);
                    if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                        c.setSelectedItem (bothText);
                    } else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)){
                        c.setSelectedItem (pushbutText);
                    } else if (t.canLock(Turnout.CABLOCKOUT)){
                        c.setSelectedItem (cabOnlyText);
                    }else {
                        c.setSelectedItem (noneText);
                    }
                    c.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            comboBoxAction(e);
                        }
                    });
                    return c;
                } else if(showTurnoutSpeed){
                    if ((col==STRAIGHTCOL) ||
                        (col==zSTRAIGHTCOL &&!showLock && !showFeedback) ||
                            (col==xSTRAIGHTCOL &&showLock && !showFeedback) ||
                            (col==ySTRAIGHTCOL &&!showLock && showFeedback)){

                        String speed = t.getStraightSpeed();
                        if(!speedListClosed.contains(speed)){
                            speedListClosed.add(speed);
                        }
                        JComboBox c = new JComboBox(speedListClosed);
                        c.setEditable(true);
                        c.setSelectedItem(speed);

                        return c;
                    }
                    else if ((col==DIVERGCOL) ||
                        (col==zDIVERGCOL &&!showLock && !showFeedback) ||
                            (col==xDIVERGCOL &&showLock && !showFeedback) ||
                            (col==yDIVERGCOL &&!showLock && showFeedback)){

                        String speed = t.getDivergingSpeed();
                        if(!speedListThrown.contains(speed)){
                            speedListThrown.add(speed);
                        }
                        JComboBox c = new JComboBox(speedListThrown);
                        c.setEditable(true);
                        c.setSelectedItem(speed);
                        return c;
                    }
                }
                return super.getValueAt(row, col);
            }
            @Override
    		public void setValueAt(Object value, int row, int col) {
                    String name = sysNameList.get(row);
                    TurnoutManager manager = turnManager;
                    Turnout t = manager.getBySystemName(name);
                    if (col == INVERTCOL) {
                        if (t.canInvert()) {
                            boolean b = ((Boolean) value).booleanValue();
                            t.setInverted(b);
                        }
                    } else if (col == LOCKCOL) {
                        boolean b = ((Boolean) value).booleanValue();
                        t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT,	b);
                    } else if (col == MODECOL && showFeedback) {
                        String modeName = (String)((JComboBox)value).getSelectedItem();
                        t.setFeedbackMode(modeName);
                    } else if (col==SENSOR1COL && showFeedback) {
                        String sname = (String)value;
                        try {
                            t.provideFirstFeedbackSensor(sname);
                        } catch (jmri.JmriException e) {
                            JOptionPane.showMessageDialog(null, e.toString());
                        }
                    } else if (col==SENSOR2COL && showFeedback) {
                        String sname = (String)value;
                        try {
                            t.provideSecondFeedbackSensor(sname);
                        } catch (jmri.JmriException e) {
                            JOptionPane.showMessageDialog(null, e.toString());
                        }
                    } else if (col==OPSONOFFCOL && showFeedback) {
                        // do nothing as this is handled by the combo box listener
                    } else if (col==OPSEDITCOL && showFeedback) {
                        t.setInhibitOperation(false);
                        editTurnoutOperation(t, (JComboBox)getValueAt(row,OPSONOFFCOL));
                    } else if ((col == LOCKOPRCOL || col==xLOCKOPRCOL) && showLock) {
                        String lockOpName = (String) ((JComboBox) value)
                            .getSelectedItem();
                        if (lockOpName.equals(bothText)){
                            t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                        if (lockOpName.equals(cabOnlyText)) {
                            t.enableLockOperation(Turnout.CABLOCKOUT, true);
                            t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
                        }
                        if (lockOpName.equals(pushbutText)) {
                            t.enableLockOperation(Turnout.CABLOCKOUT, false);
                            t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                    } else if ((col == LOCKDECCOL || col==xLOCKDECCOL) && showLock) {
                        String decoderName = (String)((JComboBox)value).getSelectedItem();
                        t.setDecoderName(decoderName);
                    } else if(showTurnoutSpeed){
                        if ((col==STRAIGHTCOL) || 
                            (col==zSTRAIGHTCOL &&!showLock && !showFeedback) ||
                                (col==xSTRAIGHTCOL &&showLock && !showFeedback) || 
                                (col==ySTRAIGHTCOL &&!showLock && showFeedback)){
                                
                            String speed = (String)((JComboBox)value).getSelectedItem();
                            try {
                                t.setStraightSpeed(speed);
                            } catch (jmri.JmriException ex) {
                                JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                                return;
                            }
                            if ((!speedListClosed.contains(speed)) && !speed.contains("Global")){
                                speedListClosed.add(speed);
                            }
                            fireTableRowsUpdated(row,row);
                        }
                        else if ((col==DIVERGCOL) || 
                            (col==zDIVERGCOL &&!showLock && !showFeedback) ||
                                (col==xDIVERGCOL &&showLock && !showFeedback) || 
                                (col==yDIVERGCOL &&!showLock && showFeedback)){
                            
                            String speed = (String)((JComboBox)value).getSelectedItem();
                            try {
                                t.setDivergingSpeed(speed);
                            } catch (jmri.JmriException ex) {
                                JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                                return;
                            }
                            if ((!speedListThrown.contains(speed)) && !speed.contains("Global")){
                                speedListThrown.add(speed);
                            }
                            fireTableRowsUpdated(row,row);
                        }
                    } else super.setValueAt(value, row, col);
    		}
                
                public String getValue(String name) {
                    int val = turnManager.getBySystemName(name).getCommandedState();
                    switch (val) {
                    case Turnout.CLOSED: return closedText;
                    case Turnout.THROWN: return thrownText;
                    case Turnout.UNKNOWN: return rbean.getString("BeanStateUnknown");
                    case Turnout.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                    default: return "Unexpected value: "+val;
                    }
                }
                public Manager getManager() { return turnManager; }

                public NamedBean getBySystemName(String name) { return turnManager.getBySystemName(name);}
                public NamedBean getByUserName(String name) { return turnManager.getByUserName(name);}
                protected String getMasterClassName() { return getClassName(); }

                
                public void clickOn(NamedBean t) {
                    int state = ((Turnout)t).getCommandedState();
                    if (state==Turnout.CLOSED) ((Turnout)t).setCommandedState(Turnout.THROWN);
                    else ((Turnout)t).setCommandedState(Turnout.CLOSED);
                }
                
                JTable table;
                @Override
                public void configureTable(JTable table) {
                	this.table = table;
                    table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                    table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                    table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                    if(showFeedback)
                        setColumnToHoldButton(table,OPSEDITCOL,editButton());
                    super.configureTable(table);
                }
                
                // update table if turnout lock or feedback changes
                @Override
                protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("locked")) return true;
                    if (e.getPropertyName().equals("feedbackchange")) return true;
                    if (e.getPropertyName().equals("TurnoutDivergingSpeedChange")) return true;
                    if (e.getPropertyName().equals("TurnoutStraightSpeedChange")) return true;
                    else return super.matchPropertyName(e);
                }
                
                public void comboBoxAction(ActionEvent e){
                	if(log.isDebugEnabled()) log.debug("Combobox change");
                	table.getCellEditor().stopCellEditing();
                }

                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("DefaultTurnoutClosedSpeedChange")){
                        updateClosedList();
                    } else if (e.getPropertyName().equals("DefaultTurnoutThrownSpeedChange")){
                        updateThrownList();
                    } else {
                        super.propertyChange(e);
                    }
                }
                
                protected String getBeanType(){
                    return AbstractTableAction.rbean.getString("BeanNameTurnout");
                }               
            };  // end of custom data model
    }
    
    private void updateClosedList(){
        speedListClosed.remove(defaultClosedSpeedText);
        defaultClosedSpeedText = ("Use Global " + turnManager.getDefaultClosedSpeed());
        speedListClosed.add(0, defaultClosedSpeedText);
        m.fireTableDataChanged();
    }
    
    private void updateThrownList(){
        speedListThrown.remove(defaultThrownSpeedText);
        defaultThrownSpeedText = ("Use Global " + turnManager.getDefaultThrownSpeed());
        speedListThrown.add(0, defaultThrownSpeedText);
        m.fireTableDataChanged();
    }
    
    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleTurnoutTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }
    
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(10);
    JTextField userName = new JTextField(20);
    JComboBox prefixBox = new JComboBox();
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox("Add a range");
    JLabel sysNameLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    String userNameError = this.getClass().getName()+".DuplicateUserName";
    jmri.UserPreferencesManager p;
    
    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddTurnout"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TurnoutAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
                
            ActionListener rangeListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        canAddRange(e);
                    }
                };
            /* We use the proxy manager in this instance so that we can deal with 
            duplicate usernames in multiple classes */
            if (InstanceManager.turnoutManagerInstance() instanceof jmri.managers.AbstractProxyManager){
                jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) InstanceManager.turnoutManagerInstance();
                List<Manager> managerList = proxy.getManagerList();
                for(int x = 0; x<managerList.size(); x++){
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    //Simple test not to add a system with a duplicate System prefix
                    for (int i = 0; i<prefixBox.getItemCount(); i++){
                        if(((String)prefixBox.getItemAt(i)).equals(manuName))
                            addToPrefix=false;
                    }
                    if (addToPrefix)
                        prefixBox.addItem(manuName);
                }
                if(p.getComboBoxLastSelection(systemSelectionCombo)!=null)
                    prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
            else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(turnManager.getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", listener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }
    
    boolean showFeedback = false;
    void showFeedbackChanged() {
        showFeedback = showFeedbackBox.isSelected();
        m.fireTableStructureChanged(); // update view
    }
    
    boolean showLock = false;
    void showLockChanged() {
        showLock = showLockBox.isSelected();
        m.fireTableStructureChanged(); // update view
    }
    
    boolean showTurnoutSpeed = false;
    void showTurnoutSpeedChanged() {
        showTurnoutSpeed = showTurnoutSpeedBox.isSelected();
        m.fireTableStructureChanged(); // update view
    }
    
    /**
     * Create a JComboBox containing all the options for turnout automation parameters for
     * this turnout
     * @param t	the turnout
     * @return	the JComboBox
     */
    protected JComboBox makeAutomationBox(Turnout t) {
    	String[] str = new String[]{"empty"};
    	final JComboBox cb = new JComboBox(str);
    	final Turnout myTurnout = t;
    	updateAutomationBox(t, cb);
    	cb.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
                    setTurnoutOperation(myTurnout, cb);
                    cb.removeActionListener(this);		// avoid recursion
                    updateAutomationBox(myTurnout, cb);
                    cb.addActionListener(this);
    		}
            });
    	return cb;    	
    }
    
    
    /**
     * Create a JButton to edit a turnout operation. 
     * @return	the JButton
     */
    protected JButton editButton() {
        JButton editButton = new JButton(AbstractTableAction.rb.getString("EditTurnoutOperation"));
        return(editButton);
    }
    
    /**
     * Add the content and make the appropriate selection to a combox box for a turnout's
     * automation choices
     * @param t	turnout
     * @param cb	the JComboBox
     */
    public static void updateAutomationBox(Turnout t, JComboBox cb) {
    	TurnoutOperation[] ops = TurnoutOperationManager.getInstance().getTurnoutOperations();
    	cb.removeAllItems();
    	Vector<String> strings = new Vector<String>(20);
    	Vector<String> defStrings = new Vector<String>(20);
    	if(log.isDebugEnabled()) log.debug("start "+ops.length);
    	for (int i=0; i<ops.length; ++i) {
    	    if(log.isDebugEnabled()) log.debug("isDef "+ops[i].isDefinitive()+
                                               " mFMM "+ops[i].matchFeedbackMode(t.getFeedbackMode())+
                                               " isNonce "+ops[i].isNonce());
            if (!ops[i].isDefinitive()
                && ops[i].matchFeedbackMode(t.getFeedbackMode())
                && !ops[i].isNonce()) {
                strings.addElement(ops[i].getName());
            }
    	}
    	if(log.isDebugEnabled()) log.debug("end");
    	for (int i=0; i<ops.length; ++i) {
            if (ops[i].isDefinitive()
                && ops[i].matchFeedbackMode(t.getFeedbackMode())) {
                defStrings.addElement(ops[i].getName());
            }
    	}
    	java.util.Collections.sort(strings);
    	java.util.Collections.sort(defStrings);
    	strings.insertElementAt("Off",0);
    	strings.insertElementAt("Use Global Default",1);
    	for (int i=0; i<defStrings.size(); ++i) {
            try {
                strings.insertElementAt(defStrings.elementAt(i),i+2);
            } catch(java.lang.ArrayIndexOutOfBoundsException obe){
                //	           strings.insertElementAt(defStrings.elementAt(i),i+2);
            }
    	}
    	for (int i=0; i<strings.size(); ++i) {
            cb.addItem(strings.elementAt(i));
    	}
    	if (t.getInhibitOperation()) {
            cb.setSelectedIndex(0);
    	} else if (t.getTurnoutOperation() == null) {
            cb.setSelectedIndex(1);
    	} else if (t.getTurnoutOperation().isNonce()) {
            cb.setSelectedIndex(2);
    	} else {
            cb.setSelectedItem(t.getTurnoutOperation().getName());
    	}
    }
    
    /**
     * set the turnout's operation info based on the contents of the combo box
     * @param t	turnout
     * @param cb JComboBox
     */
    private void setTurnoutOperation(Turnout t, JComboBox cb) {
        switch (cb.getSelectedIndex())
            {
            case 0:			// Off
                t.setInhibitOperation(true);
                t.setTurnoutOperation(null);
                break;
            case 1:			// Default
                t.setInhibitOperation(false);
                t.setTurnoutOperation(null);
                break;
            default:		// named operation
                t.setInhibitOperation(false);
                t.setTurnoutOperation(TurnoutOperationManager.getInstance().
                                      getOperation(((String)cb.getSelectedItem())));	
                break;
            }
    }
    
    /**
     * pop up a TurnoutOperationConfig for the turnout 
     * @param t turnout
     * @param box JComboBox that triggered the edit
     */
    protected void editTurnoutOperation(Turnout t, JComboBox box) {
    	TurnoutOperation op = t.getTurnoutOperation();
    	if (op==null) {
            TurnoutOperation proto = TurnoutOperationManager.getInstance().getMatchingOperationAlways(t);
            if (proto != null) {
                op = proto.makeNonce(t);
                t.setTurnoutOperation(op);
            }
    	}
    	if (op != null) {
            if (!op.isNonce()) op = op.makeNonce(t);
            // make and show edit dialog
            TurnoutOperationEditor dialog = new TurnoutOperationEditor(this, f, op, t, box);
            dialog.setVisible(true);
    	} else {
            JOptionPane.showMessageDialog(f, "There is no operation type suitable for this turnout",
                                          "No operation type", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    protected class TurnoutOperationEditor extends JDialog {
    	TurnoutOperationConfig config;
    	TurnoutOperation myOp;
    	Turnout myTurnout;
    	
    	TurnoutOperationEditor(TurnoutTableAction tta, JFrame parent, TurnoutOperation op, Turnout t, JComboBox box) {
            super(parent);
            final TurnoutOperationEditor self = this;
            myOp = op;
            myOp.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("Deleted")) {
                            setVisible(false);
                        }
                    }
    		});
            myTurnout = t;
            config = TurnoutOperationConfig.getConfigPanel(op);
            setTitle();
            if (config != null) {
                Box outerBox = Box.createVerticalBox();
                outerBox.add(config);
                Box buttonBox = Box.createHorizontalBox();
                JButton nameButton = new JButton("Give name to this setting");
                nameButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String newName = JOptionPane.showInputDialog("New name for this parameter setting:");
                            if (newName != null && !newName.equals("")) {
                                if (!myOp.rename(newName)) {
                                    JOptionPane.showMessageDialog(self, "This name is already in use",
                                                                  "Name already in use", JOptionPane.ERROR_MESSAGE);
                                }
                                setTitle();
                                myTurnout.setTurnoutOperation(null);
                                myTurnout.setTurnoutOperation(myOp);	// no-op but updates display - have to <i>change</i> value
                            }
                        }
                    });
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            config.endConfigure();
                            if (myOp.isNonce() && myOp.equivalentTo(myOp.getDefinitive())) {
                                myTurnout.setTurnoutOperation(null);
                                myOp.dispose();
                                myOp = null;
                            }
                            self.setVisible(false);
                        }
                    });
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            self.setVisible(false);
                        }
                    });
                buttonBox.add(Box.createHorizontalGlue());
                if (!op.isDefinitive()) {
                    buttonBox.add(nameButton);
                }
                buttonBox.add(okButton);
                buttonBox.add(cancelButton);
                outerBox.add(buttonBox);
                getContentPane().add(outerBox);
            }
            pack();
    	}    
    	private void setTitle() {
            String title = "Turnout Operation \"" + myOp.getName() + "\"";
            if (myOp.isNonce()) {
                title = "Turnout operation for turnout " + myTurnout.getSystemName();
            }
            setTitle(title);    		
    	}
    }
    
    JCheckBox showFeedbackBox = new JCheckBox("Show feedback information");
    JCheckBox showLockBox = new JCheckBox("Show lock information");
    JCheckBox showTurnoutSpeedBox = new JCheckBox("Show Turnout Speed Details");
    JCheckBox doAutomationBox = new JCheckBox("Automatic retry");
    
    protected void setDefaultSpeeds(JFrame _who){
        JComboBox thrownCombo = new JComboBox(speedListThrown);
        JComboBox closedCombo = new JComboBox(speedListClosed);
        thrownCombo.setEditable(true);
        closedCombo.setEditable(true);
        
        JPanel thrown = new JPanel();
        thrown.add(new JLabel("Thrown Speed"));
        thrown.add(thrownCombo);
        
        JPanel closed = new JPanel();
        closed.add(new JLabel("Closed Speed"));
        closed.add(closedCombo);
        
        thrownCombo.removeItem(defaultThrownSpeedText);
        closedCombo.removeItem(defaultClosedSpeedText);
        
        thrownCombo.setSelectedItem(turnManager.getDefaultThrownSpeed());
        closedCombo.setSelectedItem(turnManager.getDefaultClosedSpeed());
        
        int retval = JOptionPane.showOptionDialog(_who,
                                          rb.getString("TurnoutGlobalSpeedMessage") , rb.getString("TurnoutGlobalSpeedMessageTitle"),
                                          0, JOptionPane.INFORMATION_MESSAGE, null,
                                          new Object[]{"Cancel", "OK", thrown, closed}, null );
        if (retval != 1) {
            return;
        }
        
        String closedValue = (String) closedCombo.getSelectedItem();
        String thrownValue = (String) thrownCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            turnManager.setDefaultThrownSpeed(thrownValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + thrownValue);
        }
        
        try {
            turnManager.setDefaultClosedSpeed(closedValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + closedValue);
        }
    }
    
    /**
     * Add the check box and Operations menu item
     */
    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(showFeedbackBox, this.getClass().getName());
        showFeedbackBox.setToolTipText(rb.getString("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showFeedbackChanged();
                }
            });
        f.addToBottomBox(showLockBox, this.getClass().getName());
        showLockBox.setToolTipText(rb.getString("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showLockChanged();
                }
            });
        f.addToBottomBox(doAutomationBox, this.getClass().getName());
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
        	}
            });
        f.addToBottomBox(showTurnoutSpeedBox, this.getClass().getName());
        showTurnoutSpeedBox.setToolTipText(rb.getString("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showTurnoutSpeedChanged();
                }
            });
    }
    
    @Override
    public void addToPanel(AbstractTableTabAction f) {
        String systemPrefix = ConnectionNameFromSystemName.getConnectionName(turnManager.getSystemPrefix());
        
        if (turnManager.getClass().getName().contains("ProxyTurnoutManager"))
            systemPrefix = "All";
        f.addToBottomBox(showFeedbackBox, systemPrefix);
        showFeedbackBox.setToolTipText(rb.getString("TurnoutFeedbackToolTip"));
        showFeedbackBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showFeedbackChanged();
                }
            });
        f.addToBottomBox(showLockBox, systemPrefix);
        showLockBox.setToolTipText(rb.getString("TurnoutLockToolTip"));
        showLockBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showLockChanged();
                }
            });
        f.addToBottomBox(doAutomationBox, systemPrefix);
        doAutomationBox.setSelected(TurnoutOperationManager.getInstance().getDoOperations());
        doAutomationBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    TurnoutOperationManager.getInstance().setDoOperations(doAutomationBox.isSelected());
        	}
        });
        
        f.addToBottomBox(showTurnoutSpeedBox, systemPrefix);
        showTurnoutSpeedBox.setToolTipText(rb.getString("TurnoutSpeedToolTip"));
        showTurnoutSpeedBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showTurnoutSpeedChanged();
                }
            });
    }
    
    @Override
    public void setMenuBar(BeanTableFrame f){
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        JMenu opsMenu = new JMenu("Automation");
        menuBar.add(opsMenu);
        JMenuItem item = new JMenuItem("Edit...");
        opsMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    new TurnoutOperationFrame(finalF);
        	}
            });
            
        menuBar.add(opsMenu);
        
        JMenu speedMenu = new JMenu("Speeds");
        item = new JMenuItem("Defaults...");
        speedMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    setDefaultSpeeds(finalF);
        	}
            });
        menuBar.add(speedMenu);
    }
    
    void okPressed(ActionEvent e) {
        // Test if bit already in use as a light
        //int iName=0;
        int numberOfTurnouts = 1;

        if(range.isSelected()){
            try {
                numberOfTurnouts = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Number to turnouts to Add must be a number!",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
        } 
        if (numberOfTurnouts>=65){
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 "You are about to add " + numberOfTurnouts + " Turnouts into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }

        String sName = null;
        String curAddress = sysName.getText();
        //String[] turnoutList = turnManager.formatRangeOfAddresses(sysName.getText(), numberOfTurnouts, getTurnoutPrefixFromName());
        //if (turnoutList == null)
        //    return;
        int iType = 0;
        int iNum=1;
        boolean useLastBit = false;
        boolean useLastType = false;
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        for (int x = 0; x < numberOfTurnouts; x++){
            try {
                curAddress = InstanceManager.turnoutManagerInstance().getNextValidAddress(curAddress, prefix);
            } catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert '" + curAddress + "' to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
            if (curAddress==null){
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName=prefix+InstanceManager.turnoutManagerInstance().typeLetter()+curAddress;
			String testSN = prefix+"L"+curAddress;
			jmri.Light testLight = InstanceManager.lightManagerInstance().
                    getBySystemName(testSN);
			if (testLight != null) {
				// Address is already used as a Light
				log.warn("Requested Turnout "+sName+" uses same address as Light "+testSN);
				if (!noWarn) {
					int selectedValue = JOptionPane.showOptionDialog(addFrame,
													rb.getString("TurnoutWarn1")+" "+sName+" "+rb.getString("TurnoutWarn2")+" "+
													testSN+".\n   "+rb.getString("TurnoutWarn3"),rb.getString("WarningTitle"),
													JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
													new Object[]{rb.getString("ButtonYes"),rb.getString("ButtonNo"),
																rb.getString("ButtonYesPlus")},rb.getString("ButtonNo"));
					if (selectedValue == 1) return;   // return without creating if "No" response
					if (selectedValue == 2) {
						// Suppress future warnings, and continue
						noWarn = true;
					}
				}
			}
            // Ask about two bit turnout control if appropriate
            
            if(!useLastBit){
                iNum = InstanceManager.turnoutManagerInstance().askNumControlBits(sName);
                if((InstanceManager.turnoutManagerInstance().isNumControlBitsSupported(sName)) && (range.isSelected())){
                    if(JOptionPane.showConfirmDialog(addFrame,
                                                 "Do you want to use the last setting for all turnouts in this range? ","Use Setting",
                                                 JOptionPane.YES_NO_OPTION)==0)
                        useLastBit=true;
                    // Add a pop up here asking if the user wishes to use the same value for all
                } else {
                //as isNumControlBits is not supported, then we will always use the same value.
                    useLastBit=true;
                }
            }
            if (iNum==0) {
                // User specified more bits, but bits are not available - return without creating
                return;
            }
            else {
                
                // Create the new turnout
                Turnout t;
                try {
                    t = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating       
                }
                
                if (t != null) {
                    String user = userName.getText();
                    if ((x!=0) && user != null && !user.equals(""))
                        user = user+":"+x;
                    if (user != null && !user.equals("") && (InstanceManager.turnoutManagerInstance().getByUserName(user)==null)) t.setUserName(user);
                    
                    else if (InstanceManager.turnoutManagerInstance().getByUserName(user)!=null && !p.getPreferenceState(getClassName(), "duplicateUserName")){
                        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage("Duplicate UserName","The username " + user + " specified is already in use and therefore will not be set", getClassName(), "duplicateUserName", false, true, org.apache.log4j.Level.ERROR);
                        //p.showInfoMessage("Duplicate UserName", "The username " + user + " specified is already in use and therefore will not be set", userNameError, "", false, true, org.apache.log4j.Level.ERROR);
                    }
                    t.setNumberOutputBits(iNum);
                    // Ask about the type of turnout control if appropriate
                    if(!useLastType){
                        iType = InstanceManager.turnoutManagerInstance().askControlType(sName);
                        if((InstanceManager.turnoutManagerInstance().isControlTypeSupported(sName)) && (range.isSelected())){
                            if (JOptionPane.showConfirmDialog(addFrame,
                                                 "Do you want to use the last setting for all turnouts in this range? ","Use Setting",
                                                 JOptionPane.YES_NO_OPTION)==0)// Add a pop up here asking if the user wishes to use the same value for all
                                useLastType=true;
                        } else {
                            useLastType = true;
                        }
                    }
                    t.setControlType(iType);
                }
            }
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }
    
    private void canAddRange(ActionEvent e){
        range.setEnabled(false);
        range.setSelected(false);
        if (turnManager.getClass().getName().contains("ProxyTurnoutManager")){
            jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) turnManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for(int x = 0; x<managerList.size(); x++){
                jmri.TurnoutManager mgr = (jmri.TurnoutManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)){
                    range.setEnabled(true);
                    return;
                }
            }
        }
        else if (turnManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))){
            range.setEnabled(true);
        }
    }
    
    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorTurnoutAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    private boolean noWarn = false;

    protected String getClassName() { return TurnoutTableAction.class.getName(); }
    
    @Override
    public void setMessagePreferencesDetails(){
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "duplicateUserName", rb.getString("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }
    
    @Override
    public String getClassDescription() { return rb.getString("TitleTurnoutTable"); }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutTableAction.class.getName());
}

/* @(#)TurnoutTableAction.java */
