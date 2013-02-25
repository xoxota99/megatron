package com.skyline.transportation.model.quad;

/* *********************************************************************** *
 * RoadTree.java                                                           *
 * *********************************************************************** *
 * date created    : August, 2012                                          *
 * email           : info@kirstywilliams.co.uk                             *
 * author          : Kirsty Williams                                       *
 * version         : 1.0                                                   *
 * *********************************************************************** */

import java.util.*;

import sun.security.action.*;

import com.skyline.geometry.*;
import com.skyline.transportation.model.*;

public class RoadTree {

	protected RoadTreeNode root = null;
	private int size = 0;
	private AbstractCollection<ControlPoint> values = null;
	private static RoadTree instance = new RoadTree(0, 0, 1, 1);

	public static RoadTree getInstance() {
		return instance;
	}

	/**
	 * Creates an empty RoadTree with the bounds
	 */
	private RoadTree(double minX, double minY, double maxX, double maxY) {
		this.root = new RoadTreeNode(minX, minY, maxX, maxY);
	}

	public boolean put(ControlPoint value) {
		if (this.root.put(value)) {
			increaseSize();
			return true;
		}
		return false;
	}

	public boolean remove(ControlPoint value) {
		if (this.root.remove(value)) {
			decreaseSize();
			return true;
		}
		return false;
	}

	public void clear() {
		this.root.clear();
		this.size = 0;
	}

	private void increaseSize() {
		this.size++;
		this.values = null;
	}

	private void decreaseSize() {
		this.size--;
		this.values = null;
	}

	/**
	 * Gets the object closest to (x,y)
	 */
	public ControlPoint get(double x, double y) {
		return this.root.get(x, y, Double.POSITIVE_INFINITY);
	}

	/**
	 * Gets the list of all Values, unsorted, within the specified distance.
	 */
	public ArrayList<ControlPoint> get(double x, double y, double distance) {
		return this.root.get(x, y, distance, new ArrayList<ControlPoint>());
	}

	/**
	 * Gets all objects inside the specified boundary.
	 */
	public ArrayList<ControlPoint> get(Box bounds, ArrayList<ControlPoint> values) {
		return this.root.get(bounds, values);
	}

	/**
	 * Gets all objects inside the specified area.
	 */
	public ArrayList<ControlPoint> get(double minX, double minY, double maxX, double maxY, ArrayList<ControlPoint> values) {
		return get(new Box(minX, minY, maxX, maxY), values);
	}

	public int execute(Box bounds, Executor executor) {
		if (bounds == null) {
			return this.root.execute(this.root.getBounds(), executor);
		}
		return this.root.execute(bounds, executor);
	}

	public int execute(double minX, double minY, double maxX, double maxY, Executor executor) {
		return execute(new Box(minX, minY, maxX, maxY), executor);
	}

	public int size() {
		return this.size;
	}

	public double getMinX() {
		return this.root.getBounds().getMinX();
	}

	public double getMaxX() {
		return this.root.getBounds().getMaxX();
	}

	public double getMinY() {
		return this.root.getBounds().getMinY();
	}

	public double getMaxY() {
		return this.root.getBounds().getMaxY();
	}

	public AbstractCollection<ControlPoint> values() {
		if (this.values == null) {
			this.values = new AbstractCollection<ControlPoint>() {
				@Override
				public Iterator<ControlPoint> iterator() {
					Iterator<ControlPoint> iterator = new Iterator<ControlPoint>() {
						private ControlPoint currentLeaf = firstLeaf();
						private ControlPoint next = first();

						private ControlPoint first() {
							if (this.currentLeaf == null) {
								return null;
							}
							loadNext();
							return this.next;
						}

						@Override
						public boolean hasNext() {
							return this.next != null;
						}

						@Override
						public ControlPoint next() {
							if (this.next == null) {
								return null;
							}
							ControlPoint current = this.next;
							loadNext();
							return current;
						}

						private void loadNext() {
							boolean searching = true;
							while (searching) {
								this.currentLeaf = nextLeaf(this.currentLeaf);
								if (this.currentLeaf == null) {
									this.next = null;
									searching = false;
								}
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
					return iterator;
				}

				@Override
				public int size() {
					return RoadTree.this.size;
				}
			};
		}
		return this.values;
	}

	private ControlPoint firstLeaf() {
		return this.root.firstLeaf();
	}

	private ControlPoint nextLeaf(ControlPoint currentLeaf) {
		return this.root.nextLeaf(currentLeaf);
	}

	interface Executor {
		public void execute(double x, double y, ControlPoint object);
	}

	public RoadSegment getRoadSegment(double x1, double y1, double x2, double y2) {
		RoadTreeNode nd = root.getFirstCommonAncestor(x1, y1, x2, y2);
		Set<RoadSegment> segments=nd.getSegments();
		if(segments!=null && segments.size()>0){
			 for(RoadSegment seg : segments){
				 ControlPoint start = seg.getStartPoint();
				 ControlPoint end = seg.getEndPoint();
				 if((start.getX()==x1 && start.getY()==y1 && end.getX()==x2 && end.getY()==y2)
						 || (start.getX()==x2 && start.getY()==y2 && end.getX()==x1 && end.getY()==y1)){
					 //found it.
					 return seg;
				 }
			 }
		}
		return null;	//didn't find it.
		
	}

	public void put(RoadSegment segment) {
		//First, add the individual ControlPoints.
		put(segment.getStartPoint());
		put(segment.getEndPoint());
		
		//Then, find the first common ancestor for both points.
		RoadTreeNode ancestor = root.getFirstCommonAncestor(segment.getStartPoint(), segment.getEndPoint());
		
		//Then, add the segment to that node's segments collection.
		ancestor.put(segment);
	}
}