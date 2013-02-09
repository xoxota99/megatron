package com.megatron.terrain;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.megatron.terrain.path.*;

public class RiverNode extends AStarNode {

	private Point location;
	private float height;
	private static List<List<RiverNode>> allNodes = new ArrayList<List<RiverNode>>();

	public String toString() {
		return String.format("%d,%d", location.x, location.y);
	}

	public RiverNode(Point coordinates, float value) {
		this.location = coordinates;
		this.height = value;
		List<RiverNode> l = null;
		if (allNodes.size() > coordinates.x) {
			l = allNodes.get(coordinates.x);
		}
		if (l == null) {
			l = new ArrayList<RiverNode>();
			allNodes.add(coordinates.x, l);
		}
		l.add(coordinates.y, this);
	}

	@Override
	public float getCost(AStarNode node) {
		RiverNode rn = (RiverNode) node;
		float cost = 0f;
		// cost = rn.getHeight(); // adjust for absolute height.
		// cost += rn.getHeight() - getHeight(); // adjust for relative height.

		// Additional Cost for distance.
		float dx = rn.getLocation().x - location.x;
		float dy = rn.getLocation().y - location.y;
//		float dz = rn.getHeight() - height;
//		cost += (float) Math.sqrt((dx * dx) + (dy * dy));// + (dz * dz));		//in fact, Just Distance.
		cost += 1.2*Math.abs(dx);
		cost += 1.2*Math.abs(dy);
//		cost += 1.2*Math.abs(dz);
//		cost += 1.2*rn.getHeight();
		return cost;
	}

	@Override
	public float getEstimatedCost(AStarNode node) {
		// estimate based on straight-line distance.
		return getCost(node);
		// RiverNode rn = (RiverNode) node;
		// float dx = rn.getLocation().x - location.x;
		// float dy = rn.getLocation().y - location.y;
		// return (float) Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	public List<AStarNode> getNeighbors() {
		List<AStarNode> retval = new ArrayList<AStarNode>();
		for (int x = location.x - 1; x <= location.x + 1; x++) {
			if (x >= 0 && x < allNodes.size() - 1) {
				for (int y = location.y - 1; y <= location.y + 1; y++) {
					if (y >= 0 && y < allNodes.get(x).size() - 1 &&
							(x != location.x || y != location.y)) {
						retval.add((AStarNode) allNodes.get(x).get(y));
					}
				}
			}
		}
		return retval;
	}

	public Point getLocation() {
		return location;
	}

	public void setCoordinates(Point coordinates) {
		this.location = coordinates;
	}

	public float getHeight() {
		return height;
	}

	public void setValue(float value) {
		this.height = value;
	}
}
