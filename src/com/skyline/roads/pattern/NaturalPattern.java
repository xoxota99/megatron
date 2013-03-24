package com.skyline.roads.pattern;

import javax.vecmath.*;

import com.skyline.roads.*;
import com.skyline.roads.util.*;

/**
 * Adapt branch modules to natural street pattern, following population density.
 * 
 * @author philippd
 * 
 */
public class NaturalPattern implements Pattern {
	private RoadMap roadMap;
	private ValueMap2d<Double> densityMap;

	public NaturalPattern(RoadMap roadMap, ValueMap2d<Double> densityMap) {
		this.roadMap = roadMap;
		this.densityMap=densityMap;
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
		Vector2d baseVec = new Vector2d(roadMap.getVertex((int) pModule.originVertexId).x - roadMap.getVertex((int) pModule.delay).x, // ???
																																		// paramIndex
																																		// 0
																																		// is
																																		// used
																																		// weirdly
																																		// here.
																																		// Should
																																		// be
																																		// delay.
				roadMap.getVertex((int) pModule.originVertexId).y - roadMap.getVertex((int) pModule.delay).y); // again.
																												// Strange
																												// use
																												// of
																												// zero.
																												// I
																												// don't
																												// know
																												// what
																												// this
																												// is.

		baseVec.normalize();

		// set direction vectors to values
		sModule1.direction.set(baseVec.y, -baseVec.x); // + 90deg CW
		sModule2.direction.set(baseVec.x, baseVec.y); // 0deg
		sModule3.direction.set(-baseVec.y, baseVec.x); // +180 deg CW

		// adjust to follow population density
		double basex = roadMap.getVertex((int) pModule.originVertexId).x;
		double basey = roadMap.getVertex((int) pModule.originVertexId).y;

		sModule1.direction = densitySampler(basex, basey, sModule1.direction.x, sModule1.direction.y);
		sModule2.direction = densitySampler(basex, basey, sModule2.direction.x, sModule2.direction.y);
		sModule3.direction = densitySampler(basex, basey, sModule3.direction.x, sModule3.direction.y);

	}

	/**
	 * calculate direction with largest accumulated population density from a
	 * given point.
	 * 
	 * @param basex
	 * @param basey
	 * @param x
	 * @param y
	 */
	private Vector2d densitySampler(double baseX, double baseY, double dirX, double dirY) {
		Vector2d retval = new Vector2d();
		// some variables
		double baseAng = VecMath.angle(dirX, dirY);
		double densitySum = 0.0;
		double density, tempDensitySum;

		// search for direction with largest accumulated density (angle)
		for (int i = -Config.ROAD_SAMPLING_RATE; i < Config.ROAD_SAMPLING_RATE; i++) {
			tempDensitySum = 0.0;

			// calculate new direction
			Vector2d testVec = VecMath.rotateNormalizedVector(baseAng + i * (Config.ROAD_MAX_TURNING_ANGLE / (double) Config.ROAD_SAMPLING_RATE));

			// search for direction with largest accumulated density (length)
			for (int j = 0; j < Config.ROAD_SAMPLING_RATE; j++) {
				// calculate end point and new density sum
				Point2d sample = VecMath.calculatePoint(j * Config.ROAD_SAMPLING_LENGTH, baseX, baseY, testVec.x, testVec.y);
				density = densityMap.getInterpolatedValue(sample.x, sample.y);
				if (density != 0)
					tempDensitySum += density;
				else
					tempDensitySum += Config.DEFAULT_POPULATION_DENSITY;
			}
			// scale sum
			if (i == 0)
				tempDensitySum *= 1.2;
			// if calculated density sum is larger than previous one we have
			// found a new direction
			if (tempDensitySum > densitySum) {
				retval.set(testVec);
				densitySum = tempDensitySum;
			}
		}
		return retval;
	}

}
