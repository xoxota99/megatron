package com.skyline.transportation.model.quad;

/* *********************************************************************** *
 * RoadTreeNode.java                                                           *
 * *********************************************************************** *
 * date created    : August, 2012                                          *
 * email           : info@kirstywilliams.co.uk                             *
 * author          : Kirsty Williams                                       *
 * version         : 1.0                                                   *
 * *********************************************************************** */

import java.util.*;

import com.skyline.geometry.*;
import com.skyline.transportation.model.*;

public class RoadTreeNode {

	private ControlPoint leaf = null;

	private boolean hasChildren = false;
	private RoadTreeNode NW = null;
	private RoadTreeNode NE = null;
	private RoadTreeNode SE = null;
	private RoadTreeNode SW = null;
	private final Box bounds;
	private Set<RoadSegment> segments = new TreeSet<RoadSegment>();

	public RoadTreeNode(double minX, double minY, double maxX, double maxY) {
		this.bounds = new Box(minX, minY, maxX, maxY);
	}

	/**
	 * Add the specified RoadSegment to the RoadTree.
	 * 
	 * @param segment
	 * @return true if the RoadTreeNode did not already contain this segment.
	 */
	public boolean put(RoadSegment segment) {
		put(segment.getStartPoint());
		put(segment.getEndPoint());
		return segments.add(segment);
	}

	/**
	 * 
	 * @param controlPoint
	 * @return true if the Node did not already contain this ControlPoint.
	 */
	boolean put(ControlPoint controlPoint) {
		if (this.hasChildren) {
			return getChild(controlPoint.getX(), controlPoint.getY()).put(controlPoint);
		}
		if (this.leaf == null) {
			this.leaf = controlPoint;
			controlPoint.setNode(this);
			return true;
		}
		if (this.leaf.equals(controlPoint)) {
			return false;
		}
		// divide into 4 sub-nodes.
		this.divide();
		return getChild(controlPoint.getX(), controlPoint.getY()).put(controlPoint);
	}

	boolean remove(ControlPoint value) {
		boolean retval = false;
		if (this.hasChildren) {
			retval = getChild(value.getX(), value.getY()).remove(value);
		}
		if (this.leaf != null && this.leaf.equals(value)) {
			this.leaf.setNode(null);
			this.leaf = null;
			retval = true;
		}

		if (retval) {
			// we found the ControlPoint, now look for a Road containing this
			// point, and remove that as well.
			if (this.segments.size() > 0) {
				// TODO: This is suboptimal, since ancestor nodes will also
				// search their collection of segments.
				for (RoadSegment seg : this.segments) {
					if (seg.getStartPoint().equals(value)) {
						this.segments.remove(seg);
						remove(seg.getEndPoint());
					}
					if (seg.getEndPoint().equals(value)) {
						this.segments.remove(seg);
						remove(seg.getStartPoint());
					}
				}
			}
		}
		return retval;
	}

	public Box getBounds() {
		return this.bounds;
	}

	public void clear() {
		if (this.hasChildren) {
			this.NW.clear();
			this.NE.clear();
			this.SE.clear();
			this.SW.clear();
			this.NW = null;
			this.NE = null;
			this.SE = null;
			this.SW = null;
			this.hasChildren = false;
		} else {
			if (this.leaf != null) {
				this.leaf.setNode(null);
				this.leaf = null;
			}
		}
	}

	public ControlPoint get(double x, double y, double bestDistance) {
		if (this.hasChildren) {
			ControlPoint closest = null;
			RoadTreeNode bestChild = this.getChild(x, y);
			if (bestChild != null) {
				closest = bestChild.get(x, y, bestDistance);
			}
			if (bestChild != this.NW && this.NW.bounds.calcDist(x, y) < bestDistance) {
				ControlPoint value = this.NW.get(x, y, bestDistance);
				if (value != null) {
					closest = value;
				}
			}
			if (bestChild != this.NE && this.NE.bounds.calcDist(x, y) < bestDistance) {
				ControlPoint value = this.NE.get(x, y, bestDistance);
				if (value != null) {
					closest = value;
				}
			}
			if (bestChild != this.SE && this.SE.bounds.calcDist(x, y) < bestDistance) {
				ControlPoint value = this.SE.get(x, y, bestDistance);
				if (value != null) {
					closest = value;
				}
			}
			if (bestChild != this.SW && this.SW.bounds.calcDist(x, y) < bestDistance) {
				ControlPoint value = this.SW.get(x, y, bestDistance);
				if (value != null) {
					closest = value;
				}
			}
			return closest;
		}
		if (this.leaf != null) {
			double distance = Math.sqrt(
					(this.leaf.getX() - x) * (this.leaf.getX() - x)
							+ (this.leaf.getY() - y) * (this.leaf.getY() - y));
			if (distance < bestDistance) {
				bestDistance = distance;
				return this.leaf;
			}
		}
		return null;
	}

	public ArrayList<ControlPoint> get(double x, double y, double maxDistance, ArrayList<ControlPoint> values) {
		if (this.hasChildren) {
			if (this.NW.bounds.calcDist(x, y) <= maxDistance) {
				this.NW.get(x, y, maxDistance, values);
			}
			if (this.NE.bounds.calcDist(x, y) <= maxDistance) {
				this.NE.get(x, y, maxDistance, values);
			}
			if (this.SE.bounds.calcDist(x, y) <= maxDistance) {
				this.SE.get(x, y, maxDistance, values);
			}
			if (this.SW.bounds.calcDist(x, y) <= maxDistance) {
				this.SW.get(x, y, maxDistance, values);
			}
			return values;
		}
		if (this.leaf != null) {
			double distance = Math.sqrt(
					(this.leaf.getX() - x) * (this.leaf.getX() - x)
							+ (this.leaf.getY() - y) * (this.leaf.getY() - y));
			if (distance <= maxDistance) {
				values.add(this.leaf);
			}
		}
		return values;
	}

	public ArrayList<ControlPoint> get(Box bounds, ArrayList<ControlPoint> values) {
		if (this.hasChildren) {
			if (this.NW.bounds.intersects(bounds)) {
				this.NW.get(bounds, values);
			}
			if (this.NE.bounds.intersects(bounds)) {
				this.NE.get(bounds, values);
			}
			if (this.SE.bounds.intersects(bounds)) {
				this.SE.get(bounds, values);
			}
			if (this.SW.bounds.intersects(bounds)) {
				this.SW.get(bounds, values);
			}
			return values;
		}
		if (this.leaf != null && bounds.contains(this.leaf.getX(), this.leaf.getY())) {
			values.add(this.leaf);
		}
		return values;
	}

	public int execute(Box globalBounds, RoadTree.Executor executor) {
		int count = 0;
		if (this.hasChildren) {
			if (this.NW.bounds.intersects(globalBounds)) {
				count += this.NW.execute(globalBounds, executor);
			}
			if (this.NE.bounds.intersects(globalBounds)) {
				count += this.NE.execute(globalBounds, executor);
			}
			if (this.SE.bounds.intersects(globalBounds)) {
				count += this.SE.execute(globalBounds, executor);
			}
			if (this.SW.bounds.intersects(globalBounds)) {
				count += this.SW.execute(globalBounds, executor);
			}
			return count;
		}
		if (this.leaf != null && globalBounds.contains(this.leaf.getX(), this.leaf.getY())) {
			++count;
			executor.execute(this.leaf.getX(), this.leaf.getY(), this.leaf);
		}
		return count;
	}

	private void divide() {
		Box b = this.bounds;
		this.NW = new RoadTreeNode(b.getMinX(), b.getCenterY(), b.getCenterX(), b.getMaxY());
		this.NE = new RoadTreeNode(b.getCenterX(), b.getCenterY(), b.getMaxX(), b.getMaxY());
		this.SE = new RoadTreeNode(b.getCenterX(), b.getMinY(), b.getMaxX(), b.getCenterY());
		this.SW = new RoadTreeNode(b.getMinX(), b.getMinY(), b.getCenterX(), b.getCenterY());
		this.hasChildren = true;
		if (this.leaf != null) {
			RoadTreeNode nd = getChild(this.leaf.getX(), this.leaf.getY());
			nd.put(this.leaf);
			this.leaf.setNode(nd);
			this.leaf = null;
		}
	}

	private RoadTreeNode getChild(double x, double y) {
		if (this.hasChildren) {
			if (x < this.bounds.getCenterX()) {
				if (y < this.bounds.getCenterY())
					return this.SW;
				return this.NW;
			}
			if (y < this.bounds.getCenterY())
				return this.SE;
			return this.NE;
		}
		return null;
	}

	public ControlPoint firstLeaf() {
		if (this.hasChildren) {
			ControlPoint leaf = this.SW.firstLeaf();
			if (leaf == null) {
				leaf = this.NW.firstLeaf();
			}
			if (leaf == null) {
				leaf = this.SE.firstLeaf();
			}
			if (leaf == null) {
				leaf = this.NE.firstLeaf();
			}
			return leaf;
		}
		return this.leaf;
	}

	public boolean nextLeaf(ControlPoint currentLeaf, ControlPoint nextLeaf) {
		if (this.hasChildren) {
			boolean found = false;
			if (currentLeaf.getX() <= this.bounds.getCenterX() && currentLeaf.getY() <= this.bounds.getCenterY()) {
				found = this.SW.nextLeaf(currentLeaf, nextLeaf);
				if (found) {
					if (nextLeaf == null) {
						nextLeaf = this.NW.firstLeaf();
					}
					if (nextLeaf == null) {
						nextLeaf = this.SE.firstLeaf();
					}
					if (nextLeaf == null) {
						nextLeaf = this.NE.firstLeaf();
					}
					return true;
				}
			}
			if (currentLeaf.getX() <= this.bounds.getCenterX() && currentLeaf.getY() >= this.bounds.getCenterY()) {
				found = this.NW.nextLeaf(currentLeaf, nextLeaf);
				if (found) {
					if (nextLeaf == null) {
						nextLeaf = this.SE.firstLeaf();
					}
					if (nextLeaf == null) {
						nextLeaf = this.NE.firstLeaf();
					}
					return true;
				}
			}
			if (currentLeaf.getX() >= this.bounds.getCenterX() && currentLeaf.getY() <= this.bounds.getCenterY()) {
				found = this.SE.nextLeaf(currentLeaf, nextLeaf);
				if (found) {
					if (nextLeaf == null) {
						nextLeaf = this.NE.firstLeaf();
					}
					return true;
				}
			}
			if (currentLeaf.getX() >= this.bounds.getCenterX() && currentLeaf.getY() >= this.bounds.getCenterY()) {
				return this.NE.nextLeaf(currentLeaf, nextLeaf);
			}
			return false;
		}
		return currentLeaf == this.leaf;
	}

	public ControlPoint nextLeaf(ControlPoint currentLeaf) {
		ControlPoint nextLeaf = null;
		nextLeaf(currentLeaf, nextLeaf);
		System.out.println("nextLeaf is " + nextLeaf); // is it null? Not sure
														// if passing null will
														// get us back a
														// non-null.
		return nextLeaf;
	}

	public RoadTreeNode getFirstCommonAncestor(double x1, double y1, double x2, double y2) {
		if (this.hasChildren) {
			RoadTreeNode n1 = this.getChild(x1, y1);
			RoadTreeNode n2 = this.getChild(x2, y2);
			if (n1 == n2) {
				// n1 is A common ancestor, but is it THE FIRST?
				return n1.getFirstCommonAncestor(x1, y1, x2, y2);
			} else {
				return this; // n1 and n2 are different, but they're not my
								// children, so I must be THE FIRST.
			}
		}
		return null; // No children, so I can't be an ancestor.
	}

	// TODO: Test this.
	public RoadTreeNode getFirstCommonAncestor(ControlPoint point1, ControlPoint point2) {
		return getFirstCommonAncestor(point1.getX(), point1.getY(), point2.getX(), point2.getY());
	}

	public Set<RoadSegment> getSegments() {
		return segments;
	}
}