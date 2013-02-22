package com.skyline.application.tools;

import java.util.*;

/**
 * Tools sit on the fence between the "logical" coordinate system (which uses
 * the coordinates established by the worldState.size), and the "real-world"
 * coordinate system, which can be affected by scaling, translating, rotating,
 * etc in the renderer.
 * 
 * Wherever possible, we try to operate on the underlying data, and ignore
 * details of the renderer.
 * 
 * Tools observe their respective ToolBars, which will pass along WorldState
 * events.
 * 
 * @author philippd
 * 
 */
public interface Tool extends Observer {
	public String getName();

	public String getToolTipText();

	/**
	 * Is this tool "continuous"? Can I click and hold the mouse button (or key)
	 * to trigger it continuously?
	 * 
	 * @return true if this tool is triggered continuously as long as the input
	 *         is on, or false if it triggers once then stops.
	 */
	public boolean isContinuous();
}
