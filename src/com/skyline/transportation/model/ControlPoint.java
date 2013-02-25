package com.skyline.transportation.model;

import java.util.*;

import javax.vecmath.*;

import com.skyline.transportation.model.quad.*;

public class ControlPoint extends Point2d{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2160252164856829068L;
	private static double SIGMA = 0.0001; // for determining "equality".
//	private Set<RoadSegment> segments = new TreeSet<RoadSegment>();
//	private static RoadTree roadTree = RoadTree.getInstance();
	private RoadTreeNode node; // The node that contains this ControlPoint.

	/**
	 * Obtain an instance of {@link ControlPoint} at the specified coordinates.
	 * If one exists already, the existing instance will be returned. Otherwise
	 * a new instance will be created.
	 * 
	 * @param x
	 *            x-coordinate of the desired ControlPoint (between 0 and 1)
	 * @param y
	 *            y-coordinate of the desired ControlPoint (between 0 and 1)
	 * @returns a ControlPoint.
	 */
	/*
	 * TODO: If we start to leak memory, look here first. We are creating
	 * points, but not destroying them.
	 */
	public static ControlPoint getInstance(double x, double y) {
		assert x < 1 && x >= 0 : "x-coordinate must be between 0 and 1";
		assert y < 1 && y >= 0 : "y-coordinate must be between 0 and 1";
		ControlPoint retval = roadTree.get(x, y);
		if (retval == null) {
			retval = new ControlPoint(x, y);
			roadTree.put(retval);
		}
		return retval;
	}

	/**
	 * Obtain an instance of {@link ControlPoint} at the specified coordinates.
	 * If an instance exists within distance of the specified coordinates, that
	 * instance will be returned.
	 * 
	 * @param x
	 *            x-coordinate of the desired ControlPoint (between 0 and 1)
	 * @param y
	 *            y-coordinate of the desired ControlPoint (between 0 and 1)
	 * @param distance
	 *            radius to search for existing ControlPoints (between 0 and 1).
	 * @return
	 */
	public static ControlPoint getInstance(final double x, final double y, final double distance) {
		assert x < 1 && x >= 0 : "x-coordinate must be between 0 and 1";
		assert y < 1 && y >= 0 : "y-coordinate must be between 0 and 1";
		assert distance < 1 && distance >= 0 : "distance must be between 0 and 1";
		List<ControlPoint> l = roadTree.get(x, y, distance);
		if (l == null || l.isEmpty()) {
			ControlPoint cp = new ControlPoint(x, y);
			roadTree.put(cp);
			return cp;
		} else if (l.size() == 1) {
			return l.get(0);
		} else {
			// results are not sorted.
			Collections.sort(l, new Comparator<ControlPoint>() {

				@Override
				public int compare(ControlPoint o1, ControlPoint o2) {
					double dx1, dx2, d1, dy1, dy2, d2;
					dx1 = o1.x - x;
					dx2 = o2.x - x;
					dy1 = o1.y - y;
					dy2 = o2.y - y;
					d1 = Math.sqrt((dx1 * dx1) + (dy1 * dy1));
					d2 = Math.sqrt((dx2 * dx2) + (dy2 * dy2));
					return (d1 - d2 < 0 ? -1 : (d1 - d2 > 0 ? 1 : 0));
				}
			});
			return l.get(0);
		}

	}

	private ControlPoint(double x, double y) {
		assert x < 1 && x >= 0 : "x-coordinate must be between 0 and 1";
		assert y < 1 && y >= 0 : "y-coordinate must be between 0 and 1";
		this.x = x;
		this.y = y;
	}

	private ControlPoint() {
		this(0, 0);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void addRoadSegment(RoadSegment roadSegment) {
		this.segments.add(roadSegment);
	}

	public void removeRoadSegment(RoadSegment roadSegment) {
		this.segments.remove(roadSegment);
	}

	/**
	 * Two ControlPoints are "equal" if their X and Y coordinates are within the
	 * SIGMA distance of eachother (0.00001)
	 */
	public boolean equals(Object o) {
		if (o instanceof ControlPoint) {
			ControlPoint cp = (ControlPoint) o;
			return Math.abs(cp.x - x) < SIGMA && Math.abs(cp.y - y) < SIGMA;
		}
		return false;
	}

	public RoadTreeNode getNode() {
		return node;
	}

	public void setNode(RoadTreeNode node) {
		this.node = node;
	}

	public Set<RoadSegment> getSegments() {
		return segments;
	}

}
