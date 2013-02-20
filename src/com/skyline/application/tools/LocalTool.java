package com.skyline.application.tools;

import java.util.*;

import com.skyline.application.events.*;
import com.skyline.model.*;

public abstract class LocalTool implements Tool {
	
	protected String name;
	protected String toolTipText;
	protected CursorType cursorType;
	protected float power; // The relative intensity of the effect of this tool, [0..1]
	protected float radius; // The relative radius of the effect of this tool, [0..1]

	protected LocalTool(String name, String toolTipText, CursorType cursorType) {
		this.name = name;
		this.toolTipText = toolTipText;
		this.cursorType = cursorType;
	}

	/**
	 * Execute this tool on the specified WorldState, in the specified location.
	 * 
	 * @param worldState
	 *            the WorldState on which to execute the tool.
	 * @param x
	 *            the X coordinate of the tool's centroid, in "world"
	 *            coordinates (ie: in the renderer)
	 * @param y
	 *            the Y coordinate of the tool's centroid, in "world"
	 *            coordinates (ie: in the renderer)
	 */
	public abstract void execute(WorldState worldState, int x, int y, int modifiers);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	public CursorType getCursorType() {
		return cursorType;
	}

	public void setCursorType(CursorType cursorType) {
		this.cursorType = cursorType;
	}

	public float getPower() {
		return power;
	}

	public void setPower(float power) {
		this.power = power;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ToolSet) {	//TODO: This will actually be an instance of ToolSet. Which is probably wrong.
			if (arg instanceof Event) {
				if (((Event) arg).getSource() != this) {
					//Hey! I received a ToolSet Event.
				} else {
					//Hey! I received a ToolSet Event, but I'm the source, so I'll ignore it.
				}
			} else {
				System.out.println(arg.getClass().getName());
			}
		} else {
			System.out.println(o.getClass().getName());
		}
	}

}
