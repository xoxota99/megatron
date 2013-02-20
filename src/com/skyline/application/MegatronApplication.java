package com.skyline.application;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;
import java.util.*;
import java.util.List;

import com.jme3.app.*;
import com.jme3.bounding.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.post.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.heightmap.*;
import com.jme3.texture.*;
import com.jme3.texture.Image;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.plugins.*;
import com.jme3.util.*;
import com.jme3.water.*;
import com.skyline.application.events.*;
import com.skyline.application.events.Event;
import com.skyline.application.state.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

/**
 * 
 * @author philippd
 * 
 */
public class MegatronApplication extends SimpleApplication implements ActionListener, Observer {

	private static boolean showSettings = true;

	protected static final String INPUT_GLOBAL_SMOOTH = MegatronApplication.class.getName() + ".INPUT_GLOBAL_SMOOTH";
	// protected static final String INPUT_GENERATE_TERRAIN =
	// MegatronApplication.class.getName() + ".INPUT_GENERATE_TERRAIN";
	protected static final String INPUT_GLOBAL_ERODE = MegatronApplication.class.getName() + ".INPUT_GLOBAL_ERODE";
	protected static final String INPUT_RESET = MegatronApplication.class.getName() + ".INPUT_RESET";
	// protected static final String INPUT_GLOBAL_NORMALIZE =
	// MegatronApplication.class.getName() + ".INPUT_GLOBAL_NORMALIZE";
	protected static final String INPUT_WIREFRAME = MegatronApplication.class.getName() + ".INPUT_WIREFRAME";
	protected static final String INPUT_GLOBAL_RAISE = MegatronApplication.class.getName() + ".INPUT_GLOBAL_RAISE";
	protected static final String INPUT_GLOBAL_LOWER = MegatronApplication.class.getName() + ".INPUT_GLOBAL_LOWER";
	// protected static final String INPUT_GLOBAL_ZERO =
	// MegatronApplication.class.getName() + ".INPUT_GLOBAL_ZERO";

	// private int modifiers = 0;
	// private TerrainToolMode toolMode = TerrainToolMode.NONE;
	// private boolean toolChanged;

	private long seed = System.currentTimeMillis();
	// private int worldState.getSize() = 256;

	// Scale the terrain quad x and z values.
	private float worldRenderScale = 5f;
	// Scale the terrain quad Y value.
	private float heightScale = worldRenderScale / 2;

	// When generating the terrain, the patch size to use. Must be smaller than
	// worldState.getSize(), must be a power of 2, plus 1.
	private int patchSize = 1024;

	// When we generate a terrain, the highest point will be normalized to this.
	// protected static final float INITIAL_MAX_HEIGHT = 64f;

	// terrain can never be higher than this.
	// private float TERRAIN_MAX = 256f;

	// terrain can never be lower than this.
	// public static float TERRAIN_MIN = -50f;

	// For "continuous" commands (ie: hold down the key), these track the state
	// of the command.
	private boolean raiseTerrain = false;
	private boolean lowerTerrain = false;
	private boolean globalSmoothing = false;

	// private AbstractHeightMap terrainHeightMap;
	private AbstractHeightMap originalHeightMap; // a "backup" of the
	// heightMap,
	// so we can reset it.
	private TerrainQuad terrain;
	private Material terrainMat;
	private Material terrainWireframeMat;

	private boolean showWireframe = false;

	// private float maxHeight = INITIAL_MAX_HEIGHT; // During auto-generation
	// and
	// normalizing,
	// don't create terrain higher than this.

	// Where is the sun?
	private Vector3f sunDir = new Vector3f(-0.4790551f, -0.39247334f, -0.7851566f);
	private Geometry water;

	// private TerrainToolMode[] toolSet = { TerrainToolMode.NONE };
	// private int currentTool;
	private WorldState worldState;

	// private ToolsetAppState toolsetAppState = null;

	public static void main(String... args) {
		SimpleApplication app = new MegatronApplication();
		app.setShowSettings(showSettings);
		if (!showSettings) {
			AppSettings settings = getAppSettings();
			app.setSettings(settings);
			app.setDisplayStatView(false);
			// app.setDisplayFps(true);
		}
		app.start();
	}

	private static class DisplayModeSorter implements Comparator<DisplayMode> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(DisplayMode a, DisplayMode b) {
			// Width
			if (a.getWidth() != b.getWidth()) {
				return (a.getWidth() > b.getWidth()) ? 1 : -1;
			}
			// Height
			if (a.getHeight() != b.getHeight()) {
				return (a.getHeight() > b.getHeight()) ? 1 : -1;
			}
			// Bit depth
			if (a.getBitDepth() != b.getBitDepth()) {
				return (a.getBitDepth() > b.getBitDepth()) ? 1 : -1;
			}
			// Refresh rate
			if (a.getRefreshRate() != b.getRefreshRate()) {
				return (a.getRefreshRate() > b.getRefreshRate()) ? 1 : -1;
			}
			// All fields are equal
			return 0;
		}
	}

	public static AppSettings getAppSettings() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		DisplayMode[] modes = device.getDisplayModes();
		Arrays.sort(modes, new DisplayModeSorter());

		DisplayMode mode = modes[modes.length - 1]; // highest possible
													// resolution.
													// for (DisplayMode m :
													// modes) {
		// System.out.printf("%dx%dx%d @ %d\n", m.getWidth(), m.getHeight(),
		// m.getBitDepth(), m.getRefreshRate());
		// }
		AppSettings settings = new AppSettings(true);
		settings.setSamples(16);
		settings.setFullscreen(true);
		settings.setBitsPerPixel(mode.getBitDepth());
		settings.setFrequency(mode.getRefreshRate());
		settings.setResolution(mode.getWidth(), mode.getHeight());
		// System.out.println(mode.getRefreshRate());
		return settings;
	}

	public void onAction(String name, boolean pressed, float tpf) {
		globalSmoothing = false;
		if (name.equals(INPUT_GLOBAL_SMOOTH)) {
			globalSmoothing = pressed;
		} else if (pressed && name.equals(INPUT_GLOBAL_ERODE)) {
			erode();
		} else if (pressed && name.equals(INPUT_RESET)) {
			resetTerrain();
			// } else if (pressed && name.equals(INPUT_GLOBAL_ZERO)) {
			// setupTerrain();
			// } else if (pressed && name.equals(INPUT_GLOBAL_NORMALIZE)) {
			// normalizeDown(); // normalize down by 10%
		} else if (pressed && name.equals(INPUT_WIREFRAME)) {
			toggleWireframe(); // normalize down by 10%
		} else if (name.equals(INPUT_GLOBAL_RAISE)) {
			raiseTerrain = pressed; // raise all terrain by 1.
		} else if (name.equals(INPUT_GLOBAL_LOWER)) {
			lowerTerrain = pressed; // lower all terrain by 1.
		}
	}

	private void setupKeys() {
		inputManager.addMapping(INPUT_GLOBAL_SMOOTH, new KeyTrigger(KeyInput.KEY_EQUALS));
		inputManager.addListener(this, INPUT_GLOBAL_SMOOTH);
		// inputManager.addMapping(INPUT_GENERATE_TERRAIN, new
		// KeyTrigger(KeyInput.KEY_G));
		// inputManager.addListener(this, INPUT_GENERATE_TERRAIN);
		inputManager.addMapping(INPUT_GLOBAL_ERODE, new KeyTrigger(KeyInput.KEY_E));
		inputManager.addListener(this, INPUT_GLOBAL_ERODE);
		inputManager.addMapping(INPUT_RESET, new KeyTrigger(KeyInput.KEY_R));
		inputManager.addListener(this, INPUT_RESET);
		// inputManager.addMapping(INPUT_GLOBAL_ZERO, new
		// KeyTrigger(KeyInput.KEY_MINUS));
		// inputManager.addListener(this, INPUT_GLOBAL_ZERO);
		// inputManager.addMapping(INPUT_GLOBAL_NORMALIZE, new
		// KeyTrigger(KeyInput.KEY_N));
		// inputManager.addListener(this, INPUT_GLOBAL_NORMALIZE);
		inputManager.addMapping(INPUT_WIREFRAME, new KeyTrigger(KeyInput.KEY_TAB));
		inputManager.addListener(this, INPUT_WIREFRAME);
		inputManager.addMapping(INPUT_GLOBAL_LOWER, new KeyTrigger(KeyInput.KEY_LBRACKET));
		inputManager.addListener(this, INPUT_GLOBAL_LOWER);
		inputManager.addMapping(INPUT_GLOBAL_RAISE, new KeyTrigger(KeyInput.KEY_RBRACKET));
		inputManager.addListener(this, INPUT_GLOBAL_RAISE);

	}

	// private void resetMaxHeight() {
	// float[] f = heightMap.getHeightMap();
	// for (int i = 0; i < f.length; i++) {
	// if (f[i] > maxHeight) {
	// maxHeight = f[i];
	// }
	// }
	// }

	private void resetTerrain() {
		// System.out.println("Reset!");
		// HeightMap originalHeightMap = worldState.getOriginalHeightMap();
		HeightMap terrainHeightMap = worldState.getTerrainHeightMap();
		int size = originalHeightMap.getSize();
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				try {
					float h = originalHeightMap.getTrueHeightAtPoint(x, z);
					terrainHeightMap.setHeightAtPoint(h, x, z);
				} catch (ArrayIndexOutOfBoundsException aioobx) {
					System.out.printf("Out Of Bounds: (x,z)=(%d,%d), size=%d\n", x, z, size);
				}
			}
		}
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(0, 0), new Point(originalHeightMap.getSize() - 1, originalHeightMap.getSize() - 1)));
		// updateHeightQuad();
	}

	private void modifyGlobalHeights(float amt) {
		HeightMap terrainHeightMap = worldState.getTerrainHeightMap();
		for (int x = 0; x < worldState.getSize() + 1; x++) {
			for (int z = 0; z < worldState.getSize() + 1; z++) {
				float h = terrainHeightMap.getTrueHeightAtPoint(x, z) + amt;
				h = Math.min(WorldState.TERRAIN_MAX, Math.max(h, WorldState.TERRAIN_MIN));
				// This will "flatten" the terrain when it reaches TERRAIN_MIN.
				terrainHeightMap.setHeightAtPoint(h, x, z);
			}
		}

		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(0, 0), new Point(terrainHeightMap.getSize() - 1, terrainHeightMap.getSize() - 1)));
		// updateHeightQuad();

	}

	/**
	 * Re-implementation of heightMap.smooth, that takes into account our
	 * terrain "walls".
	 * 
	 * @param heightMap
	 *            the heightmap to smooth.
	 * @param np
	 *            the "node persistance". 0 means ignore the original height,
	 *            and only use the average. 1 means ignore the average, and only
	 *            use the original height.
	 * @param radius
	 *            the radius to consider for each point, when looking for
	 *            "neighbors".
	 */
	private void smoothGlobalHeight(HeightMap heightMap, float np, int radius) {
		if (np < 0 || np > 1) {
			return;
		}
		if (radius == 0)
			radius = 1;
		int size = heightMap.getSize();
		float[] heightData = heightMap.getHeightMap();
		for (int x = 1; x < size - 1; x++) {
			for (int y = 1; y < size - 1; y++) {
				int neighNumber = 0;
				float neighAverage = 0;
				for (int rx = -radius; rx <= radius; rx++) {
					for (int ry = -radius; ry <= radius; ry++) {
						if (x + rx < 1 || x + rx >= size - 1) {
							continue;
						}
						if (y + ry < 1 || y + ry >= size - 1) {
							continue;
						}
						neighNumber++;
						neighAverage += heightData[(x + rx) + (y + ry) * size];
					}
				}

				neighAverage /= neighNumber;
				float cp = 1 - np;
				heightData[x + y * size] = neighAverage * np + heightData[x + y * size] * cp;
			}
		}
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(0, 0), new Point(heightMap.getSize() - 1, heightMap.getSize() - 1)));
		// updateHeightQuad();
	}

	private void smoothGlobalHeight() {
		HeightMap terrainHeightMap = worldState.getTerrainHeightMap();
		smoothGlobalHeight(terrainHeightMap, .5f, 6);
		// resetMaxHeight();
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(0, 0), new Point(terrainHeightMap.getSize() - 1, terrainHeightMap.getSize() - 1)));
		// updateHeightQuad();
	}

	private void erode() {
		AbstractHeightMap terrainHeightMap = (AbstractHeightMap) worldState.getTerrainHeightMap();
		terrainHeightMap.flatten((byte) 2);
		Terrain.trim(worldState);
		worldState.triggerChangeEvent(new TerrainChangedEvent(this, new Point(0, 0), new Point(terrainHeightMap.getSize() - 1, terrainHeightMap.getSize() - 1)));
		// updateHeightQuad();
	}

	private void toggleWireframe() {
		showWireframe = !showWireframe;
		if (showWireframe) {
			terrain.setMaterial(terrainWireframeMat);
			rootNode.detachChild(water); // hide the water quad, since it's
											// distracting in wireframe view.
		} else {
			terrain.setMaterial(terrainMat);
			rootNode.attachChild(water); // show the water quad.
		}
	}

	/**
	 * Update the TerrainQuad to render the current values of the heightMap.
	 */
	private void updateHeightQuad() {
		// System.out.println("UpdateHeightQuad");
		AbstractHeightMap terrainHeightMap = (AbstractHeightMap) worldState.getTerrainHeightMap();

		List<Vector2f> xz = new ArrayList<Vector2f>();
		List<Float> height = new ArrayList<Float>();
		worldState.setMaxTerrainHeight(0);
		for (int x = 1; x < worldState.getSize(); x++) {
			for (int z = 1; z < worldState.getSize(); z++) {
				float h = terrainHeightMap.getTrueHeightAtPoint(x, z);
				if (h > worldState.getMaxTerrainHeight()) {
					worldState.setMaxTerrainHeight(h);
				}
				Vector2f loc = new Vector2f((x - ((worldState.getSize() + 1) / 2)) * worldRenderScale, (z - ((worldState.getSize() + 1) / 2)) * worldRenderScale);
				if (h != terrain.getHeight(loc)) {
					xz.add(loc);
					height.add(h);
				}
			}
		}
		terrain.setHeight(xz, height);
		terrain.updateModelBound();
	}

	private void updatePopDensityRender() {
		float[][] pop = worldState.getPopDensity();
		float maxPop = worldState.getMaxPop();
		// 1-length-1, to leave the "edges" on.
		Texture popMapTex = (Texture) terrainMat.getParam("AlphaMap").getValue();
		Image aImg = popMapTex.getImage(); // get the Image from the Texture
		ByteBuffer aBuf = aImg.getData(0); // Get the image as a bytebuffer to
											// read/write
		int iW = aImg.getWidth();

		for (int x = 1; x < pop.length - 1; x++) {
			for (int y = 1; y < pop[x].length - 1; y++) {
				int iP = (y * iW + x) * 4; // calculate the point in the buffer
											// for the point we want
				if (iP > aBuf.capacity() - 1) {
					System.err.print("outside buffer!" + iW);
					return;
				}
				aBuf.position(iP);
				// for PopMap, we want to set opacity on layer 2.
				int val = (int) ((pop[x][y] / maxPop) * 0xdd);

				if (val > 0) {
					System.out.printf("at (%d,%d), val = %d\n", x, y, val);
				}
				// aBuf.putInt(0x00ff00ff & val << 8);
				aBuf.put((byte) 0xff); // layer 3
				aBuf.put((byte) val); // <--- PopDensity layer is here.
				aBuf.put((byte) 0xff); // layer 1
				aBuf.put((byte) 0x00); // layer 0
			}
		}
		aImg.setData(aBuf); // set the modified buffer back into the Image
		popMapTex.setImage(aImg); // set the Image back into the Texture
		popMapTex.getImage().setUpdateNeeded();
		terrainMat.setTexture("AlphaMap", popMapTex); // set the Texture back
														// into the Material
		terrain.setMaterial(terrainMat); // set the Material back into the
											// Terrain
	}

	private void setupRandomPopulationMap(int popCenterCount) {
		float[][] pop = worldState.getPopDensity();
		Random r = new Random(System.currentTimeMillis());// worldState.getPopulationSeed());
		for (int i = 0; i < popCenterCount-1; i++) {
			int x = r.nextInt(pop.length);
			int y = r.nextInt(pop[x].length);
			System.out.printf("Putting popDense at (%d,%d)\f", x, y);
			int rad = r.nextInt(128);
			for (int xx = Math.max(x - rad, 1); xx < Math.min(x + rad, pop.length - 1); xx++) {
				for (int yy = Math.max(y - rad, 1); yy < Math.min(y + rad, pop[x].length - 1); yy++) {
					float dist = (float) Math.sqrt(((xx - x) * (xx - x)) + ((yy - y) * (yy - y)));
					if (dist < rad) {
						pop[xx][yy] += rad - dist;
					}
				}
			}
		}
		worldState.setPopDensity(pop);
	}

	private Material setupLightedMaterial() {
		float grassScale = 128;

		Material retval = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
		// retval.setBoolean("useTriPlanarMapping", true);
		retval.setFloat("Shininess", 0.0f);

		// initially, we don't show the popMap. Generate an AlphaMap image.
		BufferedImage bi = Util.getSolidColorImage(0x00ff00ff, worldState.getSize(), worldState.getSize());
		Image img = new AWTLoader().load(bi, false);
		Texture pTex = (Texture) new Texture2D(img);

		// Set the alpha blend between "layers".
		retval.setTexture("AlphaMap", pTex);

		// Grass goes on top. We have to put it on top if we want it to be "hit"
		// by the spotlight cursor.
		Texture grass = (Texture) new Texture2D(new AWTLoader().load(Util.getProceduralGridImage(0x354e30, 0x30492B, 16, 16), false));
		grass.setAnisotropicFilter(1);
		grass.setMinFilter(MinFilter.Trilinear);
//		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		retval.setTexture("DiffuseMap", grass);
		retval.setFloat("DiffuseMap_0_scale", grassScale);

		// Hot Pink PopMap "glow" goes on bottom (masked thanks to AlphaMap)
		// Note: The alpha channel in the diffusion map colors is ignored.
		bi = Util.getSolidColorImage(0xF3816B, worldState.getSize(), worldState.getSize());
		img = new AWTLoader().load(bi, false);
		Texture popMapPink = (Texture) new Texture2D(img);

		popMapPink.setWrap(WrapMode.Repeat);
		retval.setTexture("DiffuseMap_1", popMapPink);
		retval.setFloat("DiffuseMap_1_scale", grassScale);

		return retval;

	}

	private Material setupHeightBasedMaterial() {
		float grassScale = worldState.getSize() / 2; // how may times will this
														// texture
		// repeat across the map?
		float dirtScale = worldState.getSize() / 2;
		float rockScale = worldState.getSize() / 2;

		// TERRAIN TEXTURE material
		Material retval = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
		// retval.setBoolean("useTriPlanarMapping", true);

		// DIRT texture
		Texture dirt = this.assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		// GRASS texture
		Texture grass = this.assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		// ROCK texture
		Texture rock = this.assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
		rock.setWrap(WrapMode.Repeat);

		retval.setTexture("region1ColorMap", grass);
		retval.setVector3("region1", new Vector3f(-10 * heightScale, 200 * heightScale, grassScale));

		retval.setTexture("region2ColorMap", grass);
		retval.setVector3("region2", new Vector3f(-256 * heightScale, 0 * heightScale, grassScale));

		retval.setTexture("region3ColorMap", dirt);
		retval.setVector3("region3", new Vector3f(198 * heightScale, 500 * heightScale, dirtScale));

		retval.setTexture("region4ColorMap", rock);
		retval.setVector3("region4", new Vector3f(198 * heightScale, 500 * heightScale, rockScale));

		retval.setTexture("slopeColorMap", rock);
		retval.setFloat("slopeTileFactor", rockScale);

		retval.setFloat("terrainSize", worldRenderScale * worldState.getSize() + 1);

		// retval.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		// retval.getAdditionalRenderState().setWireframe(true);

		return retval;

	}

	private void setupCamera() {
		this.flyCam.setMoveSpeed(worldState.getSize() * worldRenderScale / 4);
		this.flyCam.setRotationSpeed(4);

		this.getCamera().setLocation(new Vector3f(worldState.getSize() * worldRenderScale / 3, worldState.getSize() * worldRenderScale / 2, worldState.getSize() * worldRenderScale));
		this.getCamera().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
		this.getCamera().setFrustumFar(worldState.getSize() * worldRenderScale * 4);

	}

	/**
	 * Test of the terrain "splatting" system.
	 */
	private void paintTerrain() {

	}

	/**
	 * Setup TerrainQuad with "flat" terrainHeightMap.
	 * 
	 * @param size
	 */
	private void setupTerrain() {
		// get the material

		// terrainMat = setupHeightBasedMaterial();
		terrainMat = setupLightedMaterial();
		terrainWireframeMat = Util.getWireframeMaterial(assetManager, ColorRGBA.Green);

		// get the heightMap.
		AbstractHeightMap terrainHeightMap = null;
		if (terrain != null) {
			rootNode.detachChild(terrain);
		}
		terrainHeightMap = null;

		worldState.setMaxTerrainHeight(WorldState.TERRAIN_MAX_INIT);
		try {
			terrainHeightMap = Terrain.createHeightMap(worldState.getSize(), worldState.getWaterLevel() + 1);

			worldState.setTerrainHeightMap(terrainHeightMap);
			reloadOriginalHeightMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		terrain = new TerrainQuad("terrain", patchSize, worldState.getSize() + 1, terrainHeightMap.getHeightMap());
		terrain.setMaterial(terrainMat);
		terrain.setModelBound(new BoundingBox());
		terrain.updateModelBound();
		// terrain.setLocalTranslation(0, -100, 0);
		// terrain.setLocalScale(1f, 1f, 1f);
		terrain.setLocalTranslation(0, 0, 0);
		terrain.setLocalScale(worldRenderScale, heightScale, worldRenderScale);

		// setup LOD control.
		// We don't use LOD control (yet), because it makes the water quad poke
		// through the sides of the terrain from a distance.

		// TerrainLodControl control = new TerrainLodControl(terrain,
		// this.getCamera());
		// control.setLodCalculator(new DistanceLodCalculator(patchSize, 3f));
		// terrain.addControl(control);

		// terrain.setMaterial(terrainMat);

		rootNode.attachChild(terrain);

	}

	private void reloadOriginalHeightMap() {
		originalHeightMap = new AbstractHeightMap() {
			@Override
			public boolean load() {
				float[] fromData = worldState.getTerrainHeightMap().getHeightMap();
				this.size = worldState.getTerrainHeightMap().getSize();
				this.heightData = new float[fromData.length];
				System.arraycopy(fromData, 0, this.heightData, 0, fromData.length);
				return true;
			}
		};
		originalHeightMap.load();
	}

	private void setupWater() {
		setupWaterQuad();
		// setupWaterFilter();
	}

	private void setupWaterQuad() {
		SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
		waterProcessor.setReflectionScene(rootNode);
		waterProcessor.setDebug(false);
		waterProcessor.setLightPosition(sunDir.multLocal(-400));
		waterProcessor.setRefractionClippingOffset(-100f);

		// setting the water plane
		// Vector3f waterLocation=new Vector3f(0,-20,0);
		waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, 0)); // sets the
																// plane of
																// reflection

		waterProcessor.setWaterColor(ColorRGBA.Blue);
		// waterProcessor.setDebug(true);
		// lower render size for higher performance
		waterProcessor.setRenderSize(128, 128);
		// raise depth to see through water
		waterProcessor.setWaterDepth(4);
		// lower the distortion scale if the waves appear too strong
		waterProcessor.setDistortionScale(0.1f);
		waterProcessor.setWaterTransparency(0f);
		// lower the speed of the waves if they are too fast
		waterProcessor.setWaveSpeed(0.05f);

		Quad quad = new Quad(worldState.getSize() * worldRenderScale * 0.99f, worldState.getSize() * worldRenderScale * 0.99f);

		// the texture coordinates define the general size of the waves
		quad.scaleTextureCoordinates(new Vector2f(6f, 6f));

		water = new Geometry("water", quad);
		water.setShadowMode(ShadowMode.Receive);
		water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
		water.setMaterial(waterProcessor.getMaterial());
		water.setLocalTranslation(-worldRenderScale * worldState.getSize() * 0.99f / 2, worldState.getWaterLevel(), worldRenderScale * worldState.getSize() * 0.99f / 2);
		// water.setLocalScale(scale*size, 0, scale*size);

		rootNode.attachChild(water);

		viewPort.addProcessor(waterProcessor);

	}

	private void setupWaterFilter() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		final WaterFilter water = new WaterFilter(rootNode, sunDir);
		// water.setWaterHeight(20);
		// water.setUseFoam(false);
		// water.setFoamHardness(5f);
		water.setCenter(Vector3f.UNIT_Y.mult(-50)); // This is not TERRAIN_MIN,
													// because it's in world
													// coordinates.
		water.setRadius(worldRenderScale * worldState.getSize());
		// water.setUseRipples(false);
		// water.setDeepWaterColor(ColorRGBA.Brown);
		// water.setWaterColor(ColorRGBA.Blue.mult(2.0f));
		water.setWaterTransparency(0.01f);
		// water.setMaxAmplitude(0.3f);
		water.setWaveScale(0.005f);
		water.setSpeed(0.7f);
		// water.setShoreHardness(10.0f);
		// water.setRefractionConstant(0.2f);
		// water.setShininess(0.3f);
		// water.setSunScale(10.0f);
		// water.setColorExtinction(new Vector3f(10.0f, 20.0f, 30.0f));
		water.setReflectionMapSize(256);
		water.setNormalScale(2.5f);
		fpp.addFilter(water);
		viewPort.addProcessor(fpp);

	}

	private void setupSky() {
		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
	}

	private void setupLight() {
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(sunDir);
		// sun.setColor(ColorRGBA.White.clone().multLocal(worldRenderScale));
		sun.setColor(ColorRGBA.White.mult(2f));
		rootNode.addLight(sun);

//		AmbientLight ambLight = new AmbientLight();
//		ambLight.setColor(ColorRGBA.White.mult(2f));
//		rootNode.addLight(ambLight);

		// PssmShadowRenderer pssmRenderer = new
		// PssmShadowRenderer(assetManager, 1024, 3);
		// pssmRenderer.setDirection(sunDir.normalizeLocal()); // light
		// direction
		// viewPort.addProcessor(pssmRenderer);
	}

	@Override
	public void simpleInitApp() {
		worldState = new WorldState(seed);
		worldState.addObserver(this);

		stateManager.attach(new CameraControlAppState()); // modify the camera
															// controls..
		stateManager.attach(new ToolAppState());

		// setupHintText();
		setupKeys(); // G for grid, Equals (=) for smoothing.
		setupCamera();
		setupLight();
		setupSky();
		// setupMaterial();

		setupTerrain();
		setupRandomPopulationMap(5);
		updatePopDensityRender();

		setupWater();
		// setupMesh();
		// setupRiver();

		// setupGrid(Vector3f.ZERO, this.worldState.getSize(), ColorRGBA.Blue);
		// setupArrows(Vector3f.ZERO, 256);
		// if (showGrid) {
		// rootNode.attachChild(grid);
		// }

		// setupDoF();
	}

	public void simpleUpdate(float tpf) {
		if (globalSmoothing) {
			smoothGlobalHeight();
		} else if (raiseTerrain) {
			modifyGlobalHeights(2);// tpf * worldScale);
		} else if (lowerTerrain) {
			modifyGlobalHeights(-2);// tpf * -worldScale);
		}
	}

	public float getWorldRenderScale() {
		return worldRenderScale;
	}

	public void setWorldScale(float worldScale) {
		this.worldRenderScale = worldScale;
	}

	public float getHeightScale() {
		return heightScale;
	}

	public void setHeightScale(float heightScale) {
		this.heightScale = heightScale;
	}

	public WorldState getWorldState() {
		return worldState;
	}

	public void setWorldState(WorldState worldState) {
		this.worldState = worldState;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof WorldState) {
			if (arg != null && arg instanceof Event) {
				switch (((Event) arg).getEventType()) {
				case TERRAIN:
					TerrainChangedEvent evt = (TerrainChangedEvent) arg;
					// System.out.println("Terrain Event: isNewTerrain=" +
					// evt.isNewTerrain());
					if (evt.isNewTerrain()) {
						reloadOriginalHeightMap();
					}
					updateHeightQuad();
					break;
				case POPULATION:
					break;
				case ROADS:
					break;
				case ZONING:
					break;
				case CONSTRUCTION:
					break;
				default:
					System.out.println("Whoah. Unknown event type: " + ((Event) arg).getEventType().toString());
				}
			}
		}
	}
}
