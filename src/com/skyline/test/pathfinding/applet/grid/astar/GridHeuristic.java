package com.skyline.test.pathfinding.applet.grid.astar;

import com.skyline.test.pathfinding.applet.grid.*;

public interface GridHeuristic {
	
	public double getDistance(int x, int y, GridLocation location);

}
