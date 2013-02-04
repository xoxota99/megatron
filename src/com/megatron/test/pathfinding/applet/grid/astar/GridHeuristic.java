package com.megatron.test.pathfinding.applet.grid.astar;

import com.megatron.test.pathfinding.applet.grid.*;

public interface GridHeuristic {
	
	public double getDistance(int x, int y, GridLocation location);

}
