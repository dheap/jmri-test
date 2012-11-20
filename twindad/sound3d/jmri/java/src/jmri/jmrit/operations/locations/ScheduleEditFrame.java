// ScheduleEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Frame for user edit of a schedule
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */

public class ScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	ScheduleTableModel scheduleModel = new ScheduleTableModel();
	JTable scheduleTable = new JTable(scheduleModel);
	JScrollPane schedulePane;
	
	ScheduleManager manager;
	LocationManagerXml managerXml;

	Schedule _schedule = null;
	ScheduleItem _scheduleItem = null;
	Location _location = null;
	Track _track = null;

	// labels

	// major buttons
	JButton addTypeButton = new JButton(rb.getString("AddType"));
	JButton saveScheduleButton = new JButton(rb.getString("SaveSchedule"));
	JButton deleteScheduleButton = new JButton(rb.getString("DeleteSchedule"));
	JButton addScheduleButton = new JButton(rb.getString("AddSchedule"));

	// check boxes
	JCheckBox checkBox;
	
	// radio buttons
    JRadioButton addLocAtTop = new JRadioButton(rb.getString("Top"));
    JRadioButton addLocAtBottom = new JRadioButton(rb.getString("Bottom"));   
	JRadioButton sequentialRadioButton = new JRadioButton(rb.getString("Sequential"));
	JRadioButton matchRadioButton = new JRadioButton(rb.getString("Match"));
	
	// text field
	JTextField scheduleNameTextField = new JTextField(20);
	JTextField commentTextField = new JTextField(35);
	
	// combo boxes
	JComboBox typeBox = new JComboBox();

	public static final int MAX_NAME_LENGTH = 25;
	public static final String NAME = rb.getString("Name");
	public static final String DISPOSE = "dispose" ;

	public ScheduleEditFrame() {
		super();
	}

	public void initComponents(Schedule schedule, Location location, Track track) {
				
		_schedule = schedule;
		_location = location;
		_track = track;

		// load managers
		manager = ScheduleManager.instance();
		managerXml = LocationManagerXml.instance();
		
	   	// Set up the jtable in a Scroll Pane..
    	schedulePane = new JScrollPane(scheduleTable);
    	schedulePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

      	scheduleModel.initTable(this, scheduleTable, schedule, _location, _track);
		if (_schedule != null){
			scheduleNameTextField.setText(_schedule.getName());
			commentTextField.setText(_schedule.getComment());
	      	enableButtons(true);
		} else {
			enableButtons(false);
		}
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
 				
		// Layout the panel by rows
	    JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
      	JScrollPane p1Pane = new JScrollPane(p1);
       	p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
       	p1Pane.setMinimumSize(new Dimension(300,3*scheduleNameTextField.getPreferredSize().height));
       	p1Pane.setBorder(BorderFactory.createTitledBorder(""));
    	
		// row 1a name
	   	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
		addItem(pName, scheduleNameTextField, 0, 0);
		
		// row 1b comment
    	JPanel pC = new JPanel();
    	pC.setLayout(new GridBagLayout());
    	pC.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
		addItem(pC, commentTextField, 0, 0);
		
		// row 1c mode
		JPanel pMode = new JPanel();
		pMode.setLayout(new GridBagLayout());
		pMode.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduleMode")));
		addItem(pMode, sequentialRadioButton, 0, 0);
		addItem(pMode, matchRadioButton, 1, 0);
		
		sequentialRadioButton.setToolTipText(rb.getString("TipSequential"));
		matchRadioButton.setToolTipText(rb.getString("TipMatch"));
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(sequentialRadioButton);
		modeGroup.add(matchRadioButton);
		
		sequentialRadioButton.setSelected(_track.getScheduleMode() == Track.SEQUENTIAL);
		matchRadioButton.setSelected(_track.getScheduleMode() == Track.MATCH);
		scheduleModel.setMatchMode(_track.getScheduleMode() == Track.MATCH);
		
		p1.add(pName);
		p1.add(pC);
		p1.add(pMode);

		// row 2
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridBagLayout());
    	p3.setBorder(BorderFactory.createTitledBorder(rb.getString("AddItem")));
    	addItem(p3, typeBox, 0, 1);
    	addItem(p3, addTypeButton, 1, 1);
    	addItem(p3, addLocAtTop, 2, 1);
    	addItem(p3, addLocAtBottom, 3, 1);
        ButtonGroup group = new ButtonGroup();
    	group.add(addLocAtTop);
    	group.add(addLocAtBottom);
    	addLocAtBottom.setSelected(true);
    	
		// row 11 buttons
    	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());
    	pB.setBorder(BorderFactory.createTitledBorder(""));

		// row 13
		addItem(pB, deleteScheduleButton, 0, 0);
		addItem(pB, addScheduleButton, 1, 0);
		addItem(pB, saveScheduleButton, 3, 0);
		
		getContentPane().add(p1Pane);
       	getContentPane().add(schedulePane);
       	getContentPane().add(p3);
       	getContentPane().add(pB);
		
		// setup buttons
		addButtonAction(addTypeButton);
		addButtonAction(deleteScheduleButton);
		addButtonAction(addScheduleButton);
		addButtonAction(saveScheduleButton);
		
		// setup radio buttons
		addRadioButtonAction(sequentialRadioButton);
		addRadioButtonAction(matchRadioButton);
		
		// setup combobox
		loadTypeComboBox();

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		menuBar.add(toolMenu);
		toolMenu.add(new ScheduleOptionsAction(this));
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Schedules", true);

		//	 get notified if car types or roads are changed
		CarTypes.instance().addPropertyChangeListener(this);
		_location.addPropertyChangeListener(this);
		_track.addPropertyChangeListener(this);
		
		// set frame size and schedule for display
		pack();
		if (getWidth() < Control.panelWidth)
			setSize(Control.panelWidth, getHeight());
		setVisible(true);
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addTypeButton){
			log.debug("schedule add location button activated");
			if (typeBox.getSelectedItem() != null){
				if (typeBox.getSelectedItem().equals(""))
					return;
				addNewScheduleItem();
			}
		}
		if (ae.getSource() == saveScheduleButton){
			log.debug("schedule save button activated");
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (_schedule == null && schedule == null){
				saveNewSchedule();
			} else {
				if (schedule != null && schedule != _schedule){
					reportScheduleExists(rb.getString("save"));
					return;
				}
				saveSchedule();
			}
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == deleteScheduleButton){
			log.debug("schedule delete button activated");
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("DoYouWantToDeleteSchedule"),new Object[]{scheduleNameTextField.getText()}), rb.getString("DeleteSchedule?"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
				return;
			}
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (schedule == null)
				return;
			
			if (_track != null)
				_track.setScheduleId("");
			
			manager.deregister(schedule);
			_schedule = null;
			
			enableButtons(false);
			// save schedule file
			OperationsXml.save();
		}
		if (ae.getSource() == addScheduleButton){
			Schedule schedule = manager.getScheduleByName(scheduleNameTextField.getText());
			if (schedule != null){
				reportScheduleExists(rb.getString("add"));
				return;
			}
			saveNewSchedule();
		}
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae){
		log.debug("Radio button action");
		scheduleModel.setMatchMode(ae.getSource() == matchRadioButton);		
	}
	
	private void addNewScheduleItem(){
		// add item to this schedule
		if (addLocAtTop.isSelected())
			_schedule.addItem((String)typeBox.getSelectedItem(),0);
		else
			_schedule.addItem((String)typeBox.getSelectedItem());
		if (_track.getScheduleMode() == Track.MATCH && typeBox.getSelectedIndex() < typeBox.getItemCount()-1)
			typeBox.setSelectedIndex(typeBox.getSelectedIndex()+1);
	}
	
	private void saveNewSchedule(){
		if (!checkName(rb.getString("add")))
			return;
		Schedule schedule = manager.newSchedule(scheduleNameTextField.getText());
		scheduleModel.initTable(this, scheduleTable, schedule, _location, _track);
		_schedule = schedule;
		// enable checkboxes
		enableButtons(true);
		saveSchedule();
	}
	
	private void saveSchedule (){
		if (!checkName(rb.getString("save")))
			return;
		_schedule.setName(scheduleNameTextField.getText());
		_schedule.setComment(commentTextField.getText());
		
		if(scheduleTable.isEditing()){
			log.debug("schedule table edit true");
			scheduleTable.getCellEditor().stopCellEditing();
			scheduleTable.clearSelection();
		}
		if (_track != null){
			_track.setScheduleId(_schedule.getId());
			if (sequentialRadioButton.isSelected())
				_track.setScheduleMode(Track.SEQUENTIAL);
			else
				_track.setScheduleMode(Track.MATCH);
		}
		
		saveTableDetails(scheduleTable);
		// save schedule file
		OperationsXml.save();
	}
	
	private void loadTypeComboBox(){
		typeBox.removeAllItems();
		String[] types = CarTypes.instance().getNames();
		for (int i=0; i<types.length; i++){
			if (_track.acceptsTypeName(types[i]))
					typeBox.addItem(types[i]);
		}
	}

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(String s){
		if (scheduleNameTextField.getText().trim().equals(""))
			return false;
		if (scheduleNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Schedule name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					MessageFormat.format(rb.getString("ScheduleNameLengthMax"),new Object[]{Integer.toString(MAX_NAME_LENGTH+1)}),
					MessageFormat.format(rb.getString("CanNotSchedule"),new Object[]{s}),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportScheduleExists(String s){
		log.info("Can not " + s + ", schedule already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("ReportExists"), MessageFormat.format(rb.getString("CanNotSchedule"),new Object[]{s}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		typeBox.setEnabled(enabled);
		addTypeButton.setEnabled(enabled);
		addLocAtTop.setEnabled(enabled);
		addLocAtBottom.setEnabled(enabled);
		saveScheduleButton.setEnabled(enabled);
		deleteScheduleButton.setEnabled(enabled);
		scheduleTable.setEnabled(enabled);
		// the inverse!
		addScheduleButton.setEnabled(!enabled);
	}
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);
		_location.removePropertyChangeListener(this);
		_track.removePropertyChangeListener(this);
		scheduleModel.dispose();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("ScheduleEditFrame sees property change: " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)){
			loadTypeComboBox();
		}
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ScheduleEditFrame.class.getName());
}
