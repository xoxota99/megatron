package com.skyline.transportation.engine.rules.patterns;

import java.util.*;

import com.skyline.transportation.engine.*;

/**
 * This pattern lets streets and highways follow the route of the least
 * elevation. Roads on different height levels are connected by smaller streets,
 * that follow steepest elevation and are short. This pattern is usually
 * observed in areas with large differences in ground eleva- tion. See figure 6
 * for an example.
 * 
 * @author philippd
 * 
 */

/*
 * See "Procedural Modeling of Cities", section 3.2.2
 */

public class SanFranciscoPattern extends BasicPattern {
	@Override
	public List<Module> process(Module module) {
		List<Module> retval = super.process(module);
		
		//do some stuff...
		
		return retval;
	}

}
