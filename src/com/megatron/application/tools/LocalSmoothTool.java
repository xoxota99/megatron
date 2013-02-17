package com.megatron.application.tools;

import java.awt.*;

import com.jme3.math.*;
import com.jme3.terrain.heightmap.*;
import com.megatron.application.*;
import com.megatron.application.events.*;
import com.megatron.application.i18n.*;
import com.megatron.model.*;
import com.megatron.terrain.*;

public class LocalSmoothTool extends LocalTool {
	protected static float MAX_AOE_SCALE = 0.125f; // Max tool radius is 1/8 of
													// the entire world(!)
	protected static float MAX_POWER_SCALE = 0.125f; // Max tool power is 1/8 of
														// the entire world size

	public LocalSmoothTool() {
		super(Messages.getString(LocalSmoothTool.class, "toolName"), Messages.getString(LocalSmoothTool.class, "toolTip"), CursorType.CONE);
		setPower(0.25f);
		setRadius(1f);
	}

	@Override
	public boolean isContinuous() {
		return true;
	}

	@Override
	public void execute(WorldState worldState, int x, int y) {
		boolean isLowering = worldState.getInputModifier(WorldState.ALT);
		HeightMap heightMap = worldState.getTerrainHeightMap();

		// unlike Elevation Adjustments, smoothing power is not affected by
		// worldsize.
		float pow = power * MAX_POWER_SCALE * (isLowering ? -1 : 1);
		float rad = radius * MAX_AOE_SCALE * worldState.getSize();

		System.out.printf("rad = %f, pow=%f\n", rad, pow);
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
					// System.out.println("Looking for neightbors.\n\n");

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
					float cp = (1 - pow);// * calculateLocalPower(rad, 1f, xx, yy); //power falls off as a cosine of the distance.
					float oldHeight = heightMap.getTrueHeightAtPoint(xx, yy);

					heightMap.setHeightAtPoint((neighAverage * (1-cp)) + (oldHeight * cp), xx, yy);
				}
			}
		}
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(xMin, yMin), new Point(xMax, yMax)));
	}

	private float calculateLocalPower(float radius, float multiplier, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return multiplier * yVal;
	}

}
