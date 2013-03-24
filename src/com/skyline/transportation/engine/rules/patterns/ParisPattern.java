package com.skyline.transportation.engine.rules.patterns;

import java.util.*;

import com.skyline.transportation.engine.*;

/**
 * Paris (or radial) rule: The highways follow radial tracks around a center
 * that can be either calculated from the input data or set manually.
 * 
 * @author philippd
 * 
 */

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */

public class ParisPattern extends BasicPattern {

	// Branch Pattern: This is actually very similar to the New York Pattern.
	// Imagine the NY pattern on a grid of Cartesian coordinates, and the Paris
	// pattern on a grid of polar coordinates.
	
	// Length Pattern: 

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = super.process(module);

		// do some stuff...

		return retval;
	}

}
