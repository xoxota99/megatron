package com.skyline.application.tools.population;

import java.awt.*;

import com.jme3.math.*;
import com.skyline.application.events.*;
import com.skyline.application.i18n.*;
import com.skyline.application.state.*;
import com.skyline.application.tools.*;
import com.skyline.model.*;
import com.skyline.population.*;

public class LocalPopDensityTool extends LocalTool {
	protected static float MAX_RADIUS = 0.125f; // Max tool radius
	protected static float MAX_POWER = 0.005f; // Max tool power
	protected static float MIN_RADIUS = 0.01f; // Minimum tool radius
	protected static float MIN_POWER = 0.001f; // Minimum tool power

	public LocalPopDensityTool() {
		super(Messages.getString(LocalPopDensityTool.class, "toolName"), Messages.getString(LocalPopDensityTool.class, "toolTip"), CursorType.CONE);
		setPower(0.25f);
		setRadius(1f);
	}

	/**
	 * Adjust the population density, at the given location (loc), in the radius
	 * and power of the tool.
	 */
	@Override
	public void execute(WorldState worldState, int x, int y, int modifiers) {
		// System.out.println("execute");
//		y=worldState.getSize()-y;
		boolean isLowering = (modifiers & ToolAppState.ModifierKey.ALT) != 0;
		float[][] popDensity = worldState.getPopDensity();
//		float[][] newPopDensity = new float[popDensity.length][popDensity[0].length];

		float pow = (MIN_POWER + (power * (MAX_POWER - MIN_POWER))) * worldState.getSize() * (isLowering ? -1 : 1);
		float rad = (MIN_RADIUS + (radius * (MAX_RADIUS - MIN_RADIUS))) * worldState.getSize();

		// System.out.printf("rad = %f, pow=%f\n", rad, pow);
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
					float delta = getValueDelta(rad, pow, dx, dy);
					float oldVal = popDensity[xx][yy];
					float newVal = Math.max(0, oldVal + delta); // TODO: bracket.

					// System.out.printf("at (%d,%d), oH=%f, nH=%f\n", xx, yy,
					// oldHeight, newHeight);
					popDensity[xx][yy] = newVal;
				}
			}
		}

		worldState.setPopDensity(popDensity);	//force Refresh of maxPop.
		Population.trim(worldState);	//Edge Case: We're "trimming" AFTER we set maxPop (by calling setPopDensity). Problem?
		worldState.triggerChangeEvent(new PopulationEvent(this, new Point(xMin, yMin), new Point(xMax, yMax)));
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
	private float getValueDelta(float radius, float multiplier, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return multiplier * yVal;
	}

	@Override
	public boolean isContinuous() {
		return true;
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
