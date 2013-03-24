package com.skyline.transportation.engine.rules;

import java.util.*;

import javax.vecmath.*;

import com.skyline.model.*;
import com.skyline.transportation.engine.*;
import com.skyline.transportation.model.*;
import com.skyline.transportation.model.quad.*;

/**
 * Road segments are checked to see if they intersect with existing roads or if
 * they come within a certain distance of an existing ControlPoint: (see
 * CityGen, section 3.4.1, )
 * 
 * @author philippd
 * 
 */
public class IntersectionRule implements RoadRule {

	private WorldState worldState;

	public IntersectionRule(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public List<Module> process(Module module) {
		List<Module> retval = new ArrayList<Module>(); // This Rule will never
														// branch the road.

		// Does this module intersect an existing road segment?
		// We need to do a deep dive into the RoadQuad, and test against all
		// segments (not just local), because a segment could have ends that are
		// very far away.
		RoadQuad rq = worldState.getRoadQuad();

		List<RoadSegment> segs = new ArrayList<RoadSegment>(rq.getAllSegments());
		if (segs.size() > 0) {
			// There was at least one crossing. Adjust this segment, and split
			// the segment that it crosses.
			RoadSegment closest = null;
			Point2d closestIntersection = null;
			double distClosest = 0;
			for (RoadSegment seg : segs) {
				Point2d intersection = intersection(module.getStartPoint().x,
						module.getStartPoint().y,
						module.getDirection().x,
						module.getDirection().y,
						seg.getStartPoint().x,
						seg.getStartPoint().y,
						seg.getEndPoint().x,
						seg.getEndPoint().y);
				if (intersection != null) {
					// intersection! (at last).
					// now get the distance from the intersection point to the
					// startPoint (easy).
					double dist = intersection.distance(module.getStartPoint());
					if (closest == null || dist < distClosest) {
						closest = seg;
						closestIntersection = intersection;
						distClosest = dist;
					}
				}
			}
			if (closest != null) {
				// now we have the closest crossing segment, and intersection.
				// split the existing segment.
				ControlPoint newEndPoint = new ControlPoint(closestIntersection);
				RoadSegment seg1 = new RoadSegment(closest.getStartPoint(), newEndPoint);
				RoadSegment seg2 = new RoadSegment(newEndPoint, closest.getEndPoint());
				// delicately sever the existing segment from the Quad without
				// destroying the universe.
				rq.remove(closest, false);

				// add back the two replacement segments.
				rq.put(seg1);
				rq.put(seg2);

				// set the direction and length of the module so the endPoint
				// "lands" on the new ControlPoint. (this is hacky).
				module.setDirection(new Vector2d(newEndPoint.x - module.getStartPoint().x, newEndPoint.y - module.getStartPoint().y));
			} else {
				// TODO: There was no intersection. Instead, search for a nearby
				// segment we can "snap" to.
				
				// (for now, we skip this step, and assume it will be handled in the next iteration by the InterSectionRule. This won't work for parallel roads that we want to merge.)
				
			}
		}
		return retval;
	}

	/**
	 * Find the intersection of two lines. (from
	 * http://processingjs.org/learning/custom/intersect/ )
	 * 
	 * @param x1
	 *            - The X component of the coordinates for the start point of
	 *            the first line.
	 * @param y1
	 *            - The Y component of the coordinates for the start point of
	 *            the first line.
	 * @param x2
	 *            - The X component of the coordinates for the end point of the
	 *            first line.
	 * @param y2
	 *            - The Y component of the coordinates for the end point of the
	 *            first line.
	 * @param x3
	 *            - The X component of the coordinates for the start point of
	 *            the second line.
	 * @param y3
	 *            - The Y component of the coordinates for the start point of
	 *            the second line.
	 * @param x4
	 *            - The X component of the coordinates for the end point of the
	 *            second line.
	 * @param y4
	 *            - The Y component of the coordinates for the end point of the
	 *            second line.
	 * 
	 * @return a Point2d instance, representing the intersection of the two
	 *         lines, or null, if the specified lines do not intersect.
	 */
	protected Point2d intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {

		double a1, a2, b1, b2, c1, c2;
		double r1, r2, r3, r4;
		double denom, offset, num;

		// Compute a1, b1, c1, where line joining points 1 and 2
		// is "a1 x + b1 y + c1 = 0".
		a1 = y2 - y1;
		b1 = x1 - x2;
		c1 = (x2 * y1) - (x1 * y2);

		// Compute r3 and r4.
		r3 = ((a1 * x3) + (b1 * y3) + c1);
		r4 = ((a1 * x4) + (b1 * y4) + c1);

		// Check signs of r3 and r4. If both point 3 and point 4 lie on
		// same side of line 1, the line segments do not intersect.
		if ((r3 != 0) && (r4 != 0) && (r3 * r4 >= 0)) {
			return null;
		}

		// Compute a2, b2, c2
		a2 = y4 - y3;
		b2 = x3 - x4;
		c2 = (x4 * y3) - (x3 * y4);

		// Compute r1 and r2
		r1 = (a2 * x1) + (b2 * y1) + c2;
		r2 = (a2 * x2) + (b2 * y2) + c2;

		// Check signs of r1 and r2. If both point 1 and point 2 lie
		// on same side of second line segment, the line segments do
		// not intersect.
		if ((r1 != 0) && (r2 != 0) && (r1 * r2) >= 0) {
			return null;
		}

		// Line segments intersect: compute intersection point.
		denom = (a1 * b2) - (a2 * b1);

		if (denom == 0) {
			return null;
		}

		if (denom < 0) {
			offset = -denom / 2;
		}
		else {
			offset = denom / 2;
		}

		// The denom/2 is to get rounding instead of truncating. It
		// is added or subtracted to the numerator, depending upon the
		// sign of the numerator.
		num = (b1 * c2) - (b2 * c1);
		double x, y;
		if (num < 0) {
			x = (num - offset) / denom;
		}
		else {
			x = (num + offset) / denom;
		}

		num = (a2 * c1) - (a1 * c2);
		if (num < 0) {
			y = (num - offset) / denom;
		}
		else {
			y = (num + offset) / denom;
		}

		// lines_intersect
		return new Point2d(x, y);
	}

}
