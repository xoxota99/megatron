package com.skyline.transportation.engine.rules;

import java.util.*;

import com.skyline.model.*;
import com.skyline.transportation.engine.*;

/**
 * LocalConstraint that fixes road segments that stray into water. Roads are
 * rotated to fit inside a legal area: a road to the coast bends around the
 * coastline like a coastal road. Highways are allowed to cross an illegal area
 * of a certain distance: a highway approaching a limited span of water will
 * cross over it like a bridge. When a road goes into illegal region (ie:
 * water), the system tries to curve it into the legal area within a certain
 * range either side of the original direction. If it cannot be done, the road
 * splits into a T-junction.
 */
public class WaterRule implements RoadRule {
	private WorldState worldState;

	public WaterRule(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = new ArrayList<Module>();
		
		return retval;
	}

}
