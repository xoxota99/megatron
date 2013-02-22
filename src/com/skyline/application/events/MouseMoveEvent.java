package com.skyline.application.events;

import com.jme3.math.*;

public class MouseMoveEvent extends AbstractEvent {

	private Vector3f position;

	public MouseMoveEvent(Object source) {
		super(source);
	}

	public MouseMoveEvent(Object source,Vector3f position) {
		this(source);
		this.position=position;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

}
