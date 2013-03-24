package com.skyline.transportation.model;

import java.util.*;

import com.skyline.model.zoning.*;
import com.skyline.transportation.model.quad.*;

/**
 * A RoadSegment represents the portion of road between two ControlPoints.
 * 
 * @author philippd
 * 
 */
public class RoadSegment {

	private RoadType roadType;
	private ControlPoint startPoint;
	private ControlPoint endPoint;
	private Set<Block> blocks = new TreeSet<Block>();
	private RoadTreeNode node; // The node that contains this ControlPoint.

	public RoadSegment() {
		this(0, 0, 0, 0);
	}

	public RoadSegment(double x1, double y1, double x2, double y2) {
		this(new ControlPoint(x1, y1), new ControlPoint(x2, y2));
	}

	public RoadSegment(ControlPoint startPoint, ControlPoint endPoint) {
		this.startPoint = startPoint;
		this.startPoint.addRoadSegment(this);
		this.endPoint = endPoint;
		this.endPoint.addRoadSegment(this);
	}

	public ControlPoint getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(ControlPoint startPoint) {
		if (this.startPoint != null) {
			this.startPoint.removeRoadSegment(this);
		}
		this.startPoint = startPoint;
		this.startPoint.addRoadSegment(this);
	}

	public ControlPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(ControlPoint endPoint) {
		if (this.endPoint != null) {
			this.endPoint.removeRoadSegment(this);
		}
		this.endPoint = endPoint;
		this.endPoint.addRoadSegment(this);
	}

	public boolean equals(Object o) {
		if (o instanceof RoadSegment) {
			RoadSegment seg = ((RoadSegment) o);
			return (seg.getStartPoint().equals(this.startPoint) && seg.getEndPoint().equals(this.endPoint))
					|| (seg.getStartPoint().equals(this.endPoint) && seg.getEndPoint().equals(this.startPoint));
		}
		return false;
	}

	public RoadTreeNode getNode() {
		return node;
	}

	public void setNode(RoadTreeNode node) {
		this.node = node;
	}

	public RoadType getRoadType() {
		return roadType;
	}

	public void setRoadType(RoadType roadType) {
		this.roadType = roadType;
	}

}
