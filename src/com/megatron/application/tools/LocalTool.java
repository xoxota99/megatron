package com.megatron.application.tools;

import java.util.*;

import com.megatron.application.*;
import com.megatron.application.events.*;

public abstract class LocalTool implements Tool {
	protected String name;
	protected String toolTipText;
	protected CursorType cursorType;
	protected int power; // The intensity of the effect of this tool, in logical
							// units.
	protected int radius; // The radius of the effect of this tool, in logical
							// coordinates.

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
	 *            the X coordinate of the tool's centroid.
	 * @param y
	 *            the Y coordinate of the tool's centroid.
	 */
	public abstract void execute(WorldState worldState, int x, int y);

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

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof WorldState) {
			if (arg instanceof Event) {
				if (((Event) arg).getSource() != this) {
					System.out.println("Hey! I received a WorldState Event (of type " + arg.getClass().getName() + ")");
				} else {
					System.out.println("Hey! I received a WorlState Event, but I'm the source, so I'll ignore it.");
				}
			}
		}
	}

}
