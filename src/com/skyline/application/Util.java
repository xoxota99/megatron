package com.skyline.application;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;
import java.util.List;

import com.jme3.asset.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.texture.*;
import com.jme3.texture.Image;

public class Util {
	public static Material getWireframeMaterial(AssetManager assetManager, ColorRGBA c) {
		Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m.setColor("Color", c);
		m.getAdditionalRenderState().setWireframe(true);
		m.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return m;
	}

	/**
	 * Paint the Splat AlphaMap of a TerrainQuad
	 * 
	 * @param tx
	 *            x location on terrain (0 to terrain size)
	 * @param tz
	 *            z location on terrain (0 to terrain size)
	 * @param terrain
	 *            TerrainQuad to alter
	 * @param rgba
	 *            RGBA value (as an integer) to paint this pixel.
	 */
	public void PaintSplat(TerrainQuad terrain, List<Vector2f> locations, int[] rgba) {
		// byte b1 = (byte) 0xFF;
		// byte b0 = (byte) 0x00;
		assert locations.size()==rgba.length : "locations and rgba must be the same length!";
		
		Material cMat = terrain.getMaterial(); // get the material from terrain
		Texture aTex = (Texture) cMat.getParam("AlphaMap").getValue(); // get
																		// the
																		// AlphaMap
																		// texture
		Image aImg = aTex.getImage(); // get the Image from the Texture
		aImg = aImg.clone();
		ByteBuffer aBuf = aImg.getData(0); // Get the image as a bytebuffer to
											// read/write
		int iW = aImg.getWidth();
		for (int i=0;i<locations.size();i++) {
			Vector2f v = locations.get(i);
			int iP = (int)(v.y * iW + v.x) * 4; // calculate the point in the buffer
											// for the point we want
			if (iP > aBuf.capacity() - 1) {
				System.err.print("outside buffer!" + iW);
				return;
			}
			aBuf.position(iP);
			aBuf.putInt(rgba[i]);
		}
		aImg.setData(aBuf); // set the modified buffer back into the Image
		aTex.setImage(aImg); // set the Image back into the Texture
		aTex.getImage().setUpdateNeeded();
		cMat.setTexture("AlphaMap", aTex); // set the Texture back into the
											// Material
		terrain.setMaterial(cMat); // set the Material back into the Terrain
	}

	public static BufferedImage getSolidColorImage(int rgba, int width, int height) {
		BufferedImage retval = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) retval.getGraphics();
		g2d.setBackground(new Color(rgba,true));
		g2d.setColor(new Color(rgba,true));
		g2d.clearRect(0,0,width,height);
		return retval;
	}

	public static BufferedImage getProceduralGridImage(int gridColor, int bgColor, int width, int height) {
		BufferedImage retval = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) retval.getGraphics();
		g2d.setBackground(new Color(bgColor,true));
		g2d.setColor(new Color(gridColor,true));
		g2d.clearRect(0,0,width,height);
		
		for(int i=0;i<width;i++){
			retval.setRGB(i, 0, gridColor);
		}
		for(int i=0;i<height;i++){
			retval.setRGB(0, i, gridColor);
		}
		return retval;
	}
}
