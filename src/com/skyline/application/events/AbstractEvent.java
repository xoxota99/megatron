package com.skyline.application.events;

public abstract class AbstractEvent implements Event {
	protected Object source;
	protected EventType eventType;

	public AbstractEvent(Object source) {
		this.source = source;
	}

	protected AbstractEvent(EventType eventType) {
		this.eventType = eventType;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

}
