package com.skyline.transportation.engine;

import javax.vecmath.*;

import com.skyline.transportation.model.*;

/**
 * Represents a Road and Query module, as described in Parish & Muller.
 * 
 * @author philippd
 * 
 */
public class Module {
	private ControlPoint startPoint; // StartPoint will very often contain a
										// reference to a ControlPoint that is
										// already in the RoadQuad.
	private Vector2d direction = new Vector2d();
	private RoadType roadType;
	private boolean isValid = true;

	// private double length;

	public Module(ControlPoint startPoint, Vector2d direction, RoadType roadType) {
		this.startPoint = startPoint;
		this.direction = new Vector2d(direction);
		this.direction.normalize();
		this.direction.scale(roadType.getSegmentLength());
		this.roadType = roadType;
		this.isValid = true;
	}

	/**
	 * Create a new module with the same startPoint (by reference), direction
	 * (by value) and roadType as the specified module. module length will be
	 * initialized to the default length for the road Type, and the module will
	 * be marked as "valid".
	 * 
	 * @param m
	 */
	public Module(Module m) {
		this(m.startPoint, new Vector2d(m.direction), m.roadType);
	}

	public boolean equals(Object o) {
		if (o instanceof Module) {
			Module m = (Module) o;
			return equals(m);
		}
		return false;
	}

	public boolean equals(Module m){
		return startPoint.equals(m.startPoint) && direction.epsilonEquals(m.direction, ControlPoint.EPSILON);
	}
	public ControlPoint getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(ControlPoint startPoint) {
		this.startPoint = startPoint;
	}

	public Vector2d getDirection() {
		return direction;
	}

	public void setDirection(Vector2d direction) {
		this.direction = direction;
	}

	public RoadType getRoadType() {
		return roadType;
	}

	public void setRoadType(RoadType roadType) {
		this.roadType = roadType;
	}

	public void setLength(double length) {
		this.direction.normalize();
		this.direction.scale(length);
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
