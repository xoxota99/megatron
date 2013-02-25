package com.skyline.application.tools.terrain;

import java.awt.*;

import com.jme3.math.*;
import com.jme3.terrain.heightmap.*;
import com.skyline.application.events.*;
import com.skyline.application.i18n.*;
import com.skyline.application.state.*;
import com.skyline.application.tools.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

public class LocalSmoothTool extends LocalTool {
	protected static float MAX_RADIUS = 0.125f; // Max tool radius is 1/8 of
												// the entire world(!)
	protected static float MAX_POWER = 0.125f; // Max tool power is 1/8 of
												// the entire world size
	protected static final float MIN_RADIUS = 0.01f;
	protected static final float MIN_POWER = 0.01f;

	public LocalSmoothTool() {
		super(Messages.getString(LocalSmoothTool.class, "toolName"), Messages.getString(LocalSmoothTool.class, "toolTip"), CursorType.CONE);
		setPower(0.25f);
		setRadius(1f);
		// this.toolAppState=toolAppState;
	}

	@Override
	public boolean isContinuous() {
		return true;
	}

	/**
	 * in "ALT" mode, this tool actually roughens the terrain.
	 */
	@Override
	public void execute(WorldState worldState, int x, int y, int modifiers) {
		boolean isRoughening = (modifiers & ToolAppState.ModifierKey.ALT) != 0;
		// RoadEngine.out.println("isRoughening="+isRoughening);
		HeightMap heightMap = worldState.getTerrainHeightMap();

		// unlike Elevation Adjustments, smoothing power is not affected by
		// worldsize.
		float pow = (MIN_POWER + (power * (MAX_POWER - MIN_POWER))) * (isRoughening ? -1 : 1);
		float rad = (MIN_RADIUS + (radius * (MAX_RADIUS - MIN_RADIUS))) * worldState.getSize();

		// RoadEngine.out.printf("rad = %f, pow=%f\n", rad, pow);
		int xMin = (int) Math.floor(Math.max(x - rad, 1));
		int xMax = (int) Math.floor(Math.min(x + rad, worldState.getSize() - 1));
		int yMin = (int) Math.floor(Math.max(y - rad, 1));
		int yMax = (int) Math.floor(Math.min(y + rad, worldState.getSize() - 1));
		for (int yy = yMin; yy <= yMax; yy++) {
			for (int xx = xMin; xx <= xMax; xx++) {
				float dx = xx - x;
				float dy = yy - y;
				double dist = Math.sqrt((dx * dx) + (dy * dy));
				// see if it is in the radius of the tool
				if (dist <= rad) {
					// found a node we want to smooth.
					int neighNumber = 0;
					float neighAverage = 0;
					// hunt for neighbors.
					// RoadEngine.out.println("Looking for neightbors.\n\n");

					int xMin2 = (int) Math.floor(Math.max(xx - rad, 1));
					int xMax2 = (int) Math.floor(Math.min(xx + rad, worldState.getSize() - 1));
					int yMin2 = (int) Math.floor(Math.max(yy - rad, 1));
					int yMax2 = (int) Math.floor(Math.min(yy + rad, worldState.getSize() - 1));
					for (int yyy = (int) yMin2; yyy <= yMax2; yyy++) {
						for (int xxx = (int) xMin2; xxx <= xMax2; xxx++) {
							neighNumber++;
							neighAverage += heightMap.getTrueHeightAtPoint(xxx, yyy);
						}
					}

					neighAverage /= neighNumber;
					float cp = (1 - pow);// * calculateLocalPower(rad, 1f, xx,
											// yy); //power falls off as a
											// cosine of the distance.
					float oldHeight = heightMap.getTrueHeightAtPoint(xx, yy);

					heightMap.setHeightAtPoint((neighAverage * (1 - cp)) + (oldHeight * cp), xx, yy);
				}
			}
		}
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainEvent(this, new Point(xMin, yMin), new Point(xMax, yMax)));
	}

	private float calculateLocalPower(float radius, float multiplier, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return multiplier * yVal;
	}

	@Override
	public float getMaxPower() {
		return MAX_POWER;
	}

	@Override
	public float getMinPower() {
		return MIN_POWER;
	}

	@Override
	public float getMaxRadius() {
		return MAX_RADIUS;
	}

	@Override
	public float getMinRadius() {
		return MIN_RADIUS;
	}

}
