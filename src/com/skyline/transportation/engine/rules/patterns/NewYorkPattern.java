package com.skyline.transportation.engine.rules.patterns;

import java.util.*;

import com.skyline.transportation.engine.*;

/**
 * The New York (or checkers) rule follows a given global or local angle and
 * maximal length and width of a single block. This is the most frequent street
 * pattern encountered in urban areas, where all highways and streets form
 * rectangular blocks.
 * 
 * @author philippd
 * 
 */

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */

public class NewYorkPattern extends BasicPattern {

	// Branch Pattern: If a Street branches, it will branch into two children.
	// One moving straight ahead, and the other at 90 degrees. Branches will
	// tend to occur at regular intervals, based on L1 and L2 (see
	// "Length pattern").

	// Length pattern: If a Street is closer to the "grain" of the GridType for
	// the nearest population Center, then it will be of length L1. If it is
	// more perpendicular to that "grain", then it will be of length L2.

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = super.process(module);

		// do some stuff...

		return retval;
	}

}
