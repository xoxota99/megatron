package com.skyline.test.pathfinding.applet.grid;

import com.skyline.test.pathfinding.applet.core.*;

public class GridLocation implements Location{
	
	private int x;
	private int y;
	private boolean end;
	
	public GridLocation(int x, int y, boolean end){
		this.x = x;
		this.y = y;
		this.end = end;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public boolean isEnd(){
		return end;
	}

}
