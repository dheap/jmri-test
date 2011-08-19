package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.audio.*;
import jmri.AudioException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Content;

public class VSDecoder implements PropertyChangeListener {

    private String my_id;          // Unique ID for this VSDecoder
    private String vsd_path;       // Path to VSD file used
    private String profile_name;   // Name used in the "profiles" combo box
                                   // to select this decoder.
    DccLocoAddress address;        // Currently assigned loco address
    boolean initialized = false;   // This decoder has been initialized
    boolean enabled = false;       // This decoder is enabled
    private boolean is_default = false;  // This decoder is the default for its file

    HashMap<String, VSDSound> sound_list;   // list of sounds
    HashMap<String, Trigger> trigger_list;  // list of triggers
    HashMap<String, SoundEvent> event_list; // list of events
    
    static final public int calcEngineNotch(final float throttle) {
	// This will convert to a value 0-8.
	int notch = (int) Math.rint(throttle * 8);
	if (notch < 0) { notch = 0; }
	log.warn("Throttle: " + throttle + " Notch: " + notch);
	return(notch+1);

    }

    static final public int calcEngineNotch(final double throttle) {
	// This will convert from a % to a value 0-8.
	int notch = (int) Math.rint(throttle * 8);
	if (notch < 0) { notch = 0; }
	//log.warn("Throttle: " + throttle + " Notch: " + notch);
	return(notch+1);

    }

    public VSDecoder(String id, String name) {

	profile_name = name;
	my_id = id;
	
	sound_list = new HashMap<String, VSDSound>();
	trigger_list = new HashMap<String, Trigger>();
	event_list = new HashMap<String, SoundEvent>();
	    
	// Force re-initialization
	initialized = _init();
    }

    public VSDecoder(String id, String name, String path) {
	this(id, name);

	vsd_path = path;

	try {
	    VSDFile vsdfile = new VSDFile(path);
	    if (vsdfile.isInitialized()) {
		log.debug("Constructor: vsdfile init OK, loading XML...");
		this.setXml(vsdfile, name);
	    } else {
		log.debug("Constructor: vsdfile init FAILED.");
		initialized = false;
	    }
	} catch (java.util.zip.ZipException e) {
	    log.error("ZipException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	} catch (java.io.IOException ioe) {
	    log.error("IOException loading VSDecoder from " + path);
	    // would be nice to pop up a dialog here...
	}
    }

    private boolean _init() {
	// Do nothing for now
	return(true);
    }

    public String getID() { return(my_id); }

    public boolean isInitialized() { return(initialized); }

    public void setVSDFilePath(String p) {
	vsd_path = p;
    }

    public String getVSDFilePath() {
	return(vsd_path);
    }

    public void throttlePropertyChange(PropertyChangeEvent event) {
	//WARNING: FRAGILE CODE
	// This will break if the return type of the event.getOld/NewValue() changes.
	
	String eventName = event.getPropertyName();
	Object oldValue = event.getOldValue();
	Object newValue = event.getNewValue();

	// Skip this if disabled
	if (!enabled)
	    return;

	log.warn("VSDecoderPane throttle property change: " + eventName);

	if (oldValue != null)
	    log.warn("Old: " + oldValue.toString());
	if (newValue != null)
	    log.warn("New: " + newValue.toString());

	// Iterate through the list of sound events, forwarding the propertyChange event.
	for (SoundEvent t : event_list.values()) {
	    t.propertyChange(event);
	}

	// Iterate through the list of triggers, forwarding the propertyChange event.
	for (Trigger t : trigger_list.values()) {
	    t.propertyChange(event);
	}
    }

    public void releaseAddress(int number, boolean isLong) {
	// remove the listener, if we can...
    }

    public void setAddress(int number, boolean isLong) {
	this.setAddress(new DccLocoAddress(number, isLong));
    }

    public void setAddress(DccLocoAddress a) {
	address = a;
	jmri.InstanceManager.throttleManagerInstance().attachListener(address, new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
		    throttlePropertyChange(event);
		}
	    });
	log.debug("VSDecoder: Address set to " + address.toString());
    }

    public DccLocoAddress getAddress() {
	return(address);
    }

    public void propertyChange(PropertyChangeEvent evt) {
	// Respond to events from the GUI.
	if (evt.getPropertyName().equals("AddressChange")) {
	    this.setAddress((DccLocoAddress)evt.getNewValue());
	}
    }

    public VSDSound getSound(String name) {
	return(sound_list.get(name));
    }

    public void toggleBell() {
	VSDSound snd = sound_list.get("BELL");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }
    
    public void toggleHorn() {
	VSDSound snd = sound_list.get("HORN");
        if(snd.isPlaying())
            snd.stop();
        else
            snd.loop();
    }

    public void playHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.loop();
    }

    public void shortHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.play();
    }

    public void stopHorn() {
	VSDSound snd = sound_list.get("HORN");
	snd.stop();
    }

    // Java Bean set/get Functions

    public void setProfileName(String pn) {
	profile_name = pn;
    }

    public String getProfileName() {
	return(profile_name);
    }
	
    public void enable() {
	enabled = true;
    }

    public void disable() {
	enabled = false;
    }

    public Collection getEventList() {
	return(event_list.values());
    }
    
    public boolean isDefault() {
	return(is_default);
    }

    public void setDefault(boolean d) {
	is_default = d;
    }

    public Element getXml() {
	Element me = new Element("vsdecoder");
	ArrayList<Element> le = new ArrayList<Element>();

	me.setAttribute("name", this.profile_name);

	// If this decoder is marked as default, add the default Element.
	if (is_default)
	    me.addContent(new Element("default"));
	
	for (SoundEvent se : event_list.values()) {
	    le.add(se.getXml());
	}

	for (VSDSound vs : sound_list.values()) {
	    le.add(vs.getXml());
	}

	for (Trigger t : trigger_list.values()) {
	    le.add(t.getXml());
	}

	
	me.addContent(le);

	// Need to add whatever else here.

	return(me);
    }

    @Deprecated
    public void setXml(Element e) {
	this.setXml(e, null);
    }

    @Deprecated
    public void setXml(Element e, VSDFile vf) {
	this.setXml(vf);
    }

    @Deprecated
    public void setXml(VSDFile vf) { };

    public void setXml(VSDFile vf, String pn) {
	Iterator itr;
	Element e = null;
	Element el = null;
	SoundEvent se;
	
	// Set filename and path
	if (vf != null) {
	    log.debug("VSD File Name = " + vf.getName());
	    // need to choose one.
	    this.setVSDFilePath(vf.getName());
	}

	// Find the <profile/> element that matches the name pn
	List<Element> profiles = vf.getRoot().getChildren("profile");
	java.util.Iterator<Element> i = profiles.iterator();
	while (i.hasNext()) {
	    e = i.next();
	    if (e.getAttributeValue("name").equals(pn))
		break;
	}
	// E is now the first <profile/> in vsdfile that matches pn.

	// Set this decoder's name.
	this.setProfileName(e.getAttributeValue("name"));
	log.debug("Decoder Name = " + e.getAttributeValue("name"));
	if(vf != null) {
	}

	// Read and create all of its components.

	// Check for default element.
	if (e.getChild("default") != null) {
	    log.debug("" + getProfileName() + "is default.");
	    is_default = true;
	}
	else {
	    is_default = false;
	}

	// Log and print all of the child elements.
	itr = (e.getChildren()).iterator();
	while(itr.hasNext()) {
	    // Pull each element from the XML file.
	    el = (Element)itr.next();
	    log.debug("Element: " + el.toString());
	    if (el.getAttribute("name") != null) {
		log.debug("  Name: " + el.getAttributeValue("name"));
		log.debug("   type: " + el.getAttributeValue("type"));
	    }
	}


	// First, the sounds.
	itr = (e.getChildren("sound")).iterator();
	while(itr.hasNext()) {
	    el = (Element)itr.next();
	    if (el.getAttributeValue("type") == null) {
		// Empty sound.  Skip.
		log.debug("Skipping empty Sound.");
		continue;
	    } else if (el.getAttributeValue("type").equals("configurable")) {
		// Handle configurable sounds.
		ConfigurableSound cs = new ConfigurableSound(el.getAttributeValue("name"));
		cs.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"),cs);
	    } else if (el.getAttributeValue("type").equals("diesel")) {
		// Handle a Diesel Engine sound
		EngineSound es = new EngineSound(el.getAttributeValue("name"));
		es.setXml(el, vf);
		sound_list.put(el.getAttributeValue("name"), es);
	    } else {
		//TODO: Some type other than configurable sound.  Handle appropriately
	    }
	}

	// Next, grab all of the SoundEvents
	// Have to do the sounds first because the SoundEvent's setXml() will
	// expect to be able to look it up.
	itr = (e.getChildren("sound-event")).iterator();
	while (itr.hasNext()) {
	    el = (Element)itr.next();
	    switch(SoundEvent.ButtonType.valueOf(el.getAttributeValue("buttontype").toUpperCase())) {
	    case MOMENTARY:
		se = new MomentarySoundEvent(el.getAttributeValue("name"));
		break;
	    case TOGGLE:
		se = new ToggleSoundEvent(el.getAttributeValue("name"));
		break;
	    case ENGINE:
		se = new EngineSoundEvent(el.getAttributeValue("name"));
		break;
	    case NONE:
	    default:
		se = new SoundEvent(el.getAttributeValue("name"));
	    }
	    se.setParent(this);
	    se.setXml(el, vf);
	    event_list.put(se.getName(), se);
	}

	// Handle other types of children similarly here.
	
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDecoder.class.getName());

}
