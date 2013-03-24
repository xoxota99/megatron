package com.skyline.transportation.engine.rules;

import java.util.*;

import com.skyline.model.*;
import com.skyline.transportation.engine.*;

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */
/**
 * The RoadPatternRule examines the PopulationCenters defined in WorldState.
 * Each PopulationCenter has a GridType that specifies what types of Road
 * Patterns should be used.
 * 
 * The combination of all Population Centers generates a Road Pattern Map that
 * describes Road Patterns for the entire city.
 * 
 * (see "Procedural Modeling of Cities", section 3.2.2)
 * 
 * @author philippd
 * 
 */
public class RoadPatternRule implements RoadRule {

	private WorldState worldState;

	public RoadPatternRule(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = new ArrayList<Module>();

		return retval;
	}

}
