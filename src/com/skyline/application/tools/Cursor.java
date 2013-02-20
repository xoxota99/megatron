package com.skyline.application.tools;

import com.skyline.application.*;
import com.skyline.model.*;

/**
 * Cursor contains information about how a tool is applied, as well as how the
 * cursor will be rendered on screen. Each tool will have a Cursor instance,
 * through which the tool will be applied, either in a specific radius, to a
 * specific power, or to the whole world.
 * 
 * @author philippd
 * 
 */
public interface Cursor {

	//Some theoretical methods. (thinking out loud).
	public int getRenderHeight();
	public int getRenderRadius();
	public int getPower();
	public int getRadius();
	public void applyTool(Tool t, WorldState worldState, int x, int y);
}
