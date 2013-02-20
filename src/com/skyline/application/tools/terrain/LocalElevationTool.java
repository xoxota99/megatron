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

/**
 * A Terrain {@link LocalTool} that affects terrain elevation in a specific
 * radius, by a specific power (ie: Cone cursor).
 * 
 * @author philippd
 * 
 */
public class LocalElevationTool extends LocalTool {
	protected static float MAX_AOE_SCALE = 0.125f; // Max tool radius
	protected static float MAX_POWER_SCALE = 0.005f; // Max tool power
	protected static float MIN_AOE_SCALE = 0.01f; // Minimum tool radius
	protected static float MIN_POWER_SCALE = 0.001f; // Minimum tool power

	public LocalElevationTool() {
		super(Messages.getString(LocalElevationTool.class, "toolName"), Messages.getString(LocalElevationTool.class, "toolTip"), CursorType.CONE);
		setPower(0.25f);
		setRadius(1f);
	}

	/**
	 * Adjust the height of the terrain, at the given location (loc), in the
	 * radius and power of the tool.
	 */
	@Override
	public void execute(WorldState worldState, int x, int y, int modifiers) {
		// System.out.println("execute");
		boolean isLowering = (modifiers & ToolAppState.ModifierKey.ALT) != 0;
		HeightMap heightMap = worldState.getTerrainHeightMap();

		float pow = (MIN_POWER_SCALE + (power * (MAX_POWER_SCALE - MIN_POWER_SCALE))) * worldState.getSize() * (isLowering ? -1 : 1);
		float rad = (MIN_AOE_SCALE + (radius * (MAX_AOE_SCALE - MIN_AOE_SCALE))) * worldState.getSize();

//		System.out.printf("rad = %f, pow=%f\n", rad, pow);
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
					float dH = calculateHeightDelta(rad, pow, dx, dy);
					float oldHeight = heightMap.getTrueHeightAtPoint(xx, yy);
					float newHeight = oldHeight + dH; // TODO: bracket.

					// System.out.printf("at (%d,%d), oH=%f, nH=%f\n", xx, yy,
					// oldHeight, newHeight);
					heightMap.setHeightAtPoint(newHeight, xx, yy);
				}
			}
		}

		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(xMin, yMin), new Point(xMax, yMax)));
		// terrain.adjustHeight(locs, heights);
		// terrain.updateModelBound();
		// terrain.forceRefresh(true, true, true);
	}

	/**
	 * In a given radius, for a given multiplier, calculate the projected height
	 * delta at given x/y coordinates, as a cosin function of distance.
	 * 
	 * @param radius
	 * @param multiplier
	 * @param x
	 * @param z
	 * @return
	 */
	private float calculateHeightDelta(float radius, float multiplier, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return multiplier * yVal;
	}

	@Override
	public boolean isContinuous() {
		return true;
	}

}
