//AbstractOperationsServer.java

package jmri.jmris;

import java.io.*;

import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.locations.*;

/**
 * Abstract interface between the JMRI operations and a network connection
 * 
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (added checks for null train)
 * @version $Revision$
 */

abstract public class AbstractOperationsServer implements
		java.beans.PropertyChangeListener {

	TrainManager tm = null;
	LocationManager lm = null;

	public AbstractOperationsServer() {
		tm = TrainManager.instance();
		tm.addPropertyChangeListener(this);
		lm = LocationManager.instance();
		lm.addPropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent ev) {
		// we may need to do something with the properties at some
		// point, but we don't know what yet.
	}

	public void dispose() {
		if (tm != null)
			tm.removePropertyChangeListener(this);
		if (lm != null)
			lm.removePropertyChangeListener(this);
	}

	/* send a list of trains */
	public void sendTrainList() throws IOException {
		java.util.List<String> trainList = tm.getTrainsByNameList();
		for (String trainID : trainList)
			sendInfoString("OPERATIONS TRAINS "
					+ tm.getTrainById(trainID).getName());
		// end list with a . on a line by itself
		sendInfoString("OPERATIONS TRAINS .");
	}

	/* send a list of locations */
	public void sendLocationList() throws IOException {
		java.util.List<String> locationList = lm.getLocationsByNameList();
		for (String LocationID : locationList)
			sendInfoString("OPERATIONS LOCATIONS "
					+ lm.getLocationById(LocationID).getName());
		// end list with a . on a line by itself
		sendInfoString("OPERATIONS LOCATIONS .");
	}

	/* send train status */
	public void sendTrainStatus(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null)
			sendInfoString("OPERATIONS " + trainName + " STATUS "
					+ train.getStatus());
		else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* send train location */
	public void sendTrainLocation(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null)
			sendInfoString("OPERATIONS " + trainName + " LOCATION "
					+ train.getCurrentLocationName());
		else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* Set the current location of the train */
	public void setTrainLocation(String trainName, String locationName)
			throws IOException {
		log.debug("Set train " + trainName + " Location " + locationName);
		Train train = tm.getTrainByName(trainName);
		if (train != null) {
			if (train.move(locationName))
				sendTrainLocation(trainName);
			else
				sendInfoString("OPERATIONS ERROR Move of " + trainName
						+ " to location " + locationName + " failed.");
		} else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* send train length */
	public void sendTrainLength(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null)
			sendInfoString("OPERATIONS " + trainName + " LENGTH "
					+ train.getTrainLength());
		else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* send train tonnage */
	public void sendTrainWeight(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null)
			sendInfoString("OPERATIONS " + trainName + " WEIGHT "
					+ train.getTrainWeight());
		else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* send number of cars in train */
	public void sendTrainNumberOfCars(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null)
			sendInfoString("OPERATIONS " + trainName + " CARS "
					+ train.getNumberCarsInTrain());
		else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/* Terminate the train */
	public void terminateTrain(String trainName) throws IOException {
		Train train = tm.getTrainByName(trainName);
		if (train != null) {
			train.terminate();
			sendTrainStatus(trainName);
		} else
			sendInfoString("OPERATIONS ERROR train name doesn't exist "+trainName);
	}

	/*
	 * Protocol Specific Abstract Functions
	 */

	abstract public void sendInfoString(String statusString) throws IOException;

	abstract public void sendErrorStatus() throws IOException;

	abstract public void parseStatus(String statusString)
			throws jmri.JmriException;

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(AbstractOperationsServer.class.getName());

}
