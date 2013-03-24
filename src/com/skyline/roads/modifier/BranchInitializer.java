package com.skyline.roads.modifier;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roads.*;
import com.skyline.roads.util.*;

/**
 * L-System parameter modifier. Sets initial parameters of branch modules. That
 * is mainly the direction in which the later to be added road/street segments
 * should point. Here the current street pattern is used.
 * 
 * @author philippd
 * 
 */
public class BranchInitializer implements Modifier {

	private long seed;
	private Random random;
	private ValueMap2d<Double> heightMap;
	private ValueMap2d<Double> densityMap;
	private RoadMap roadMap;
	private ValueMap2d<Byte> patternMap;

	public BranchInitializer(RoadMap roadMap, ValueMap2d<Double> heightMap, ValueMap2d<Double> densityMap, ValueMap2d<Byte> patternMap, long seed) {
		this.roadMap = roadMap;
		this.seed = seed;
		this.random = new Random(this.seed);
		this.heightMap = heightMap;
		this.densityMap = densityMap;
		this.patternMap = patternMap;
	}

	@Override
	public void modify(int i, ModuleString pred, ModuleString succ) {
		// get predecessor module
		Module pModule = pred.get(i);

		// get successor modules
		Module sModule1 = succ.get(i);
		Module sModule2 = succ.get(i + 1);
		Module sModule3 = succ.get(i + 2);

		// set delay values
		sModule1.delay = Config.ROAD_BRANCH_DELAY + 1;
		sModule2.delay = 0;
		sModule3.delay = Config.ROAD_BRANCH_DELAY;

		// set current vertex id as origin id for successors
		sModule1.originVertexId = pModule.originVertexId;
		sModule2.originVertexId = pModule.originVertexId;
		sModule3.originVertexId = pModule.originVertexId;

		// set type
		if (pModule.mainSegmentType == SegmentType.ROAD) {
			// roads may branch into streets.
			double rval1 = random.nextDouble();
			double rval2 = random.nextDouble();
			if (rval1 > Config.ROAD_BRANCH_POSSIBILITY) {
				sModule1.offshootSegmentType = SegmentType.STREET;
			} else {
				sModule1.offshootSegmentType = SegmentType.ROAD;
			}

			sModule2.offshootSegmentType = SegmentType.ROAD;

			if (rval2 > Config.ROAD_BRANCH_POSSIBILITY) {
				sModule3.offshootSegmentType = SegmentType.STREET;
			} else {
				sModule3.offshootSegmentType = SegmentType.ROAD;
			}
		} else {
			// streets can only branch into streets.
			sModule1.offshootSegmentType = SegmentType.STREET;
			sModule2.offshootSegmentType = SegmentType.STREET;
			sModule3.offshootSegmentType = SegmentType.STREET;
		}

		// depending on the road pattern at the current vertex modify
		// direction vectors
		int patternMapVal = patternMap.getRealValue(
				roadMap.getVertex(pModule.originVertexId).x,
				roadMap.getVertex(pModule.originVertexId).y);

		// pre init
		if (!Config.ROAD_PATTERN_SENSITIVE) {
			// #BYREF (checked)
			naturalPattern(i, pred, succ);
		}

		// (legacy) patternMapVal contains one of three values, 255
		// (ManhattenPattern),
		// 178
		// (SANFRANCISCO), or 0 (NATURAL). Use one of these patterns to
		// align
		// the roads.

		// pattern
		if (patternMapVal == Config.MANHATTEN_PATTERN_COLOR_KEY) {
			// #BYREF (checked)
			manhattenPattern(i, pred, succ);
		} else if (patternMapVal == Config.SAN_FRANCISCO_PATTERN_COLOR_KEY) {
			// #BYREF
			sanFranciscoPattern(i, pred, succ);
		} else {
			// #BYREF (checked)
			naturalPattern(i, pred, succ);
		}
	}

}
