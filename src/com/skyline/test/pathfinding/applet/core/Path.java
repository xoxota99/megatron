package com.skyline.test.pathfinding.applet.core;
public interface Path {
	
	public boolean hasNextMove();
	
	public Location getNextMove();
	
	public Path clone();

}
