package com.skyline.application.tools;

import java.util.*;

import com.skyline.application.*;
import com.skyline.application.events.*;
import com.skyline.model.*;

/**
 * Represents a tool that acts "Globally" . Examples
 * <ul>
 * <li>Generate Terrain</li>
 * <li>Global Raise/Lower Terrain</li>
 * <li>Global Erode Terrain</li>
 * <li>Normalize Terrain</li>
 * <li>Toggle Wireframe(?)</li>
 * </ul>
 * 
 * @author philippd
 * 
 */
public abstract class GlobalTool implements Tool {

	private String name;
	private String toolTipText;

	public GlobalTool(String name, String toolTipText) {
		this.name = name;
		this.toolTipText = toolTipText;
	}

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

	/**
	 * Execute this tool on the specified worldState.
	 * 
	 * @param worldState
	 */
	public abstract void execute(WorldState worldState, int modifiers);

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
