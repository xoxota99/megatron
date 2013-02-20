package com.skyline.test.pathfinding.applet.grid;

import com.skyline.test.pathfinding.applet.core.*;
import com.skyline.test.pathfinding.applet.grid.astar.*;

public class GridPathfinding implements Pathfinding{

	GridAstar astar;
	GridHeuristic heuristic;
	
	public GridPathfinding(){
		heuristic = new GridHeuristicManathan();
	}
	
	@Override
	public GridPath getPath(Location s, Location e, Map m) {
		GridLocation start = (GridLocation)s;
		GridLocation end = (GridLocation)e;
		GridMap map = (GridMap)m;
		
		astar = new GridAstar(start, end, map, heuristic);
		
		return astar.getPath();
	}

}
