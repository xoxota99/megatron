package com.megatron.application.tools;

import java.util.*;

import com.megatron.application.*;
import com.megatron.application.events.*;
import com.megatron.model.*;

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
	 * @param context
	 */
	public abstract void execute(WorldState context);

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
