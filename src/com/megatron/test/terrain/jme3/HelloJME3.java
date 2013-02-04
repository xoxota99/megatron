package com.megatron.test.terrain.jme3;

import java.util.*;
import java.util.logging.*;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.asset.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.post.*;
import com.jme3.post.filters.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.grid.*;
import com.jme3.terrain.geomipmap.lodcalc.*;
import com.jme3.terrain.heightmap.*;
import com.jme3.terrain.noise.basis.*;
import com.jme3.terrain.noise.filter.*;
import com.jme3.terrain.noise.fractal.*;
import com.jme3.terrain.noise.modulator.*;
import com.jme3.texture.*;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.*;
import com.megatron.model.*;
import com.megatron.terrain.*;

public class HelloJME3 extends SimpleApplication {

	private int patchSize = 65;
	private boolean hasWireframe = false;
	// private Material wfMat;
	private Spatial wireframe;
	private TerrainQuad terrain;
	private AbstractHeightMap terrainHeight;
	private City city;

	public HelloJME3() {
		// setShowSettings(false);
		// setDisplayStatView(false);
		// setDisplayFps(false);
		// AppSettings settings = new AppSettings(true);
		// settings.setFullscreen(true);
		// settings.setWidth(1280);
		// settings.setHeight(800);
		// setSettings(settings);
	}

	private void setupKeys() {
		System.out.println("setupkeys");
		inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addListener(actionListener, "wireframe");
		inputManager.addMapping("smooth", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addListener(actionListener, "smooth");
	}

	private ActionListener actionListener = new ActionListener() {

		public void onAction(String binding, boolean keyPressed, float tpf) {

			if (binding.equals("wireframe") && keyPressed) {
				// System.out.println(binding+": keyPressed="+keyPressed+", tpf="+tpf);
				// Node nd = ((Node) (rootNode.getChild("tNode")));
				hasWireframe = !hasWireframe;
				if (hasWireframe) {
					// System.out.println("attachWireframe");
					rootNode.attachChild(wireframe);
				} else {
					// System.out.println("detachWireframe");
					rootNode.detachChildNamed("wireframe");
				}
			} else if (binding.equals("smooth")) {
				// Node nd = ((Node) (rootNode.getChild("tNode")));
				if (rootNode.getChild("terrain") != null) {
					int size = city.getSize() + 1;
					// terrain.getHeightMap();
					terrainHeight.smooth(1f); // severe smoothing.
					System.out.println("smoothed.");
					List<Vector2f> xz = new ArrayList<Vector2f>();
					List<Float> height = new ArrayList<Float>();
					float[] f = terrainHeight.getHeightMap();
					for (int i = 0; i < f.length; i++) {
						int x = i % size - (size / 2);
						int y = (i - x) / size - (size / 2);
						xz.add(new Vector2f(x, y));
						height.add(f[i]);
					}
					terrain.setHeight(xz, height);
					terrain.updateModelBound();
				}
			}

		}
	};

	public static void main(String[] args) {
		// java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
		HelloJME3 app = new HelloJME3();
		app.start();
	}

	private Material loadMaterial(int size) {
		return loadHeightBasedMaterial(size);
	}

	private Material loadGrassMaterial(int size) {
		TextureKey key = new TextureKey("assets/town/grass.jpg", false);
		key.setGenerateMips(true);
		Texture tex = assetManager.loadTexture(key);

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
		mat.setTexture("ColorMap", tex);
		return mat;
	}

	private Material loadHeightBasedMaterial(int size) {
		float dirtScale = 128;
		float grassScale = 128;
		float rockScale = 128;

		// TERRAIN TEXTURE material
		Material retval = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

		// DIRT texture
		Texture dirt = this.assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		// GRASS texture
		Texture grass = this.assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		// ROCK texture
		Texture rock = this.assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
		rock.setWrap(WrapMode.Repeat);

		retval.setTexture("region1ColorMap", dirt);
		retval.setVector3("region1", new Vector3f(-20, 20, dirtScale));

		retval.setTexture("region2ColorMap", grass);
		retval.setVector3("region2", new Vector3f(0, 150, grassScale));

		retval.setTexture("region3ColorMap", rock);
		retval.setVector3("region3", new Vector3f(140, 260, rockScale));

		retval.setTexture("region4ColorMap", rock);
		retval.setVector3("region4", new Vector3f(198, 260, rockScale));

		retval.setTexture("slopeColorMap", rock);
		retval.setFloat("slopeTileFactor", 32);

		retval.setFloat("terrainSize", size + 1);

		// retval.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		// retval.getAdditionalRenderState().setWireframe(true);

		return retval;

	}

	@Override
	public void simpleInitApp() {

		setupKeys();
		city = new City();
		// load sky
		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

		Material mat = loadMaterial(city.getSize());

		// Node tNode = new Node("tNode"); // node for terrain stuff.

		terrain = setupTerrain(city.getWaterLevel(), city.getSize(), city.getSeed());
		terrain.setMaterial(mat);

		wireframe = terrain.clone();
		wireframe.setName("wireframe");
		Material wfMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		wfMat.setColor("Color", ColorRGBA.Green);
		wfMat.getAdditionalRenderState().setWireframe(true);

		wireframe.setMaterial(wfMat);
		Vector3f trans = wireframe.getLocalTranslation();
		trans.y++;
		wireframe.setLocalTranslation(trans);
		rootNode.attachChild(terrain);
		if (hasWireframe) {
			rootNode.attachChild(wireframe);
		}

		// rootNode.attachChild(tNode);

		// ..

		// Create sun
		// DirectionalLight sun = new DirectionalLight();
		// sun.setDirection(new Vector3f(-0.4790551f, -0.39247334f,
		// -0.7851566f));
		// sun.setColor(ColorRGBA.White.clone().multLocal(2));
		// terrain.addLight(sun);

		setupCamera();

		this.viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		setupFog();

		float scaling = 2f;
		attachCoordinateAxes(Vector3f.ZERO, 255);
		attachGrid(Vector3f.ZERO, (int) (city.getSize() * scaling), ColorRGBA.Blue);
	}

	private AbstractHeightMap setupRiver(AbstractHeightMap heightMap, float waterLevel, int size, long seed) {

		RiverFactory rf = new RiverFactory(waterLevel, seed);
		rf.createRiver(heightMap.getHeightMap(), size);
		return heightMap;
	}

	private TerrainQuad setupTerrain(float waterLevel, int size, long seed) {
		terrainHeight = getFaultHeightMap(size, seed);

		// terrainHeight.smooth(1f,10);
		terrainHeight.normalizeTerrain(30f);
		float[] mmh = terrainHeight.findMinMaxHeights();
		System.out.printf("min=%f, max=%f\n", mmh[0], mmh[1]);

		terrainHeight = setupRiver(terrainHeight, waterLevel, size + 1, seed);

		TerrainQuad retval = new TerrainQuad("terrain", patchSize, size + 1, terrainHeight.getHeightMap());
		retval.setLocalTranslation((size + 1) / 2, 0, (size + 1) / 2);
		// retval.setLocalScale(2f, 2f, 2f); //embiggen

		// add LOD control.
		TerrainLodControl control = new TerrainLodControl(retval, this.getCamera());
		control.setLodCalculator(new DistanceLodCalculator(patchSize, 1.7f));
		retval.addControl(control);

		return retval;
	}

	private AbstractHeightMap getFaultHeightMap(int size, long seed) {
		AbstractHeightMap heightmap = null;
		try {
			heightmap = new FaultHeightMap(size + 1, 1000, FaultHeightMap.FAULTTYPE_COSINE, FaultHeightMap.FAULTSHAPE_LINE, 1f, 20f, seed);

			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return heightmap;
	}

	private AbstractHeightMap getHillHeightMap(int size, long seed) {
		// CREATE HEIGHTMAP
		AbstractHeightMap heightmap = null;
		try {
			heightmap = new HillHeightMap(size + 1, 1000, 50, 100, seed);

			// heightmap = new ImageBasedHeightMap(heightMapImage.getImage(),
			// 1f);
			heightmap.load();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return heightmap;
	}

	private void setupCamera() {
		this.flyCam.setMoveSpeed(50f);
		this.flyCam.setRotationSpeed(3f);
		this.getCamera().setLocation(new Vector3f(90, 140, 250));
		this.getCamera().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
	}

	private void setupFog() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		FogFilter fog = new FogFilter();
		fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1f));
		fog.setFogDistance(155);
		fog.setFogDensity(.5f);
		fpp.addFilter(fog);
		viewPort.addProcessor(fpp);
	}

	public void simpleUpdate(float tpf) {

		// System.out.println(this.getCamera().getDirection());
		// Spatial s = rootNode.getChild("terrain");
		// s.setLocalTranslation(0,0,tpf);
		// s.rotate(0, tpf, 0);
		// s.scale(2, 2, 2);
	}

	private void attachCoordinateAxes(Vector3f pos, int size) {
		Arrow arrow = new Arrow(new Vector3f(size, 0, 0));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

		arrow = new Arrow(new Vector3f(0, size, 0));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

		arrow = new Arrow(new Vector3f(0, 0, size));
		arrow.setLineWidth(4); // make arrow thicker
		putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
	}

	private Geometry putShape(Mesh shape, ColorRGBA color) {
		Geometry g = new Geometry("coordinate axis", shape);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		rootNode.attachChild(g);
		return g;
	}

	private void attachGrid(Vector3f pos, int size, ColorRGBA color) {
		Geometry g = new Geometry("wireframe grid", new Grid(size, size, 0.2f));
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		g.center().move(pos);
		rootNode.attachChild(g);
	}
}