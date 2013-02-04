package com.megatron.terrain;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.megatron.util.path.*;

/*
 * River carving algorithm, taken mostly from:
 * http://archive.gamedev.net/archive/reference/articles/article2065.html
 */
public class RiverFactory {

	private float waterLevel;
	private float minDepth = 5f;
	private float maxDepth = 20f;
	private Random r;

	public RiverFactory(float waterLevel, long seed) {
		this.waterLevel = waterLevel;
		r = new Random(seed);
	}

	public RiverFactory(float waterLevel, float minDepth, float maxDepth, long seed) {
		this(waterLevel, seed);
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
	}

	/**
	 * Given a terrain heightMap, return a modified heightMap that includes a
	 * river.
	 * 
	 * @param terrain
	 * @param seed
	 * @return
	 */
	public float[] createRiver(float[] terrain, int size) {
		float[] retval = new float[terrain.length];
		System.arraycopy(terrain, 0, retval, 0, terrain.length);

		// determine the path of the river.
		TreeMap<Float, Point> edgeVertices = getOrderedEdgeVertices(terrain, size);
		Point startPoint = findStartPoint(edgeVertices);
		Point endPoint = findEndPoint(edgeVertices, startPoint, size, false);

		float[][] costs = getCosts(terrain, size, startPoint.x, startPoint.y, endPoint.x,endPoint.y);

		RiverNode startNode = null, goalNode = null;
		for (int i = 0; i < retval.length; i++) {
			int x = i % size;
			int y = (i - x) / size;
			RiverNode n = new RiverNode(new Point(x, y), retval[i]);
			if (x == startPoint.x && y == startPoint.y) {
				startNode = n;
			} else if (x == endPoint.x && y == endPoint.y) {
				goalNode = n;
			}
		}
		
		AStarSearch s = new AStarSearch();
		if (startNode != null && goalNode != null) {
			List<AStarNode> res = s.findPath(startNode, goalNode);
			if (res != null) {
				for (AStarNode nd : res) {
					RiverNode rn = (RiverNode) nd;
					int i = (rn.getLocation().y * size) + rn.getLocation().x;
					retval[i] = 0f;
				}
			}
		}
		// cleanup the terrain around the river.

		// carve the riverbed.

		return retval;
	}

	private float[][] getCosts(float[] terrain, int size, int startX, int startY, int endX, int endY){
		float[][] retval = new float[size][size];
		for (int i = 0; i < terrain.length; i++) {
			int x = i % size;
			int y = (i - x) / size;
			int dx = x-startX;
			int dy=y-startY;
			float cost = (float)Math.sqrt((dx*dx)+(dy*dy));
			retval[x][y]=cost;
		}
		return retval;
	}
	private Point findEndPoint(TreeMap<Float, Point> verts, Point startPoint, int size, boolean restrictToOpposite) {
		// StartPoint is in the bottom 1/5 of ordered vertices.
		Point retval = startPoint; // force loop entry.
		while (retval.x == startPoint.x || retval.y == startPoint.y
				|| (Math.abs(retval.x - startPoint.x) < size - 1
				&& Math.abs(retval.y - startPoint.y) < size - 1)) {
			int idx = (int) (r.nextDouble() * verts.size() / 5);
			retval = (Point) verts.values().toArray()[idx];
		}

		return retval;
	}

	private Point findStartPoint(TreeMap<Float, Point> verts) {
		// StartPoint is in the top 3/5 of ordered vertices.
		int bar = (int) (verts.size() * 2 / 5);
		int idx = bar + (int) (r.nextDouble() * verts.size() * 3 / 5);
		return (Point) verts.values().toArray()[idx]; // weird, but okay...
	}

	/**
	 * Return a list of four ordered sets of points. Each set represents the
	 * points for one side of a square map, in ascending order of Y value.
	 * 
	 * @param terrain
	 * @param size
	 * @return
	 */
	private TreeMap<Float, Point> getOrderedEdgeVertices(float[] terrain, int size) {
		TreeMap<Float, Point> retval = new TreeMap<Float, Point>();

		for (int i = 0; i < terrain.length; i++) {
			int x = i % size;
			int y = (i - x) / size;
			if (x == 0 || y == 0 || x == size - 1 || y == size - 1) {
				retval.put(terrain[i], new Point(x, y));
			}
		}

		return retval;

	}
}
