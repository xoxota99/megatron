package com.skyline.roads.pattern;

import javax.vecmath.*;

import com.skyline.roads.*;
import com.skyline.roads.util.*;

/**
 * Adapt branch modules to San Francisco street pattern. (Follows terrain
 * height).
 * 
 * @author philippd
 * 
 */
public class SanFranciscoPattern implements Pattern {
	private RoadMap roadMap;
	private ValueMap2d<Double> heightMap;

	public SanFranciscoPattern(RoadMap roadMap, ValueMap2d<Double> heightMap) {
		this.roadMap = roadMap;
		this.heightMap=heightMap;
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
				roadMap.getVertex((int) pModule.originVertexId).y - roadMap.getVertex((int) pModule.delay).y);

		baseVec.normalize();

		// set direction vectors to values
		sModule1.direction.set(baseVec.y, -baseVec.x);
		sModule2.direction.set(baseVec.x, baseVec.y);
		sModule3.direction.set(-baseVec.y, baseVec.x);

		// adjust to follow gradient
		double basex = roadMap.getVertex((int) pModule.originVertexId).x;
		double basey = roadMap.getVertex((int) pModule.originVertexId).y;

		if ((sModule1.offshootSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE)
			sModule1.direction = followSlope(basex, basey, sModule1.direction.x, sModule1.direction.y);
		if ((sModule2.offshootSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE)
			sModule2.direction = followSlope(basex, basey, sModule2.direction.x, sModule2.direction.y);
		if ((sModule3.offshootSegmentType != SegmentType.ROAD) || Config.ROAD_PATTERN_SENSITIVE)
			sModule3.direction = followSlope(basex, basey, sModule3.direction.x, sModule3.direction.y);

	}

	/**
	 * calculate direction with smallest slope from a given point.
	 * 
	 * @param origin
	 * @param dir
	 */
	private Vector2d followSlope(double baseX, double baseY, double dirX, double dirY) {
		// some variables
		Vector2d retval = new Vector2d();
		double baseAng = VecMath.angle(dirX, dirY);
		// double densitySum = 0.0;
		double minDeltaZ = 999999.0;
		double zDelta;
		double tx = 0d, ty = 0d, baseZ, ez;
		// #TODO: Ensure "dir" is NOT normalized. We're sampling at a particular
		// spot. Not "unity".
		// z1 is z-value at base
		baseZ = heightMap.getInterpolatedValue(dirX, dirY);
		baseZ = (float) (baseZ * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));
		// search for direction with minimal slope
		for (int i = -Config.ROAD_SAMPLING_RATE; i < Config.ROAD_SAMPLING_RATE; i++) {
			// calculate new direction
			Vector2d sDir = VecMath.rotateNormalizedVector(baseAng + i * (Config.ROAD_MAX_TURNING_ANGLE / (double) Config.ROAD_SAMPLING_RATE));
			// calculate end point and new density sum
			Point2d sPoint = VecMath.calculatePoint(Config.ROAD_SAMPLING_LENGTH, baseX, baseY, sDir.x, sDir.y);
			// ez is z-value at calculated point
			ez = heightMap.getInterpolatedValue(sPoint.x, sPoint.y);
			ez = (float) (ez * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT)); // normalize
			// to
			// the
			// range.
			// calculate heightDiff

			zDelta = VecMath.slope(baseX, baseY, baseZ, sPoint.x, sPoint.y, ez);

			// scale sum
			if (i == 0)
				zDelta /= 1.2; // # For the first sample, we "shrink" the
										// gradient (to make it more attractive)
										// Why? Maybe this is so that we always have someplace to go.

			// if calculated gradient is smaller than minGradient we've found a
			// new direction
			if (zDelta < minDeltaZ) {
				retval.x = tx;
				retval.y = ty;
				minDeltaZ = zDelta;
			}
		}
		return retval;
	}

}
