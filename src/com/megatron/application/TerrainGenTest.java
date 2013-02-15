package com.megatron.application;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.jme3.app.*;
import com.jme3.font.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.post.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.shadow.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.heightmap.*;
import com.jme3.texture.*;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.*;
import com.jme3.water.*;
import com.megatron.application.events.Event;
import com.megatron.application.state.*;
import com.megatron.application.tools.*;

/**
 * 
 * @author philippd
 * 
 */
public class TerrainGenTest extends SimpleApplication implements ActionListener, Observer {

	private static boolean showSettings = true;

	protected static final String INPUT_GLOBAL_SMOOTH = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_SMOOTH";
	protected static final String INPUT_GENERATE_TERRAIN = TerrainGenTest.class.getName() + ".INPUT_GENERATE_TERRAIN";
	protected static final String INPUT_GLOBAL_ERODE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_ERODE";
	protected static final String INPUT_RESET = TerrainGenTest.class.getName() + ".INPUT_RESET";
	protected static final String INPUT_GLOBAL_NORMALIZE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_NORMALIZE";
	protected static final String INPUT_WIREFRAME = TerrainGenTest.class.getName() + ".INPUT_WIREFRAME";
	protected static final String INPUT_GLOBAL_RAISE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_RAISE";
	protected static final String INPUT_GLOBAL_LOWER = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_LOWER";

	// private int modifiers = 0;
	// private TerrainToolMode toolMode = TerrainToolMode.NONE;
	// private boolean toolChanged;

	private long seed = System.currentTimeMillis();
	// private int worldState.getSize() = 256;

	// Scale the terrain quad x and z values.
	private float worldScale = 5f;
	// Scale the terrain quad Y value.
	private float heightScale = worldScale / 2;

	// When generating the terrain, the patch size to use. Must be smaller than
	// worldState.getSize(), must be a power of 2, plus 1.
	private int patchSize = 1024;

	// When we generate a terrain, the highest point will be normalized to this.
	protected static final float INITIAL_MAX_HEIGHT = 64f;

	// terrain can never be higher than this.
	private float TERRAIN_MAX = 256f;

	// terrain can never be lower than this.
	// public static float TERRAIN_MIN = -50f;

	// For "continuous" commands (ie: hold down the key), these track the state
	// of the command.
	private boolean raiseTerrain = false;
	private boolean lowerTerrain = false;
	private boolean smoothing = false;

	private AbstractHeightMap heightMap;
	private AbstractHeightMap originalHeightMap; // a "backup" of the heightMap,
													// so we can reset it.
	private TerrainQuad terrain;

	private Material terrainMat;
	private Material terrainWireframeMat;

	private boolean showWireframe = false;

	private float maxHeight = INITIAL_MAX_HEIGHT; // During auto-generation and
													// normalizing,
	// don't create terrain higher than this.

	private BitmapText hintText;
	private boolean initialized = false;

	// Where is the sun?
	private Vector3f sunDir = new Vector3f(-0.4790551f, -0.39247334f, -0.7851566f);
	private Geometry water;

	private TerrainToolMode[] toolSet = { TerrainToolMode.NONE, TerrainToolMode.RAISE_LOWER, TerrainToolMode.SMOOTH };
	private int currentTool;
	private WorldState worldState;

	// private ToolsetAppState toolsetAppState = null;

	public static void main(String... args) {
		SimpleApplication app = new TerrainGenTest();
		app.setShowSettings(showSettings);
		if (!showSettings) {
			AppSettings settings = getAppSettings();
			app.setSettings(settings);
			// app.setDisplayStatView(false);
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

		DisplayMode mode = modes[modes.length - 2]; // highest possible
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
		smoothing = false;
		if (name.equals(INPUT_GLOBAL_SMOOTH)) {
			smoothing = pressed;
		} else if (pressed && name.equals(INPUT_GLOBAL_ERODE)) {
			erode();
		} else if (pressed && name.equals(INPUT_GENERATE_TERRAIN)) {
			rootNode.detachChild(terrain);
			seed = System.currentTimeMillis();
			generateRandomTerrain(worldState.getSize(), seed);
			rootNode.attachChild(terrain);
		} else if (pressed && name.equals(INPUT_RESET)) {
			resetTerrain();
		} else if (pressed && name.equals(INPUT_GLOBAL_NORMALIZE)) {
			normalizeDown(); // normalize down by 10%
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
		inputManager.addMapping(INPUT_GENERATE_TERRAIN, new KeyTrigger(KeyInput.KEY_G));
		inputManager.addListener(this, INPUT_GENERATE_TERRAIN);
		inputManager.addMapping(INPUT_GLOBAL_ERODE, new KeyTrigger(KeyInput.KEY_E));
		inputManager.addListener(this, INPUT_GLOBAL_ERODE);
		inputManager.addMapping(INPUT_RESET, new KeyTrigger(KeyInput.KEY_R));
		inputManager.addListener(this, INPUT_RESET);
		inputManager.addMapping(INPUT_GLOBAL_NORMALIZE, new KeyTrigger(KeyInput.KEY_N));
		inputManager.addListener(this, INPUT_GLOBAL_NORMALIZE);
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
		for (int x = 0; x < originalHeightMap.getSize(); x++) {
			for (int z = 0; z < originalHeightMap.getSize(); z++) {
				float h = originalHeightMap.getTrueHeightAtPoint(x, z);
				heightMap.setHeightAtPoint(h, x, z);
			}
		}
		trimTerrain(heightMap);
		updateHeights();
	}

	private void modifyGlobalHeights(float amt) {
		for (int x = 0; x < worldState.getSize() + 1; x++) {
			for (int z = 0; z < worldState.getSize() + 1; z++) {
				float h = heightMap.getTrueHeightAtPoint(x, z) + amt;
				h = Math.min(TERRAIN_MAX, Math.max(h, WorldState.TERRAIN_MIN));
				// This will "flatten" the terrain when it reaches TERRAIN_MIN.
				heightMap.setHeightAtPoint(h, x, z);
			}
		}

		trimTerrain(heightMap);
		updateHeights();

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
		trimTerrain(heightMap);
		updateHeights();
	}

	private void smoothGlobalHeight() {
		smoothGlobalHeight(heightMap, .5f, 6);
		// resetMaxHeight();
		trimTerrain(heightMap);
		updateHeights();
	}

	private void erode() {
		heightMap.flatten((byte) 2);
		trimTerrain(heightMap);
		// resetMaxHeight();
		trimTerrain(heightMap);
		updateHeights();
	}

	private void normalizeDown() {
		maxHeight *= 0.9f;
		System.out.printf("Normalizing to maxHeight %f\n", maxHeight);
		heightMap.normalizeTerrain(maxHeight);
		trimTerrain(heightMap);
		updateHeights();
	}

	private void toggleWireframe() {
		showWireframe = !showWireframe;
		if (showWireframe) {
			terrain.setMaterial(terrainWireframeMat);
			rootNode.detachChild(water);
		} else {
			terrain.setMaterial(terrainMat);
			rootNode.attachChild(water);
		}
	}

	/**
	 * Update the TerrainQuad to render the current values of the heightMap.
	 */
	private void updateHeights() {

		List<Vector2f> xz = new ArrayList<Vector2f>();
		List<Float> height = new ArrayList<Float>();
		maxHeight = 0;
		for (int x = 1; x < worldState.getSize(); x++) {
			for (int z = 1; z < worldState.getSize(); z++) {
				float h = heightMap.getTrueHeightAtPoint(x, z);
				if (h > maxHeight) {
					maxHeight = h;
				}
				Vector2f loc = new Vector2f((x - ((worldState.getSize() + 1) / 2)) * worldScale, (z - ((worldState.getSize() + 1) / 2)) * worldScale);
				if (h != terrain.getHeight(loc)) {
					xz.add(loc);
					height.add(h);
				}
			}
		}
		terrain.setHeight(xz, height);
		terrain.updateModelBound();
	}

	private Material setupMaterial() {
		terrainMat = setupHeightBasedMaterial();
		terrainWireframeMat = Util.getWireframeMaterial(assetManager, ColorRGBA.Green);
		return showWireframe ? terrainWireframeMat : terrainMat;
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

		retval.setFloat("terrainSize", worldScale * worldState.getSize() + 1);

		// retval.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		// retval.getAdditionalRenderState().setWireframe(true);

		return retval;

	}

	private void trimTerrain(HeightMap heightMap) {
		// drop the edges.
		for (int x = 0; x < worldState.getSize() + 1; x++) {
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, x, 0);
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, x, worldState.getSize());
		}
		for (int z = 0; z < worldState.getSize() + 1; z++) {
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, 0, z);
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, worldState.getSize(), z);
		}

	}

	private void setupCamera() {
		this.flyCam.setMoveSpeed(worldState.getSize() * worldScale / 4);
		this.flyCam.setRotationSpeed(4);

		this.getCamera().setLocation(new Vector3f(worldState.getSize() * worldScale / 3, worldState.getSize() * worldScale / 2, worldState.getSize() * worldScale));
		this.getCamera().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
		this.getCamera().setFrustumFar(worldState.getSize() * worldScale * 4);

	}

	private void setupTerrain(int size) {
		if (terrain != null)
			rootNode.detachChild(terrain);
		heightMap = null;
		maxHeight = INITIAL_MAX_HEIGHT;
		try {
			heightMap = new AbstractHeightMap() {

				@Override
				public boolean load() {
					this.size = worldState.getSize() + 1;
					this.heightData = new float[(worldState.getSize() + 1) * (worldState.getSize() + 1)];
					// System.out.println("Setting up heightData with "+heightData.length+" points.");
					for (int i = 0; i < heightData.length; i++) {
						heightData[i] = 1f;
					}
					return true;
				}
			};
			heightMap.load();
			// System.out.println("Calling trimTerrain..");
			trimTerrain(heightMap);
			// System.out.println("Calling printHeightMap..");
			// printHeightMap(heightMap);

			// now back it up, for "reset".
			originalHeightMap = new AbstractHeightMap() {
				@Override
				public boolean load() {
					this.size = worldState.getSize() + 1;
					this.heightData = new float[(worldState.getSize() + 1) * (worldState.getSize() + 1)];
					for (int i = 0; i < heightData.length; i++) {
						heightData[i] = 1f;
					}
					return true;
				}
			};

			originalHeightMap.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		terrain = new TerrainQuad("terrain", patchSize, size + 1, heightMap.getHeightMap());

		terrain.setLocalTranslation(0, 0, 0);
		terrain.setLocalScale(worldScale, heightScale, worldScale);

		// setup LOD control.
		// We don't use LOD control (yet), because it makes the water quad poke
		// through the sides of the terrain from a distance.

		// TerrainLodControl control = new TerrainLodControl(terrain,
		// this.getCamera());
		// control.setLodCalculator(new DistanceLodCalculator(patchSize, 3f));
		// terrain.addControl(control);

		terrain.setMaterial(terrainMat);

		rootNode.attachChild(terrain);

	}

	private void generateRandomTerrain(int size, long seed) {
		if (terrain != null)
			rootNode.detachChild(terrain);
		heightMap = null;
		maxHeight = INITIAL_MAX_HEIGHT;
		try {
			// heightMap = new FaultHeightMap(size + 1, 1000,
			// FaultHeightMap.FAULTTYPE_COSINE,
			// FaultHeightMap.FAULTSHAPE_LINE,1f, 20f, seed);

			// heightMap = new FluidSimHeightMap(size+1, 1000, 0f, 256f, .01f,
			// .5f, 10f, 10f, seed);

			// heightMap = new HillHeightMap(size + 1, 1000, 10, size / 2,seed);
			heightMap = new MidpointDisplacementHeightMap(size + 1, 1, .5f, seed);
			// System.out.println("Setting up heightMap with " +
			// heightMap.getHeightMap().length + " points.");
			heightMap.normalizeTerrain(maxHeight);

			trimTerrain(heightMap);
			// heightMap.smooth(1, 10);

			// now back it up, for "reset".
			originalHeightMap = new AbstractHeightMap() {
				@Override
				public boolean load() {
					this.size = worldState.getSize() + 1;
					float[] f = heightMap.getHeightMap();
					float[] f2 = new float[f.length];
					System.arraycopy(f, 0, f2, 0, f.length);
					this.heightData = f2;
					return true;
				}
			};

			originalHeightMap.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		terrain = new TerrainQuad("terrain", patchSize, size + 1, heightMap.getHeightMap());

		terrain.setLocalTranslation(0, 0, 0);
		terrain.setLocalScale(worldScale, heightScale, worldScale);

		// setup LOD control.
		// TerrainLodControl control = new TerrainLodControl(terrain,
		// this.getCamera());
		// control.setLodCalculator(new DistanceLodCalculator(patchSize, 4f));
		// terrain.addControl(control);
		terrain.setMaterial(terrainMat);

		rootNode.attachChild(terrain);

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
		waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, 0));

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

		Quad quad = new Quad(worldState.getSize() * worldScale * 0.99f, worldState.getSize() * worldScale * 0.99f);

		// the texture coordinates define the general size of the waves
		quad.scaleTextureCoordinates(new Vector2f(6f, 6f));

		water = new Geometry("water", quad);
		water.setShadowMode(ShadowMode.Receive);
		water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
		water.setMaterial(waterProcessor.getMaterial());
		water.setLocalTranslation(-worldScale * worldState.getSize() * 0.99f / 2, 0, worldScale * worldState.getSize() * 0.99f / 2);
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
		water.setRadius(worldScale * worldState.getSize());
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
		sun.setColor(ColorRGBA.White.clone().multLocal(worldScale));
		rootNode.addLight(sun);

		AmbientLight ambLight = new AmbientLight();
		ambLight.setColor(new ColorRGBA(1f, 1f, 0.8f, 0.2f));
		rootNode.addLight(ambLight);

		PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
		pssmRenderer.setDirection(sunDir.normalizeLocal()); // light direction
		viewPort.addProcessor(pssmRenderer);
	}

	@Override
	public void simpleInitApp() {
		worldState = new WorldState(seed);
		stateManager.attach(new CameraControlAppState()); // modify the camera
															// controls..
		stateManager.attach(new ToolAppState());

		// setupHintText();
		setupKeys(); // G for grid, Equals (=) for smoothing.
		setupCamera();
		setupLight();
		setupSky();
		setupMaterial();

		setupTerrain(worldState.getSize());

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

	private void setupHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
		hintText.setText("Hint");
		guiNode.attachChild(hintText);
	}

	private void smoothLocalHeight(Vector3f loc, float toolRadius, float toolPower) {
		int size = heightMap.getSize();
		int xLoc = (int) Math.floor(loc.x / worldScale + size / 2);
		int zLoc = (int) Math.floor(loc.z / worldScale + size / 2);
		int actualRadius = (int) (toolRadius / worldScale);
		float np = toolPower / ToolAppState.CURSOR_POWER_MAX;

		// System.out.printf("(x, z) = (%d, %d)\nradius = %d\nnp =%f", xLoc,
		// zLoc, actualRadius,np);

		// float[] heightData = heightMap.getHeightMap();
		for (int x = (int) (xLoc - actualRadius); x < xLoc + actualRadius; x++) {
			for (int z = (int) (zLoc - actualRadius); z < zLoc + actualRadius; z++) {
				// System.out.printf("Checking point (%d,%d) is in radius %d\n",x,z,actualRadius);
				if (isInRadius(x - xLoc, z - zLoc, actualRadius)) {
					// found a node we want to smooth.
					int neighNumber = 0;
					float neighAverage = 0;
					// hunt for neighbors.
					// System.out.println("Looking for neightbors.\n\n");
					for (int rx = (int) -actualRadius; rx <= actualRadius; rx++) {
						for (int rz = (int) -actualRadius; rz <= actualRadius; rz++) {
							if (x + rx < 1 || x + rx >= size - 1) {
								continue;
							}
							if (z + rz < 1 || z + rz >= size - 1) {
								continue;
							}
							neighNumber++;
							neighAverage += heightMap.getTrueHeightAtPoint(x + rx, z + rz);// heightData[(x
																							// +
																							// rx)
																							// +
																							// (z
																							// +
																							// ry)
																							// *
																							// size];
						}
					}

					neighAverage /= neighNumber;
					float cp = 1 - np;
					// System.out.printf("oldHeight =%f, newHeight = %f\n\n",
					// heightData[x + z * size], neighAverage * np +
					// heightData[x + z * size] * cp);
					if (x >= 1 && x < size - 1) {
						if (z >= 1 && z < size - 1) {
							float oldHeight = heightMap.getTrueHeightAtPoint(x, z);
							heightMap.setHeightAtPoint(neighAverage * np + oldHeight * cp, x, z);
							// heightData[x + z * size] = neighAverage * np +
							// heightData[x + z * size] * cp;
						}
					}
				}
			}
		}
		trimTerrain(heightMap);
		updateHeights();
	}

	/**
	 * Adust the height of the terrain
	 * 
	 * @param loc
	 *            the location of the center of the effect.
	 * @param radius
	 *            the radius of the effect.
	 * @param delta
	 *            the change in height.
	 */
	private void adjustLocalHeight_old(Vector3f loc, float radius, float delta) {

		// offset it by radius because in the loop we iterate through 2 radii
		int radiusStepsX = (int) (radius / terrain.getLocalScale().x);
		int radiusStepsZ = (int) (radius / terrain.getLocalScale().z);

		float xStepAmount = terrain.getLocalScale().x;
		float zStepAmount = terrain.getLocalScale().z;
		// long start = System.currentTimeMillis();
		List<Vector2f> locs = new ArrayList<Vector2f>();
		List<Float> heights = new ArrayList<Float>();
		// float[] hMap = heightMap.getHeightMap();
		float xRange = (float) ((worldState.getSize() + 1) * worldScale * 0.5) - xStepAmount;
		float zRange = (float) ((worldState.getSize() + 1) * worldScale * 0.5) - zStepAmount;

		for (int z = -radiusStepsZ; z < radiusStepsZ; z++) {
			for (int x = -radiusStepsX; x < radiusStepsX; x++) {

				float locX = loc.x + (x * xStepAmount);
				float locZ = loc.z + (z * zStepAmount);

				if (Math.floor(locX) > -xRange
						&& Math.floor(locZ) > -zRange
						&& Math.floor(locX) < xRange
						&& Math.floor(locZ) < zRange) {
					if (isInRadius(locX - loc.x, locZ - loc.z, radius)) {
						// see if it is in the radius of the tool
						float dH = calculateHeight_old(radius, delta, locX - loc.x, locZ - loc.z);
						// dH = Math.max(TERRAIN_MIN, Math.min(TERRAIN_MAX,
						// dH));

						int tX = (int) Math.floor(((locX / xStepAmount) + ((worldState.getSize() + 1) / 2)));
						int tZ = (int) Math.floor(((locZ / zStepAmount) + ((worldState.getSize() + 1) / 2)));
						// System.out.printf("locX=%f, tX=%d\n",locX,tX);
						// System.out.printf("locZ=%f, tZ=%d\n",locZ,tZ);
						if (tZ >= 0 && tZ < worldState.getSize() + 1 && tX >= 0 && tX < worldState.getSize() + 1) {
							locs.add(new Vector2f(locX, locZ));

							float oldHeight = heightMap.getTrueHeightAtPoint(tX, tZ);
							float newHeight = oldHeight + dH; // bracket.

							if (newHeight < WorldState.TERRAIN_MIN) {
								newHeight = WorldState.TERRAIN_MIN;
								// System.out.printf("dH = %f . Replacing with %f\n",dH,TERRAIN_MIN-oldHeight);
								dH = WorldState.TERRAIN_MIN - oldHeight;
							}

							heights.add(dH);

							heightMap.setHeightAtPoint(newHeight, tX, tZ);
						}
					}
					// } else {
					// System.out.printf("locX: %f, locZ: %f, worldState.getSize(): %d\n",locX,locZ,worldState.getSize());
				}
			}
		}

		trimTerrain(heightMap);
		updateHeights();
		// terrain.adjustHeight(locs, heights);
		// terrain.updateModelBound();
		// terrain.forceRefresh(true, true, true);
	}

	private boolean isInRadius(float x, float y, float radius) {
		return Math.sqrt((x * x) + (y * y)) <= radius;
	}

	private float calculateHeight_old(float radius, float heightFactor, float x, float z) {
		return calculateHeightCos_old(radius, heightFactor, x, z);
	}

	private float calculateHeightCos_old(float radius, float heightFactor, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return heightFactor * yVal;
	}

	private void updateHintText() {
		int x = (int) getCamera().getLocation().x;
		int y = (int) getCamera().getLocation().y;
		int z = (int) getCamera().getLocation().z;
		hintText.setText("<LMB>:raise\n<RMB>:lower\n<=>:smooth\n<g>:grid\n<e>:erode\n<r>:reset\n<n>:normalize\n<tab>:wireframe\n<.>:lowerall\n<,>:raiseall\n<1,2,3...>: Change tool\n\ncam:" + x + "," + y + "," + z + "\nTool:" + toolSet[currentTool].toString());
	}

	public void simpleUpdate(float tpf) {
		Vector3f intersection = null;
		// int modifiers = 0;

		ToolAppState ts = stateManager.getState(ToolAppState.class);
		if (ts != null) {
			intersection = ts.getCursorPosition();
		} else {
			System.out.println("ToolsetAppState is NULL");
		}

		if (smoothing) {
			smoothGlobalHeight();
		} else if (raiseTerrain) {
			modifyGlobalHeights(2);// tpf * worldScale);
		} else if (lowerTerrain) {
			modifyGlobalHeights(-2);// tpf * -worldScale);
		} else if (ts != null && ts.isToolPressed()) {
			// System.out.println("toolPressed");
			// if (toolSet[currentTool] != TerrainToolMode.NONE) {

			// System.out.println(toolMode);
			switch (toolSet[currentTool]) {
			case NONE:
				// Do nothing.
				break;
			case RAISE_LOWER:
				if (intersection != null) {
					adjustLocalHeight(intersection, ts.getCursorRadius() * worldScale / 2, (worldState.getInputModifier(WorldState.ALT) ? -1 : 1) * tpf * ts.getCursorPower() * 2);
				}
				break;
			case SMOOTH:
				if (intersection != null) {
					smoothLocalHeight(intersection, ts.getCursorRadius() * worldScale / 2, tpf * ts.getCursorPower() * 2);
				}
				break;
			}
			// }
		}

		// // update the location of the cursor.
		// if (terrain != null && intersection != null) {
		// float h = terrain.getHeight(new Vector2f(intersection.x,
		// intersection.z));
		// Vector3f tl = terrain.getWorldTranslation();
		// cursor.setLocalTranslation(tl.add(new Vector3f(intersection.x, h +
		// cursorPower, intersection.z)));
		// // cursorNormal.setLocalTranslation(tl.add(new
		// // Vector3f(intersection.x, h, intersection.z)));
		//
		// // Vector2f v = new Vector2f(intersection.x, intersection.z);
		// // Vector3f normal = terrain.getNormal(v);
		// // ((Arrow)markerNormal.getMesh()).setArrowExtent(normal);
		// }
		//
	}

	public int getCurrentTool() {
		return currentTool;
	}

	/**
	 * @param tool
	 *            - index into this app's toolSet.
	 * @return true if this tool requires a cursor, false if otherwise.
	 */
	public boolean setCurrentTool(int tool) {
		if (tool < toolSet.length && tool >= 0) {
			this.currentTool = tool;
		}
		return toolSet[currentTool].getCursorType() != CursorType.NONE;

	}

	public float getWorldScale() {
		return worldScale;
	}

	public void setWorldScale(float worldScale) {
		this.worldScale = worldScale;
	}

	public float getHeightScale() {
		return heightScale;
	}

	public void setHeightScale(float heightScale) {
		this.heightScale = heightScale;
	}

	public boolean hasCursor() {
		if (currentTool >= 0 && currentTool < toolSet.length) {
			return toolSet[currentTool].getCursorType() != CursorType.NONE;
		}
		return false;
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
				Event evt = (Event) arg;
				switch (evt.getEventType()) {
				case TERRAIN:
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
					System.out.println("Whoah. Unknown event type: " + evt.getEventType().toString());
				}
			}
		}
	}
}
