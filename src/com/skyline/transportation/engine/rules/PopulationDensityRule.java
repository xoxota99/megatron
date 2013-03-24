package com.skyline.transportation.engine.rules;

import java.util.*;

import com.skyline.model.*;
import com.skyline.transportation.engine.*;
import com.skyline.transportation.model.*;

/*
 * See "Procedural Modeling of Cities", section 3.2.1
 */
/**
 * Highways connect centers of population. To find the next population center,
 * every highway road-end shoots a number of rays radially within a preset
 * radius and angle from initial direction. Along this ray, samples of the
 * population density are taken from the WorldState population density map. The
 * population at every sample point on the ray is weighted with the inverse
 * distance to the Module StartPoint and summed up. The direction with the
 * largest sum is chosen for continuing the growth.
 * 
 * @author philippd
 * 
 */
public class PopulationDensityRule implements RoadRule {

	private static final double SAMPLE_RADIUS = 10;
	private static final double SAMPLE_ARC = Math.PI/2;	//90 degrees.
	private WorldState worldState;

	public PopulationDensityRule(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = new ArrayList<Module>();
		if(module.getRoadType()==RoadType.HIGHWAY){
			//This rule really only applies to highways
			
		}
		return retval;
	}

}
