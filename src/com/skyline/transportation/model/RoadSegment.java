package com.skyline.transportation.model;

import java.util.*;

import com.skyline.model.zoning.*;
import com.skyline.transportation.model.quad.*;

/**
 * A RoadSegment represent the portion of road between two control points.
 * 
 * @author philippd
 * 
 */
public class RoadSegment {

	private ControlPoint startPoint;
	private ControlPoint endPoint;
	private Set<Block> blocks = new TreeSet<Block>();
	private static RoadTree roadTree = RoadTree.getInstance();

	public static RoadSegment getInstance(double x1, double y1, double x2, double y2) {
		assert x1 < 1 && x2<1 && x1>0 && x2>0: "x-coordinates must be between 0 and 1";
		assert y1 < 1 && y2<1 && y1>0 && y2>0: "y-coordinates must be between 0 and 1";
		RoadSegment retval = roadTree.getRoadSegment(x1,y1,x2,y2);
		if (retval == null) {
			retval = new RoadSegment(x1,y1,x2,y2);			
			roadTree.put(retval);
		}
		return retval;
	}

	public static RoadSegment getInstance(ControlPoint startPoint, ControlPoint endPoint) {
		return getInstance(startPoint.getX(),startPoint.getY(),endPoint.getX(),endPoint.getY());
	}

	private RoadSegment() {
		this(0, 0, 0, 0);
	}

	private RoadSegment(double x1, double y1, double x2, double y2) {
		this(ControlPoint.getInstance(x1, y1), ControlPoint.getInstance(x2, y2));
	}

	private RoadSegment(ControlPoint startPoint, ControlPoint endPoint) {
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
}
