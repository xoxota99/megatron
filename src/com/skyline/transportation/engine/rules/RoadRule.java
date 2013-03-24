package com.skyline.transportation.engine.rules;

import java.util.*;

import com.skyline.transportation.engine.*;

/**
 * Represents a Global Goal, or Local Constraint (see
 * "Procedural Modeling of Cities", section 3.2, 3.3)
 * 
 * @author philippd
 * 
 */
public interface RoadRule {

	/**
	 * Process a module, updating it in place. If the module should branch,
	 * create one or more new modules, and return them in a List. If the module
	 * does not branch, return an empty List.
	 * 
	 * @param module
	 * @param worldState
	 * @return a list of one or more modules with the same startPoint.
	 */
	public List<Module> process(Module module);

}
