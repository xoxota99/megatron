package com.skyline.application.tools;

import com.jme3.scene.*;

/**
 * Cursor for setting up a "radial" population Center.
 * @author philippd
 *
 */
public class RadialPopCursor extends AbstractCursor{

	public RadialPopCursor(float power, float radius, int maxRenderHeight, int maxRenderRadius, float renderScale) {
		super(power, radius, maxRenderHeight, maxRenderRadius, renderScale);
	}

	@Override
	public int getRenderHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRenderRadius() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void render(Node root) {
		// TODO Auto-generated method stub
		
	}

}
