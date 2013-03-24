package com.skyline.roads;

public class ValueMap2d<T extends Number> {
	private T[][] values;

	public T[][] getValues() {
		return values;
	}

	public void setValues(T[][] values) {
		this.values = values;
	}

	/**
	 * Returns the bilinear-interpolated value at the specified coordinates
	 * 
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 * @return the interpolated value at the specified coordinates.
	 */
	public double getInterpolatedValue(double x, double y) {
		// double xratio = ((double) values.length - 1) / Config.RESOLUTION_X;
		// double yratio = ((double) values[0].length - 1) /
		// Config.RESOLUTION_Y;
		// double mx = x * xratio;
		// double my = y * yratio;
		double u = x - (int) x;
		double v = y - (int) y;
		int xl = (int) x;
		int xr = (int) x + 1;
		int yb = (int) y;
		int yt = (int) y + 1;
		/*
		 * t2 ---------- t4 | | | +(mx,my) | | | t1 ---------- t3
		 */
		double bl, tl, br, tr;
		bl = (Double) values[Math.max(Math.min(xl, values.length - 1), 0)][Math.max(Math.min(yb, values.length - 1), 0)];
		tl = (Double) values[Math.max(Math.min(xl, values.length - 1), 0)][Math.max(Math.min(yt, values.length - 1), 0)];
		br = (Double) values[Math.max(Math.min(xr, values.length - 1), 0)][Math.max(Math.min(yb, values.length - 1), 0)];
		tr = (Double) values[Math.max(Math.min(xr, values.length - 1), 0)][Math.max(Math.min(yt, values.length - 1), 0)];

		// do bilinear interpolation
		return ((1.0 - u) * (1.0 - v) * bl + u * (1.0 - v) * br + (1.0 - u) * v * tl + u * v * tr);
	}

	public T getRealValue(double x, double y) {
		return values[Math.max(Math.min((int) x, values.length - 1), 0)][Math.max(Math.min((int) y, values[(int) x].length - 1), 0)];
	}

}
