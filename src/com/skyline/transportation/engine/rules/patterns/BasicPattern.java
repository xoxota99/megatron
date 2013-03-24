package com.skyline.transportation.engine.rules.patterns;

import java.util.*;

import com.skyline.transportation.engine.*;
import com.skyline.transportation.engine.rules.*;

/**
 * BasicPattern is the simplest possible rule. There is no superimposed pattern and all
 * roads follow population density. This may also be referred to as the
 * natural growth of a transportation network. Mainly older parts of cities show
 * such patterns. All other rules are based on restrictions of this rule by
 * narrowing the choices of branch angles and road segment length.
 * 
 * @author philippd
 * 
 */

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */

// implements RoadRule? Not sure...
public class BasicPattern implements RoadRule {

	//Orientation: Roads will orient according to population Density (similar to PopDensityRule).
	
	//Branching: ???
	
	//Length: Segments have a max and min length, but otherwise are not constrained.
	
	@Override
	public List<Module> process(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

}
