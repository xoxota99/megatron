package com.skyline.application.events;


/**
 * Marker interface for Events.
 * 
 * @author philippd
 * 
 */
public interface Event {

//	public EventType getEventType();
	public Object getSource();
}
