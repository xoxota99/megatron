package com.skyline.application.events;

import java.awt.*;

public class TerrainEvent extends AbstractEvent {

	private Point fromPoint = null;
	private Point toPoint = null;
	private boolean isNewTerrain;

	public TerrainEvent(Object source) {
		super(source);
//		this.eventType = EventType.TERRAIN;
	}

	public TerrainEvent(Object source, Point fromPoint, Point toPoint) {
		this(source);
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
	}

	/**
	 * The x/y coordinates of the top/left corner of the rectangle containing
	 * the change, or <null> if not specified.
	 * 
	 * @return
	 */
	public Point getFromPoint() {
		return fromPoint;
	}

	public void setFromPoint(Point fromPoint) {
		this.fromPoint = fromPoint;
	}

	/**
	 * The x/y coordinates of the bottom/right corner of the rectangle
	 * containing the change, or <null> if not specified.
	 * 
	 * @return
	 */
	public Point getToPoint() {
		return toPoint;
	}

	public void setToPoint(Point toPoint) {
		this.toPoint = toPoint;
	}

	/**
	 * Indicates whether this event denotes a "brand new" terrain / height map.
	 * Use this to determine if the terrain "backup" should be refreshed.
	 * 
	 * @return
	 */
	public boolean isNewTerrain() {
		return isNewTerrain;
	}

	public void setNewTerrain(boolean isNewTerrain) {
		this.isNewTerrain = isNewTerrain;
	}

}
