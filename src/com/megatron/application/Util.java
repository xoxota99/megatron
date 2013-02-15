package com.megatron.application;

import com.jme3.asset.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.*;

public class Util {
	public static Material getWireframeMaterial(AssetManager assetManager, ColorRGBA c) {
		Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m.setColor("Color", c);
		m.getAdditionalRenderState().setWireframe(true);
		m.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return m;
	}

}
