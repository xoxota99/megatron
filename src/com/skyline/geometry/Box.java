package com.skyline.geometry;

public class Box {
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	public Box() {
		this(0, 0, 0, 0);
	}

	/**
	 * @param x
	 * @param y
	 * @return the distance from the given coordinates to the nearest corner of
	 *         the Box, or zero if the point lies inside the box.
	 */
	public double calcDist(double x, double y) {
		double distanceX;
		double distanceY;

		if (this.minX <= x && x <= this.maxX) {
			distanceX = 0;
		} else {
			distanceX = Math.min(Math.abs(this.minX - x), Math.abs(this.maxX - x));
		}
		if (this.minY <= y && y <= this.maxY) {
			distanceY = 0;
		} else {
			distanceY = Math.min(Math.abs(this.minY - y), Math.abs(this.maxY - y));
		}

		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
	}

	public Box(double minX, double minY, double maxX, double maxY) {
		setMinX(minX);
		setMinY(minY);
		setMaxX(maxX);
		setMaxY(maxY);
	}

	public Box getBounds() {
		return new Box(minX, minY, maxX, maxY);
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		if (minX > this.maxX) {
			this.minX = this.maxX;
			this.maxX = minX;
		} else {
			this.minX = minX;
		}
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		if (minY > this.maxY) {
			this.minY = this.maxY;
			this.maxY = minY;
		} else {
			this.minY = minY;
		}
	}

	public boolean contains(double x, double y) {
		return (x > this.minX &&
				y > this.minY &&
				x < this.maxX && y < this.maxY);
	}

	public boolean containsOrEquals(Box box) {
		return (box.minX >= this.minX &&
				box.minY >= this.minY &&
				box.maxX <= this.maxX && box.maxY <= this.maxY);
	}

	/**
	 * Determines whether or not this <code>Box</code> and the specified
	 * <code>Box</code> intersect. Two rectangles intersect if their
	 * intersection is nonempty.
	 * 
	 * @param r
	 *            the specified <code>Box</code>
	 * @return <code>true</code> if the specified <code>Box</code> and this
	 *         <code>Box</code> intersect; <code>false</code> otherwise.
	 */
	public boolean intersects(Box r) {
		return intersects(r.minX, r.minY, r.maxX, r.maxY);
	}

	public boolean intersects(double x, double y, double width, double height) {
		double tw = maxX - minX;
		double th = maxY - minY;
		double rw = width;
		double rh = height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}
		double tx = this.minX;
		double ty = this.minY;
		double rx = x;
		double ry = y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		// overflow || intersect
		return ((rw < rx || rw > tx) &&
				(rh < ry || rh > ty) &&
				(tw < tx || tw > rx) && (th < ty || th > ry));
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		if (maxX < this.minX) {
			this.maxX = this.minX;
			this.minX = maxX;
		} else {
			this.maxX = maxX;
		}
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		if (maxY < this.minY) {
			this.maxY = this.minY;
			this.minY = maxY;
		} else {
			this.maxY = maxY;
		}
	}

	public double getCenterX() {
		return (minX + maxX) / 2;
	}
	public double getCenterY() {
		return (minY + maxY) / 2;
	}
}
