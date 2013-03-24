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
	 * If all this node's children are empty, remove them.
	 */
	public void collapse() {
		if (this.hasChildren) {
			NW.collapse();
			NE.collapse();
			SE.collapse();
			SW.collapse();

			if ((NW.leaf == null && !NW.hasChildren)
					&& (NE.leaf == null && !NE.hasChildren)
					&& (SE.leaf == null && !SE.hasChildren)
					&& (SW.leaf == null && !SW.hasChildren)) {
				clear();
			}
		}
	}

	/**
	 * Add the specified RoadSegment to the RoadQuad.
	 * 
	 * @param segment
	 * @return true if the RoadTreeNode did not already contain this segment.
	 */
	public boolean put(RoadSegment segment) {
		put(segment.getStartPoint());
		put(segment.getEndPoint());
		RoadTreeNode ancestor = getFirstCommonAncestor(segment.getStartPoint(), segment.getEndPoint());
		return ancestor.segments.add(segment);
	}

	/**
	 * 
	 * @param controlPoint
	 * @return true if the Node did not already contain this ControlPoint.
	 */
	boolean put(ControlPoint controlPoint) {
		if (this.hasChildren) {
			return getChild(controlPoint.x, controlPoint.y).put(controlPoint);
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
		return getChild(controlPoint.x, controlPoint.y).put(controlPoint);
	}

	/**
	 * Remove a segment from the RoadTree. If the endpoints of the segment have
	 * no other segments attached to them, and removeEndPoints is set to true,
	 * the endpoints will also be removed.
	 * 
	 * @param value
	 * @param removeEndPoints
	 * @return
	 */
	public boolean remove(RoadSegment value, boolean removeEndPoints) {
		RoadTreeNode node = null;
		if (value.getNode() != null && value.getNode().getSegments().contains(value)) {
			// hey, we already know the node! That's handy.
			node = value.getNode();
		} else {
			// ugh. brute force it.
			node = getFirstCommonAncestor(value.getStartPoint(), value.getEndPoint());
		}
		node.getSegments().remove(value);
		value.setNode(null);
		// if (!node.getSegments().remove(value)) {
		// return false;
		// }

		ControlPoint sp = get(value.getStartPoint());
		ControlPoint ep = get(value.getEndPoint());

		if (sp != null && ep != null) {
			sp.getSegments().remove(value); // remove the segment from this
											// ControlPoint's collection of
											// segments.
			ep.getSegments().remove(value);
			if (removeEndPoints) {
				if (sp.getSegments().size() == 0) {
					remove(sp); // no other segments, so remove it.
				}
				if (ep.getSegments().size() == 0) {
					remove(ep); // no other segments, so remove it.
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private ControlPoint get(ControlPoint pt) {
		if (pt.getNode() != null) {
			return pt; // we are already referencing a node in the quad.
		} else {
			return get(pt.x, pt.y, ControlPoint.EPSILON); // Get the actual node
															// from the tree.
		}

	}

	public boolean remove(ControlPoint value) {
		ControlPoint rem = null;
		boolean retval = false;

		if (this.hasChildren) {
			retval = getChild(value.x, value.y).remove(value);
			if (retval) {
				collapse(); // clean up.
			}
		}

		if (this.leaf != null && this.leaf.equals(value)) {
			rem = this.leaf;
			this.leaf.setNode(null);
			this.leaf = null;
			retval = true;
			// we found the ControlPoint, now cycle through this ControlPoint's
			// segments, and remove them as well.
			for (RoadSegment seg : rem.getSegments()) {
				if (seg.getNode() != null) {
					// find the "other end" of this segment, and remove it,
					// if it has no other segments attached.
					if (seg.getStartPoint().equals(rem)) { // we are the start
															// point.
						seg.getEndPoint().getSegments().remove(seg);
						// Note: We only remove the other end of this
						// segment if it has no other segments attached to it.
						if (seg.getEndPoint().getSegments().size() > 0) {
							seg.getNode().remove(seg.getEndPoint());
						}
					} else { // we are the end point.
						seg.getStartPoint().getSegments().remove(seg);
						if (seg.getStartPoint().getSegments().size() > 0) {
							seg.getNode().remove(seg.getStartPoint());
						}
					}
				}
				seg.getNode().segments.remove(seg);
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
			this.segments.clear(); // obviously, if we have no children, we
									// can't have any segments.
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
					(this.leaf.x - x) * (this.leaf.x - x)
							+ (this.leaf.y - y) * (this.leaf.y - y));
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
					(this.leaf.x - x) * (this.leaf.x - x)
							+ (this.leaf.y - y) * (this.leaf.y - y));
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
		if (this.leaf != null && bounds.contains(this.leaf.x, this.leaf.y)) {
			values.add(this.leaf);
		}
		return values;
	}

	public int execute(Box globalBounds, RoadQuad.Executor executor) {
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
		if (this.leaf != null && globalBounds.contains(this.leaf.x, this.leaf.y)) {
			++count;
			executor.execute(this.leaf.x, this.leaf.y, this.leaf);
		}
		return count;
	}

	/**
	 * Create new child nodes.
	 */
	private void divide() {
		Box b = this.bounds;
		this.NW = new RoadTreeNode(b.getMinX(), b.getCenterY(), b.getCenterX(), b.getMaxY());
		this.NE = new RoadTreeNode(b.getCenterX(), b.getCenterY(), b.getMaxX(), b.getMaxY());
		this.SE = new RoadTreeNode(b.getCenterX(), b.getMinY(), b.getMaxX(), b.getCenterY());
		this.SW = new RoadTreeNode(b.getMinX(), b.getMinY(), b.getCenterX(), b.getCenterY());
		this.hasChildren = true;
		if (this.leaf != null) {
			RoadTreeNode nd = getChild(this.leaf.x, this.leaf.y);
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
			if (currentLeaf.x <= this.bounds.getCenterX() && currentLeaf.y <= this.bounds.getCenterY()) {
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
			if (currentLeaf.x <= this.bounds.getCenterX() && currentLeaf.y >= this.bounds.getCenterY()) {
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
			if (currentLeaf.x >= this.bounds.getCenterX() && currentLeaf.y <= this.bounds.getCenterY()) {
				found = this.SE.nextLeaf(currentLeaf, nextLeaf);
				if (found) {
					if (nextLeaf == null) {
						nextLeaf = this.NE.firstLeaf();
					}
					return true;
				}
			}
			if (currentLeaf.x >= this.bounds.getCenterX() && currentLeaf.y >= this.bounds.getCenterY()) {
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

	/**
	 * Return the RoadTreeNode that is the first common ancestor of the
	 * specified points.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public RoadTreeNode getFirstCommonAncestor(double x1, double y1, double x2, double y2) {
		if (this.hasChildren) {
			RoadTreeNode n1 = this.getChild(x1, y1);
			RoadTreeNode n2 = this.getChild(x2, y2);
			if (n1 == n2) {
				// n1 is A common ancestor, but is it THE FIRST?
				return n1.getFirstCommonAncestor(x1, y1, x2, y2);
			} else if (n1 != null && n2 != null) {
				return this; // n1 and n2 are different, but they're both my
								// children, so I must be THE FIRST Common
								// Ancestor.
			} else {
				// at least one of the points could not be found among my
				// children, so I am not a common ancestor.
				return null;
			}
		}
		return null; // No children, so I can't be an ancestor.
	}

	public RoadTreeNode getFirstCommonAncestor(ControlPoint point1, ControlPoint point2) {
		return getFirstCommonAncestor(point1.x, point1.y, point2.x, point2.y);
	}

	public Set<RoadSegment> getSegments() {
		return segments;
	}

	/**
	 * Return the Set of all existing RoadSegments that are completely contained
	 * in the specified bounds.
	 * 
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @return
	 */
	public Set<RoadSegment> getContainedSegments(double minX, double minY, double maxX, double maxY) {
		Set<RoadSegment> retval = new TreeSet<RoadSegment>();
		if (hasChildren) {
			retval.addAll(SW.getContainedSegments(minX, minY, maxX, maxY));
			retval.addAll(NW.getContainedSegments(minX, minY, maxX, maxY));
			retval.addAll(SE.getContainedSegments(minX, minY, maxX, maxY));
			retval.addAll(NE.getContainedSegments(minX, minY, maxX, maxY));
		}
		// retval now contains all valid child segments. Now add my own.
		Box b = new Box(minX, minY, maxX, maxY);
		for (RoadSegment seg : this.segments) {
			if (b.contains(seg.getStartPoint().x, seg.getStartPoint().y)
					&& b.contains(seg.getEndPoint().x, seg.getEndPoint().y)) {
				retval.add(seg);
			}
		}
		return retval;
	}

	public Set<RoadSegment> getAllSegments() {
		return getContainedSegments(this.bounds.getMinX(), this.bounds.getMinY(), this.bounds.getMaxX(), this.bounds.getMaxY());
	}
}