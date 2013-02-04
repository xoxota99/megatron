package com.megatron.test.pathfinding.applet.grid.astar;

import com.megatron.test.pathfinding.applet.grid.*;

public class GridLocationAstar extends GridLocation{

	private double doneDist;
	private double todoDist;
	
	public GridLocationAstar(int x, int y, boolean end, double doneDist, double todoDist) {
		super(x, y, end);
		this.doneDist = doneDist;
		this.todoDist = todoDist;
	}
	
	public double getDoneDistance(){
		return doneDist;
	}
	
	public double getTotalDistance(){
		return doneDist + todoDist;
	}

}
