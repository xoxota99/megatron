package com.skyline.roads.util;

import javax.vecmath.*;

public class VecMath {
	public static final double PI180 = Math.PI / 180; // 0.0174532925, or
														// thereabouts.

	/**
	 * Returns a point calculated from a origin, distance and direction.
	 * 
	 * @param dist
	 *            distance from origin.
	 * @param origin
	 *            origin point.
	 * @param dir
	 *            direction to travel, in degrees angle from the X axis.
	 * @return
	 */
	public static Point2d calculatePoint(double dist, double baseX, double baseY, double dirX, double dirY) {
		Vector2d dir = new Vector2d(dirX, dirY);
		dir.normalize();
		dir.x = baseX + dist * dir.x;
		dir.y = baseY + dist * dir.y;
		return new Point2d(dir);
	}

	public static double slope(double x1, double y1, double z1, double x2, double y2, double z2) {
		double d = distance(x1, y1, x2, y2);
		return Math.abs(z2 - z1) / d;
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	/**
	 * Calculate angle of a vector to x axis.
	 * 
	 * @param vec
	 * @return
	 */
	public static double angle(double x, double y) {
		Vector2d temp = new Vector2d(x, y);
		temp.normalize();

		if ((temp.x >= 0.0) && (temp.y >= 0.0))
			return Math.asin(temp.y) / PI180; // sector I
		if ((temp.x < 0.0) && (temp.y >= 0.0))
			return 180.0 - Math.asin(temp.y) / PI180; // sector II
		if ((temp.x < 0.0) && (temp.y < 0.0))
			return 180.0 + Math.asin(-temp.y) / PI180; // sector III
		if ((temp.x >= 0.0) && (temp.y < 0.0))
			return 360.0 - Math.asin(-temp.y) / PI180; // sector IV
		return 0.0; // will never be reached
	}

	/**
	 * Get a normalized vector (x,y), rotated by the specified angle.
	 * 
	 * @param alpha
	 *            The angle to rotate, in degrees.
	 * 
	 * @return a new Vector2d, representing the normalized, rotated vector.
	 */
	public static Vector2d rotateNormalizedVector(double alpha) {
		return new Vector2d(Math.cos(alpha * PI180), Math.sin(alpha * PI180));
	}

}
