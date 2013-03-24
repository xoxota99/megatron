package com.skyline.transportation.model;

import java.util.*;

import javax.vecmath.*;

import com.skyline.transportation.model.quad.*;

/**
 * A node in the graph of road segments.
 * 
 * @author philippd
 * 
 */
public class ControlPoint extends Point2d {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2160252164856829068L;
	public static double EPSILON = 0.00001; // for determining "equality".
	private Set<RoadSegment> segments = new TreeSet<RoadSegment>();
	// private static RoadQuad roadTree = RoadQuad.getInstance();
	private RoadTreeNode node; // The node that contains this ControlPoint.

	public ControlPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public ControlPoint() {
		this(0, 0);
	}

	public ControlPoint(Point2d p) {
		this(p.x, p.y);
	}

	public void addRoadSegment(RoadSegment roadSegment) {
		this.segments.add(roadSegment);
	}

	public void removeRoadSegment(RoadSegment roadSegment) {
		this.segments.remove(roadSegment);
	}

	/**
	 * Two ControlPoints are "equal" if their X and Y coordinates are within the
	 * EPSILON distance of eachother (0.00001)
	 */
	public boolean equals(Object o) {
		if (o instanceof ControlPoint) {
			return equals((ControlPoint) o);
		}
		return false;
	}

	public boolean equals(ControlPoint cp) {
		return epsilonEquals(cp, EPSILON);
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
