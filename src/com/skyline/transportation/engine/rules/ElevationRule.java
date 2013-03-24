package com.skyline.transportation.engine.rules;

import java.util.*;

import com.skyline.model.*;
import com.skyline.transportation.engine.*;

/**
 * LocalConstraint that prevents roads from climbing mountains. Road segments
 * encountering excessive slope will be rotated until a better
 * slope is found. If no improved slope can be found, the segment is removed.
 */
/*
 * TODO: This may need to be altered or replaced, for implementation of the SANFRANCISCO GridType.
 */
public class ElevationRule implements RoadRule {

	private WorldState worldState;

	public ElevationRule(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public List<Module> process(Module module) {
		// at the moment, do nothing. There are no constraints, all is
		// permitted.
		return new ArrayList<Module>();
	}

}
