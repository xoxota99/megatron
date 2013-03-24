package com.skyline.transportation.engine;

import java.util.*;

import javax.vecmath.*;

import com.skyline.model.*;
import com.skyline.population.model.*;
import com.skyline.transportation.engine.rules.*;
import com.skyline.transportation.model.*;
import com.skyline.transportation.model.quad.*;

/*
 * Code mostly derived from "Procedural City Generation Tool" by Kevin Wu
 */
public class RoadEngine {
	private Set<Module> modules = new TreeSet<Module>();
	private List<RoadRule> globals = new ArrayList<RoadRule>();
	private List<RoadRule> locals = new ArrayList<RoadRule>();
	private WorldState worldState;
	private long seed = 0L;

	public RoadEngine(WorldState worldState) {
		this.worldState = worldState;
		this.seed = worldState.getRoadSeed();
		this.globals = initGlobals(worldState);
		this.locals = initLocals(worldState);
		this.modules = initModules(worldState);
	}

	/**
	 * Create the "seeds" from which our road network will grow. Road Seeds are
	 * placed randomly within the bounds of existing population centers.
	 * Population Centers with higher "volume" (Density * Radius) will contain
	 * relatively more Road Seeds than those with smaller "volume".
	 * 
	 * @param worldState
	 * @return
	 */
	private Set<Module> initModules(WorldState worldState) {
		Set<Module> retval = new TreeSet<Module>();
		Random r = new Random(this.seed);
		for (PopulationCenter popCent : worldState.getPopulationCenters()) {
			double vol = Math.PI * popCent.getRadius() * popCent.getRadius() * popCent.getDensity();
			int seedCount = Math.max(2, Math.min((int) (vol / 5), 5)); // between
																		// 2 and
																		// 5
																		// highways.
			for (int i = 0; i < seedCount;) {
				double rad = r.nextDouble() * popCent.getRadius(); // distance
																	// from
																	// center.
				double theta = r.nextDouble() * Math.PI * 2; // Angle (in
																// radians)

				// now convert that to X/Y coordinates, smart guy.
				double x = rad * Math.cos(theta) + popCent.x;
				double y = rad * Math.sin(theta) + popCent.y;
				if (x >= 1 && x < worldState.getSize()
						&& y >= 1 && y < worldState.getSize()) {
					// we're on the map. plonk down a Module.
					++i;
					ControlPoint cp = new ControlPoint(x, y);
					Vector2d vec = new Vector2d(x - popCent.x, y - popCent.y);
					vec.normalize();
					vec.scale(RoadType.HIGHWAY.getSegmentLength()); // This is a
																	// Highway.

					Module m = new Module(cp, vec, RoadType.HIGHWAY);
					retval.add(m);
				}
			}

		}
		return retval;
	}

	private List<RoadRule> initLocals(WorldState worldState2) {
		List<RoadRule> retval = new ArrayList<RoadRule>();
		retval.add(new IntersectionRule(worldState));
		retval.add(new WaterRule(worldState));
		retval.add(new ElevationRule(worldState));
		return retval;
	}

	/**
	 * Initialize the set of Global "goals".
	 * 
	 * @return
	 */
	private List<RoadRule> initGlobals(WorldState worldState) {
		List<RoadRule> retval = new ArrayList<RoadRule>();
		retval.add(new RoadPatternRule(worldState));
		retval.add(new PopulationDensityRule(worldState));
		return retval;
	}

	/**
	 * Perform a single pass through the current list of Modules.
	 */
	// TODO: What about when one rule (such as RoadPatterns) conflicts with
	// another rule (such as PopDensity)? There should be a way for us to weight
	// the outcome of multiple rules, to come up with a final solution for the
	// Module.
	public void tick() {
		Iterator<Module> it = modules.iterator();
		RoadQuad roadQuad = worldState.getRoadQuad();
		Set<Module> newModules = new TreeSet<Module>();
		while (it.hasNext()) {
			Module m = it.next();
			/*
			 * From Wu: Given a module, the global goal looks at the direction
			 * the road is currently growing and generates an ideal direction
			 * based on that and road patterns. The global goal also determines
			 * whether the road branches or not.
			 */
			for (RoadRule roadRule : globals) {
				List<Module> branches = roadRule.process(m);
				for (Module branch : branches) {
					newModules.add(branch);
				}
			}
			/*
			 * The local constraint checks the result from global goals to see
			 * if it is valid and whether it intersects with any existing roads.
			 */
			for (RoadRule roadRule : locals) {
				List<Module> branches = roadRule.process(m);
				for (Module branch : branches) {
					newModules.add(branch);
				}
			}

			if (m != null && m.isValid()) {
				/*
				 * If the module passes the local constraint check, a road
				 * segment is created between the starting point from the module
				 * to the end point represented by the starting point plus the
				 * unit length direction vector multiplied by the length.
				 */
				ControlPoint start = m.getStartPoint();
				ControlPoint end = new ControlPoint(m.getStartPoint().x + m.getDirection().x, m.getStartPoint().y + m.getDirection().y);

				RoadSegment seg = new RoadSegment(start, end);
				roadQuad.put(seg);

				/*
				 * Once that road segment is placed into the quadtree, the
				 * module is removed from the list, and a new one is created
				 * with the previous end point being the new starting point.
				 */
				m.setStartPoint(end); // reuse the existing Module instance.
				m.setLength(m.getRoadType().getSegmentLength()); // reinitialize
																	// the
																	// length.
				newModules.add(m);
			}

			it.remove(); // I don't think we technically need to do this.

		}
		this.modules = newModules;
	}

}
