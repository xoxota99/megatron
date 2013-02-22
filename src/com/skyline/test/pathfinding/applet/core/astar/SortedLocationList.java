
package com.skyline.test.pathfinding.applet.core.astar;

import com.skyline.test.pathfinding.applet.core.*;


public interface SortedLocationList {
	
	public void add(Location location);
	
	public Location getNext();
	
	public boolean hasNext();

}
