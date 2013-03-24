package com.skyline.population.model;

import javax.vecmath.*;

import com.skyline.transportation.model.*;

/**
 * A PopulationCenter represents the center of an area of high population. It
 * has X/Y coordinates, a radius, and a density (value from 0-1).
 * 
 * A population center also has a {@link GridType}, that determines the pattern
 * of roads that will serve this portion of the city.
 * 
 * @author philippd
 * 
 */
/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */
public class PopulationCenter extends Point2d {

	/**
	 * 
	 */
	private static final long serialVersionUID = -685238373790925399L;
	private double radius;
	private double density;
	private GridType gridType;

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = Math.min(1, Math.max(0, density));
	}

	public GridType getGridType() {
		return gridType;
	}

	public void setGridType(GridType gridType) {
		this.gridType = gridType;
	}
}
