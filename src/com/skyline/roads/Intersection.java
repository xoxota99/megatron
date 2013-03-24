package com.skyline.roads;

import javax.vecmath.*;

/**
 * Represents an intersection with a segment.
 * 
 * @author philippd
 * 
 */
public class Intersection extends Point2d {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1489088072981378224L;
	public int edgeIndex;

	public Intersection(double x, double y, int edgeIndex) {
		super(x, y);
		this.edgeIndex = edgeIndex;
	}

	public Intersection() {
		super();
	}
}
