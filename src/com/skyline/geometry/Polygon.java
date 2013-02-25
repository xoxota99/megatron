package com.skyline.geometry;

import java.awt.geom.*;
import java.util.*;

public class Polygon {
	/**
	 * This total number of endpoints.
	 * 
	 * @serial the number of endpoints, possibly less than the array sizes
	 */
	public int npoints;

	/**
	 * The array of X coordinates of endpoints. This should not be null.
	 * 
	 * @see #addPoint(double, double)
	 * @serial the x coordinates
	 */
	public double[] xpoints;

	/**
	 * The array of Y coordinates of endpoints. This should not be null.
	 * 
	 * @see #addPoint(double, double)
	 * @serial the y coordinates
	 */
	public double[] ypoints;

	/**
	 * The bounding box of this polygon. This is lazily created and cached, so
	 * it must be invalidated after changing points.
	 * 
	 * @see #getBounds()
	 * @serial the bounding box, or null
	 */
	protected Box bounds;

	/** A big number, but not so big it can't survive a few double operations */
	private static final double BIG_VALUE = java.lang.Double.MAX_VALUE / 10.0;

	/**
	 * Initializes an empty polygon.
	 */
	public Polygon()
	{
		// Leave room for growth.
		xpoints = new double[4];
		ypoints = new double[4];
	}

	public Polygon(double[] xpoints, double[] ypoints, int npoints)
	{
		this.xpoints = new double[npoints];
		this.ypoints = new double[npoints];
		System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
		System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
		this.npoints = npoints;
	}

	/**
	 * Adds the specified endpoint to the polygon. This updates the bounding
	 * box, if it has been created.
	 * 
	 * @param x
	 *            the X coordinate of the point to add
	 * @param y
	 *            the Y coordiante of the point to add
	 */
	public void addPoint(int x, int y)
	{
		if (npoints + 1 > xpoints.length)
		{
			double[] newx = new double[npoints + 1];
			System.arraycopy(xpoints, 0, newx, 0, npoints);
			xpoints = newx;
		}
		if (npoints + 1 > ypoints.length)
		{
			double[] newy = new double[npoints + 1];
			System.arraycopy(ypoints, 0, newy, 0, npoints);
			ypoints = newy;
		}
		xpoints[npoints] = x;
		ypoints[npoints] = y;
		npoints++;
		if (bounds != null)
		{
			if (npoints == 1)
			{
				bounds.setMinX(x);
				bounds.setMinY(y);
			}
			else
			{
				if (x < bounds.getMinX())
				{
					bounds.setMinX(x);
				}
				else if (x > bounds.getMaxX())
				{
					bounds.setMaxX(x);
				}

				if (y < bounds.getMinY())
				{
					bounds.setMinY(y);
				}
				else if (y > bounds.getMaxY()) {
					bounds.setMaxY(y);
				}
			}
		}
	}

	public Box getBoundingBox()
	{
		if (bounds == null)
		{
			if (npoints == 0)
				return bounds = new Box();
			int i = npoints - 1;
			double minx = xpoints[i];
			double maxx = minx;
			double miny = ypoints[i];
			double maxy = miny;
			while (--i >= 0)
			{
				double x = xpoints[i];
				double y = ypoints[i];
				if (x < minx)
					minx = x;
				else if (x > maxx)
					maxx = x;
				if (y < miny)
					miny = y;
				else if (y > maxy)
					maxy = y;
			}
			bounds = new Box(minx, miny, maxx, maxy);
		}
		return bounds;
	}

	/**
	 * Translates the polygon by adding the specified values to all X and Y
	 * coordinates. This updates the bounding box, if it has been calculated.
	 * 
	 * @param dx
	 *            the amount to add to all X coordinates
	 * @param dy
	 *            the amount to add to all Y coordinates
	 * @since 1.1
	 */
	public void translate(double dx, double dy)
	{
		int i = npoints;
		while (--i >= 0)
		{
			xpoints[i] += dx;
			ypoints[i] += dy;
		}
		if (bounds != null)
		{
			bounds.setMinX(bounds.getMinX() + dx);
			bounds.setMinY(bounds.getMinY() + dy);
		}
	}

	/**
	 * Helper for contains, intersects, calculates the number of intersections
	 * between the polygon and a line extending from the point (x, y) along the
	 * positive X, or Y axis, within a given interval.
	 * 
	 * @return the winding number.
	 * @see #contains(double, double)
	 */
	private int evaluateCrossings(double x, double y, boolean useYaxis,
			double distance)
	{
		double x0;
		double x1;
		double y0;
		double y1;
		double epsilon = 0.0;
		int crossings = 0;
		double[] xp;
		double[] yp;

		if (useYaxis)
		{
			xp = ypoints;
			yp = xpoints;
			double swap;
			swap = y;
			y = x;
			x = swap;
		}
		else
		{
			xp = xpoints;
			yp = ypoints;
		}

		/* Get a value which is small but not insignificant relative the path. */
		epsilon = 1E-7;

		x0 = xp[0] - x;
		y0 = yp[0] - y;
		for (int i = 1; i < npoints; i++)
		{
			x1 = xp[i] - x;
			y1 = yp[i] - y;

			if (y0 == 0.0)
				y0 -= epsilon;
			if (y1 == 0.0)
				y1 -= epsilon;
			if (y0 * y1 < 0)
				if (Line2D.linesIntersect(x0, y0, x1, y1, epsilon, 0.0, distance, 0.0))
					++crossings;

			x0 = xp[i] - x;
			y0 = yp[i] - y;
		}

		// end segment
		x1 = xp[0] - x;
		y1 = yp[0] - y;
		if (y0 == 0.0)
			y0 -= epsilon;
		if (y1 == 0.0)
			y1 -= epsilon;
		if (y0 * y1 < 0)
			if (Line2D.linesIntersect(x0, y0, x1, y1, epsilon, 0.0, distance, 0.0))
				++crossings;

		return crossings;
	}

	/**
	 * Tests whether or not the specified point is inside this polygon.
	 * 
	 * @param x
	 *            the X coordinate of the point to test
	 * @param y
	 *            the Y coordinate of the point to test
	 * @return true if the point is inside this polygon
	 * @since 1.2
	 */
	public boolean contains(double x, double y)
	{
		return ((evaluateCrossings(x, y, false, BIG_VALUE) & 1) != 0);
	}

	/**
	 * Test if a given rectangle intersects this shape.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return true if the rectangle intersects this shape
	 */
	public boolean intersects(double x, double y, double w, double h)
	{
		/* Does any edge intersect? */
		if (evaluateCrossings(x, y, false, w) != 0 /* top */
				|| evaluateCrossings(x, y + h, false, w) != 0 /* bottom */
				|| evaluateCrossings(x + w, y, true, h) != 0 /* right */
				|| evaluateCrossings(x, y, true, h) != 0) /* left */
			return true;

		/* No intersections, is any point inside? */
		if ((evaluateCrossings(x, y, false, BIG_VALUE) & 1) != 0)
			return true;

		return false;
	}

	/**
	 * Test if a rectangle lies completely in this Shape. This is true if all
	 * points in the rectangle are in the shape.
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return true if the rectangle is contained in this shape
	 */
	public boolean contains(double x, double y, double w, double h)
	{
		if (!getBoundingBox().intersects(x, y, w, h))
			return false;

		/* Does any edge intersect? */
		if (evaluateCrossings(x, y, false, w) != 0 /* top */
				|| evaluateCrossings(x, y + h, false, w) != 0 /* bottom */
				|| evaluateCrossings(x + w, y, true, h) != 0 /* right */
				|| evaluateCrossings(x, y, true, h) != 0) /* left */
			return false;

		/* No intersections, is any point inside? */
		if ((evaluateCrossings(x, y, false, BIG_VALUE) & 1) != 0)
			return true;

		return false;
	}

}
