package com.megatron.application.tools;

import java.awt.*;

import com.jme3.math.*;
import com.jme3.terrain.heightmap.*;
import com.megatron.application.*;
import com.megatron.application.events.*;
import com.megatron.application.i18n.*;

/**
 * A Terrain {@link LocalTool} that affects terrain elevation in a specific
 * radius, by a specific power (ie: Cone cursor).
 * 
 * @author philippd
 * 
 */
public class LocalElevationTool extends LocalTool {

	public LocalElevationTool() {
		super(Messages.getString(LocalElevationTool.class, "toolName"), Messages.getString(LocalElevationTool.class, "toolTip"), CursorType.CONE);
	}

	/**
	 * Adjust the height of the terrain, at the given location (loc), in the
	 * radius and power of the tool.
	 * 
	 */
	@Override
	public void execute(WorldState worldState, int x, int y) {
		boolean isLowering = worldState.getInputModifier(WorldState.ALT);
		HeightMap heightMap = worldState.getTerrainHeightMap();

		float mult = power * (isLowering ? -1 : 1);
		for (int yy = -radius; yy < radius; yy++) {
			for (int xx = -radius; xx < radius; xx++) {

				int locX = x + xx;
				int locY = y + yy;

				// see if it is in the radius of the tool
				if (Math.sqrt((xx * xx) + (yy * yy)) <= radius) {
					float dH = calculateHeightDelta(radius, mult, xx, yy);
					float oldHeight = heightMap.getTrueHeightAtPoint(locX, locY);
					float newHeight = oldHeight + dH; // TODO: bracket.

					heightMap.setHeightAtPoint(newHeight, locX, locY);
				}
			}
		}

		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(x - radius, y - radius), new Point(x + radius, y + radius)));
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

}
