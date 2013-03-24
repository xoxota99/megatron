package com.skyline.roads.pattern;

import javax.vecmath.*;

import com.skyline.roads.*;

/**
 * 
 * Adapt branch modules to ManhattenPattern street pattern.
 * 
 * @author philippd
 * 
 */
public class ManhattenPattern implements Pattern {
	private RoadMap roadMap;

	public ManhattenPattern(RoadMap roadMap) {
		this.roadMap = roadMap;
	}

	@Override
	public void adapt(int i, ModuleString pred, ModuleString succ) {

		// get predecessor module
		Module pModule = pred.get(i);
		// get successor modules
		Module sModule1 = succ.get(i);
		Module sModule2 = succ.get(i + 1);
		Module sModule3 = succ.get(i + 2);
		// calculate normalized base vector, that is the vector from the
		// origin vertex of the predecessor to the current vertex
		Vector2d baseVec = new Vector2d(roadMap.getVertex((int) pModule.originVertexId).x - roadMap.getVertex((int) pModule.delay).x,
				roadMap.getVertex((int) pModule.originVertexId).y - roadMap.getVertex((int) pModule.delay).y); // #
																												// Weird
																												// use
																												// of
																												// [0].
		baseVec.normalize();
		// set direction vectors to values
		if ((sModule1.offshootSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE) {
			sModule1.direction.set(baseVec.y, -baseVec.x);
		}
		if ((sModule2.mainSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE) {
			sModule2.direction.set(baseVec.x, baseVec.y);
		}
		if ((sModule3.offshootSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE) {
			sModule3.direction.set(-baseVec.y, baseVec.x);
		}
	}

}
