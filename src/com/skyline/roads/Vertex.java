package com.skyline.roads;

import javax.vecmath.*;

/**
 * Stores a vertex representing a connection between multiple Edges, or Road
 * Segments.
 * 
 * @author philippd
 * 
 */
public class Vertex extends Point3d {

	/**
	 * 
	 */
	private static final long serialVersionUID = -354561527271179291L;
	private static final double EPSILON = 0.000001d; // Closer to eachother than
														// this, and two points
														// will be considered
														// equal.
	
	public int belongsTo = -1; // For Curbs, the index of the road vertex
								// that this curb belongs to.

	public Vertex() {
		super();
	}

	public Vertex(double x, double y, double z, int belongsTo) {
		super(x, y, z);
		this.belongsTo = belongsTo;
	}

	public Vertex(double x, double y, double z) {
		super(x, y, z);
	}

	public Vertex(Point3d point) {
		super(point);
	}

	/**
	 * Calculate distance in the XY plane, as if this were a 2D point.
	 * 
	 * @param intersection
	 * @return
	 */
	public double distance(Point2d intersection) {
		double dx = this.x - intersection.x;
		double dy = this.y = intersection.y;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	public boolean equals(Vertex v) {
		return super.epsilonEquals(v, EPSILON);
	}

}
