package com.skyline.transportation.model;

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */
public enum GridType {
	/**
	 * BASIC is the simplest possible rule. There is no superimposed pattern and
	 * all roads follow population density. This may also be referred to as the
	 * natural growth of a transportation network. Mainly older parts of cities
	 * show such patterns. All other rules are based on restrictions of this
	 * rule by narrowing the choices of branch angles and road segment length.
	 */
	BASIC,
	/**
	 * NEWYORK follows a given global or local angle and maximal length and
	 * width of a single block. This is the most frequent street pattern
	 * encountered in urban areas, where all highways and streets form
	 * rectangular blocks.
	 */
	NEWYORK,
	/**
	 * In PARIS, The highways follow radial tracks around a center
	 */
	PARIS
}
