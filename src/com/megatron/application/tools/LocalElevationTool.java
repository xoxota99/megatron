package com.megatron.application.tools;

import java.awt.*;

import com.jme3.math.*;
import com.jme3.terrain.heightmap.*;
import com.megatron.application.*;
import com.megatron.application.events.*;
import com.megatron.application.i18n.*;
import com.megatron.model.*;
import com.megatron.terrain.*;

/**
 * A Terrain {@link LocalTool} that affects terrain elevation in a specific
 * radius, by a specific power (ie: Cone cursor).
 * 
 * @author philippd
 * 
 */
public class LocalElevationTool extends LocalTool {
	protected static float MAX_AOE_SCALE=0.125f;	//Max tool radius is 1/8 of the entire world(!)
	protected static float MAX_POWER_SCALE=0.03125f;	//Max tool power is 1/32 of the entire world size
	
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
	public void execute(WorldState worldState, int x, int y) {
		// System.out.println("execute");
		boolean isLowering = worldState.getInputModifier(WorldState.ALT);
		HeightMap heightMap = worldState.getTerrainHeightMap();

		float pow = power * MAX_POWER_SCALE * worldState.getSize() * (isLowering ? -1 : 1);
		float rad = radius * MAX_AOE_SCALE * worldState.getSize();

		System.out.printf("rad = %f, pow=%f\n", rad, pow);
		int xMin = (int) Math.floor(Math.max(x - rad, 1));
		int xMax = (int) Math.floor(Math.min(x + rad, worldState.getSize()-1));
		int yMin = (int) Math.floor(Math.max(y - rad, 1));
		int yMax = (int) Math.floor(Math.min(y + rad, worldState.getSize()-1));
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
