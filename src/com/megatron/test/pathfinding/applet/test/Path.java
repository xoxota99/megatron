package com.megatron.test.pathfinding.applet.test;

import java.awt.*;

import com.megatron.test.pathfinding.applet.grid.*;

public class Path {
	
	GridPath path;
	
	public void setPath(GridPath path){
		this.path = path;
	}
	
	public void drawPath(Graphics2D g){
		
		if(path != null){
			for(GridLocation location : path.getList()){
				g.translate(10, 10);
				g.setColor(Color.orange);
				g.fillRect(location.getX()*8, location.getY()*8, 8, 8);
				g.translate(-10, -10);
			}
		}
	}

}
