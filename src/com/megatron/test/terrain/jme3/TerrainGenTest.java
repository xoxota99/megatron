package com.megatron.test.terrain.jme3;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.jme3.app.*;
import com.jme3.bullet.*;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.*;
import com.jme3.bullet.util.*;
import com.jme3.collision.*;
import com.jme3.font.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.post.*;
import com.jme3.post.filters.*;
import com.jme3.renderer.queue.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.scene.shape.*;
import com.jme3.shadow.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.lodcalc.*;
import com.jme3.terrain.heightmap.*;
import com.jme3.texture.*;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.*;
import com.jme3.water.*;
import com.megatron.terrain.*;

/**
 * 
 * @author philippd
 * 
 */
public class TerrainGenTest extends SimpleApplication {

	protected static final String INPUT_GLOBAL_SMOOTH = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_SMOOTH";
	protected static final String INPUT_TOGGLE_GRID = TerrainGenTest.class.getName() + ".INPUT_TOGGLE_GRID";
	protected static final String INPUT_GLOBAL_ERODE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_ERODE";
	protected static final String INPUT_RESET = TerrainGenTest.class.getName() + ".INPUT_RESET";
	protected static final String INPUT_GLOBAL_NORMALIZE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_NORMALIZE";
	protected static final String INPUT_WIREFRAME = TerrainGenTest.class.getName() + ".INPUT_WIREFRAME";
	protected static final String INPUT_GLOBAL_RAISE = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_RAISE";
	protected static final String INPUT_GLOBAL_LOWER = TerrainGenTest.class.getName() + ".INPUT_GLOBAL_LOWER";
	// Use the currently selected tool, in the currently selected mode.
	protected static final String INPUT_TOOL = TerrainGenTest.class.getName() + ".INPUT_TOOL";

	// "Decrease" the tool.
	protected static final String INPUT_TOOL_DOWN = TerrainGenTest.class.getName() + ".INPUT_TOOL_DOWN";
	// "Increase" the tool.
	protected static final String INPUT_TOOL_UP = TerrainGenTest.class.getName() + ".INPUT_TOOL_UP";
	protected static final String INPUT_TOOL_SELECT_0 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_0";
	protected static final String INPUT_TOOL_SELECT_1 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_1";
	protected static final String INPUT_TOOL_SELECT_2 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_2";
	protected static final String INPUT_TOOL_SELECT_3 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_3";
	protected static final String INPUT_TOOL_SELECT_4 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_4";
	protected static final String INPUT_TOOL_SELECT_5 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_5";
	protected static final String INPUT_TOOL_SELECT_6 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_6";
	protected static final String INPUT_TOOL_SELECT_7 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_7";
	protected static final String INPUT_TOOL_SELECT_8 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_8";
	protected static final String INPUT_TOOL_SELECT_9 = TerrainGenTest.class.getName() + ".INPUT_TOOL_SELECT_9";

	protected static final String INPUT_CTRL = TerrainGenTest.class.getName() + ".INPUT_CTRL";
	protected static final String INPUT_SHIFT = TerrainGenTest.class.getName() + ".INPUT_SHIFT";
	protected static final String INPUT_ALT = TerrainGenTest.class.getName() + ".INPUT_ALT";

	private static int SHIFT = 0x1;
	private static int CTRL = 0x10;
	private static int ALT = 0x100;

	private int modifiers = 0;
	private TerrainToolMode toolMode = TerrainToolMode.NONE;
	private boolean toolChanged;

	private long seed = System.currentTimeMillis();
	private int size = 256;
	int patchSize = 65;

	// private float TERRAIN_MAX = 256f; // terrain can never be
	// higher than this.
	// private float TERRAIN_MIN = -50f; // terrain can never be lower
	// than this.
	private boolean showGrid = false;
	// private int gridScale = 2;
	private boolean smoothing = false;
	private Node grid;
	// private Node arrows;
	private AbstractHeightMap heightMap;
	private AbstractHeightMap originalHeightMap;
	private TerrainQuad terrain;

	private Material terrainMat;
	private Material wfMat;
	private Material cursorMat;
	private Material wfMat2;

	boolean showWireframe = false;
	private boolean raiseTerrain = false;
	private boolean lowerTerrain = false;
	private boolean toolPressed = false;
	// private boolean lowerTerrainLocal = false;

	private int cursorRadius = 60;
	private int cursorPower = 50;

	private Geometry cursor;
	// private Geometry cursorNormal;

	private float scale = 5f;
	private float heightScale = scale / 2;
	private float maxHeight = 64f; // During auto-generation and normalizing,
	// don't create terrain higher than this.

	private BitmapText hintText;
	private boolean initialized = false;

	// Where is the sun?
	Vector3f sunDir = new Vector3f(-0.4790551f, -0.39247334f, -0.7851566f);

	public static void main(String... args) {
		SimpleApplication app = new TerrainGenTest();
		app.setShowSettings(false);
		AppSettings settings = getAppSettings();
		app.setSettings(settings);
		// app.setDisplayStatView(false);
		// app.setDisplayFps(false);
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
//		settings.setFullscreen(true);
		settings.setBitsPerPixel(mode.getBitDepth());
		settings.setFrequency(mode.getRefreshRate());
		settings.setResolution(mode.getWidth(), mode.getHeight());
		// System.out.println(mode.getRefreshRate());
		return settings;
	}

	private ActionListener actionListener = new ActionListener() {

		public void onAction(String name, boolean pressed, float tpf) {
			smoothing = false;
			if (pressed && name.equals(INPUT_TOGGLE_GRID)) {
				toggleGrid();
			} else if (name.equals(INPUT_GLOBAL_SMOOTH)) {
				smoothing = pressed;
			} else if (pressed && name.equals(INPUT_GLOBAL_ERODE)) {
				erode();
			} else if (pressed && name.equals(INPUT_RESET)) {
				reset();
			} else if (pressed && name.equals(INPUT_GLOBAL_NORMALIZE)) {
				normalizeDown(); // normalize down by 10%
			} else if (pressed && name.equals(INPUT_WIREFRAME)) {
				toggleWireframe(); // normalize down by 10%
			} else if (name.equals(INPUT_GLOBAL_RAISE)) {
				raiseTerrain = pressed; // raise all terrain by 1.
			} else if (name.equals(INPUT_GLOBAL_LOWER)) {
				lowerTerrain = pressed; // lower all terrain by 1.
			} else if (name.equals(INPUT_TOOL)) { // Use your tool.
//				System.out.println("Use your tool.");
				toolPressed = pressed;
			} else if (pressed && name.contains("INPUT_TOOL_SELECT")) {
				toolChanged = pressed;
				if (pressed && name.equals(INPUT_TOOL_SELECT_0)) { // Change
																	// tool.
																	// System.out.println("No tool.");
					toolMode = TerrainToolMode.NONE;
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_1)) { // Change
																			// tool.
																			// System.out.println("Raise/Lower Terrain.");
					toolMode = TerrainToolMode.RAISE_LOWER;
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_2)) { // Change
																			// tool.
																			// System.out.println("Smooth.");
					toolMode = TerrainToolMode.SMOOTH;
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_3)) { // Change
																			// tool.

				} else if (pressed && name.equals(INPUT_TOOL_SELECT_4)) { // Change
																			// tool.
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_5)) { // Change
																			// tool.
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_6)) { // Change
																			// tool.
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_7)) { // Change
																			// tool.
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_8)) { // Change
																			// tool.
				} else if (pressed && name.equals(INPUT_TOOL_SELECT_9)) { // Change
																			// tool.
				}
			} else if (name.equals(INPUT_TOOL_DOWN)) {
				if ((modifiers & ALT) > 0) {
					cursorPower = (int) Math.max(cursorPower - scale, 5 * scale);
				} else {
					cursorRadius = (int) Math.max(cursorRadius - scale, 5 * scale);
				}
				// rootNode.detachChild(cursor);
				// setupCursor();
				updateCursor();
			} else if (name.equals(INPUT_TOOL_UP)) {
				if ((modifiers & ALT) > 0) {
					cursorPower = (int) Math.min(cursorPower + scale, size * scale / 8);
				} else {
					cursorRadius = (int) Math.min(cursorRadius + scale, size * scale / 8);
				}
				// rootNode.detachChild(cursor);
				// setupCursor();
				updateCursor();
			} else if (name.equals(INPUT_SHIFT)) {
				// System.out.printf("modifiers was %s\n",
				// Integer.toHexString(modifiers));
				if (pressed) {
					modifiers |= SHIFT;
				} else {
					modifiers &= ~SHIFT;
				}
				// System.out.printf("modifiers is now %s\n",
				// Integer.toHexString(modifiers));
			} else if (name.equals(INPUT_CTRL)) {
				// System.out.printf("modifiers was %s\n",
				// Integer.toHexString(modifiers));
				if (pressed) {
					modifiers |= CTRL;
				} else {
					modifiers &= ~CTRL;
				}
				// System.out.printf("modifiers is now %s\n",
				// Integer.toHexString(modifiers));
			} else if (name.equals(INPUT_ALT)) {
				// System.out.printf("modifiers was %s\n",
				// Integer.toHexString(modifiers));
				if (pressed) {
					modifiers |= ALT;
				} else {
					modifiers &= ~ALT;
				}
				// System.out.printf("modifiers is now %s\n",
				// Integer.toHexString(modifiers));
			}
		}
	};

	private void setupKeys() {
		inputManager.addMapping(INPUT_GLOBAL_SMOOTH, new KeyTrigger(KeyInput.KEY_EQUALS));
		inputManager.addListener(actionListener, INPUT_GLOBAL_SMOOTH);
		inputManager.addMapping(INPUT_TOGGLE_GRID, new KeyTrigger(KeyInput.KEY_G));
		inputManager.addListener(actionListener, INPUT_TOGGLE_GRID);
		inputManager.addMapping(INPUT_GLOBAL_ERODE, new KeyTrigger(KeyInput.KEY_E));
		inputManager.addListener(actionListener, INPUT_GLOBAL_ERODE);
		inputManager.addMapping(INPUT_RESET, new KeyTrigger(KeyInput.KEY_R));
		inputManager.addListener(actionListener, INPUT_RESET);
		inputManager.addMapping(INPUT_GLOBAL_NORMALIZE, new KeyTrigger(KeyInput.KEY_N));
		inputManager.addListener(actionListener, INPUT_GLOBAL_NORMALIZE);
		inputManager.addMapping(INPUT_WIREFRAME, new KeyTrigger(KeyInput.KEY_TAB));
		inputManager.addListener(actionListener, INPUT_WIREFRAME);
		inputManager.addMapping(INPUT_GLOBAL_LOWER, new KeyTrigger(KeyInput.KEY_PERIOD));
		inputManager.addListener(actionListener, INPUT_GLOBAL_LOWER);
		inputManager.addMapping(INPUT_GLOBAL_RAISE, new KeyTrigger(KeyInput.KEY_COMMA));
		inputManager.addListener(actionListener, INPUT_GLOBAL_RAISE);
		// Use Tool.
		inputManager.addMapping(INPUT_TOOL, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(actionListener, INPUT_TOOL);
		// Decrease Tool.
		inputManager.addMapping(INPUT_TOOL_DOWN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addListener(actionListener, INPUT_TOOL_DOWN);
		// Increase tool.
		inputManager.addMapping(INPUT_TOOL_UP, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addListener(actionListener, INPUT_TOOL_UP);

		// Select Tool.
		inputManager.addMapping(INPUT_TOOL_SELECT_0, new KeyTrigger(KeyInput.KEY_0));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_0);
		inputManager.addMapping(INPUT_TOOL_SELECT_1, new KeyTrigger(KeyInput.KEY_1));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_1);
		inputManager.addMapping(INPUT_TOOL_SELECT_2, new KeyTrigger(KeyInput.KEY_2));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_2);
		inputManager.addMapping(INPUT_TOOL_SELECT_3, new KeyTrigger(KeyInput.KEY_3));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_3);
		inputManager.addMapping(INPUT_TOOL_SELECT_4, new KeyTrigger(KeyInput.KEY_4));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_4);
		inputManager.addMapping(INPUT_TOOL_SELECT_5, new KeyTrigger(KeyInput.KEY_5));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_5);
		inputManager.addMapping(INPUT_TOOL_SELECT_6, new KeyTrigger(KeyInput.KEY_6));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_6);
		inputManager.addMapping(INPUT_TOOL_SELECT_7, new KeyTrigger(KeyInput.KEY_7));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_7);
		inputManager.addMapping(INPUT_TOOL_SELECT_8, new KeyTrigger(KeyInput.KEY_8));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_8);
		inputManager.addMapping(INPUT_TOOL_SELECT_9, new KeyTrigger(KeyInput.KEY_9));
		inputManager.addListener(actionListener, INPUT_TOOL_SELECT_9);

		inputManager.addMapping(INPUT_SHIFT, new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addMapping(INPUT_SHIFT, new KeyTrigger(KeyInput.KEY_RSHIFT));
		inputManager.addListener(actionListener, INPUT_SHIFT);

		inputManager.addMapping(INPUT_CTRL, new KeyTrigger(KeyInput.KEY_LCONTROL));
		inputManager.addMapping(INPUT_CTRL, new KeyTrigger(KeyInput.KEY_RCONTROL));
		inputManager.addListener(actionListener, INPUT_CTRL);

		inputManager.addMapping(INPUT_ALT, new KeyTrigger(KeyInput.KEY_LMENU));
		inputManager.addMapping(INPUT_ALT, new KeyTrigger(KeyInput.KEY_RMENU));
		inputManager.addListener(actionListener, INPUT_ALT);

	}

	// private void resetMaxHeight() {
	// float[] f = heightMap.getHeightMap();
	// for (int i = 0; i < f.length; i++) {
	// if (f[i] > maxHeight) {
	// maxHeight = f[i];
	// }
	// }
	// }

	private void reset() {
		for (int x = 1; x < size; x++) {
			for (int z = 1; z < size; z++) {
				float h = originalHeightMap.getTrueHeightAtPoint(x, z);
				heightMap.setHeightAtPoint(h, x, z);
			}
		}
		updateHeights();
	}

	private void modifyGlobalHeights(float amt) {
		for (int x = 0; x < size + 1; x++) {
			for (int z = 0; z < size + 1; z++) {
				float h = heightMap.getTrueHeightAtPoint(x, z) + amt;
				heightMap.setHeightAtPoint(h, x, z);
			}
		}
		// float[] f = heightMap.getHeightMap();
		// for (int i = 0; i < f.length; i++) {
		// f[i] += amt;
		// }

		updateHeights();

	}

	private void smooth() {
		heightMap.smooth(.5f, 6);
		trimTerrain(heightMap);
		// resetMaxHeight();
		updateHeights();
	}

	private void erode() {
		heightMap.flatten((byte) 2);
		trimTerrain(heightMap);
		// resetMaxHeight();
		updateHeights();
	}

	private void normalizeDown() {
		maxHeight *= 0.9f;
		heightMap.normalizeTerrain(maxHeight);
		trimTerrain(heightMap);
		updateHeights();
	}

	private void toggleWireframe() {
		showWireframe = !showWireframe;
		if (showWireframe) {
			terrain.setMaterial(wfMat);
			cursor.setMaterial(wfMat2);
		} else {
			terrain.setMaterial(terrainMat);
			cursor.setMaterial(cursorMat);
		}
	}

	private void updateHeights() {

		List<Vector2f> xz = new ArrayList<Vector2f>();
		List<Float> height = new ArrayList<Float>();
		maxHeight = 0;
		for (int x = 1; x < size; x++) {
			for (int z = 1; z < size; z++) {
				float h = heightMap.getTrueHeightAtPoint(x, z);
				if (h > maxHeight) {
					maxHeight = h;
				}
				xz.add(new Vector2f((x - ((size + 1) / 2)) * scale, (z - ((size + 1) / 2)) * scale));
				height.add(h);
			}
		}
		terrain.setHeight(xz, height);
		terrain.updateModelBound();
	}

	private void updateHeights_old(float[] f) {
		if (rootNode.getChild("terrain") == terrain) {
			List<Vector2f> xz = new ArrayList<Vector2f>();
			List<Float> height = new ArrayList<Float>();
			maxHeight = 0;
			for (int i = 0; i < f.length; i++) {
				// x and y are the indices into the heightMap.
				int x = (i % (size + 1));
				int z = ((i - x) / (size + 1));
				if (x > 0 && z > 0 && x < size && z < size) {
					if (f[i] > maxHeight)
						maxHeight = f[i];
					// But the "origin" on the terrain Spatial is in the middle
					// (it
					// looks like?). So change the x/y coordinate, I guess...
					x = x - ((size + 1) / 2);
					z = z - ((size + 1) / 2);
					xz.add(new Vector2f(x * scale, z * scale));
					height.add(f[i]);
				} else {
					x = x - ((size + 1) / 2);
					z = z - ((size + 1) / 2);
					xz.add(new Vector2f(x * scale, z * scale));
					height.add(f[i]);
				}
			}
			terrain.setHeight(xz, height);
			terrain.updateModelBound();
		}
	}

	/**
	 * Toggle the grid on/off. (This also toggles the arrows).
	 */
	private void toggleGrid() {
		showGrid = !showGrid;
		if (showGrid) {
			// turn it on.
			rootNode.attachChild(grid);
			// rootNode.attachChild(arrows);
		} else {
			// turn it off.
			rootNode.detachChildNamed("grid");
			// rootNode.detachChildNamed("arrows");
		}
	}

	private Material setupMaterial() {
		terrainMat = setupHeightBasedMaterial();
		wfMat = getWireframeMaterial(ColorRGBA.Green);
		wfMat2 = getWireframeMaterial(ColorRGBA.Red);
		return showWireframe ? wfMat : terrainMat;
	}

	private Material getWireframeMaterial(ColorRGBA c) {
		Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m.setColor("Color", c);
		m.getAdditionalRenderState().setWireframe(true);
		// m.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
		return m;
	}

	private Material setupHeightBasedMaterial() {
		float grassScale = 128;
		float dirtScale = 128;
		float rockScale = 128;

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
		retval.setVector3("region1", new Vector3f(-10, 200, grassScale));

		retval.setTexture("region2ColorMap", grass);
		retval.setVector3("region2", new Vector3f(-256, 0, grassScale));

		retval.setTexture("region3ColorMap", dirt);
		retval.setVector3("region3", new Vector3f(198, 500, dirtScale));

		retval.setTexture("region4ColorMap", rock);
		retval.setVector3("region4", new Vector3f(198, 500, rockScale));

		retval.setTexture("slopeColorMap", rock);
		retval.setFloat("slopeTileFactor", 32);

		retval.setFloat("terrainSize", scale * size + 1);

		// retval.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		// retval.getAdditionalRenderState().setWireframe(true);

		return retval;

	}

	private void trimTerrain(HeightMap heightMap) {
		// drop the edges.
		for (int x = 0; x < size + 1; x++) {
			heightMap.setHeightAtPoint(-50f, x, 0);
			heightMap.setHeightAtPoint(-50f, x, size);
		}
		for (int z = 0; z < size + 1; z++) {
			heightMap.setHeightAtPoint(-50f, 0, z);
			heightMap.setHeightAtPoint(-50f, size, z);
		}

	}

	private void setupHeightMap(int size, long seed) {
		heightMap = null;
		try {
			// heightMap = new FaultHeightMap(size + 1, 1000,
			// FaultHeightMap.FAULTTYPE_COSINE,
			// FaultHeightMap.FAULTSHAPE_LINE,1f, 20f, seed);

			// heightMap = new FluidSimHeightMap(size+1, 1000, 0f, 256f, .01f,
			// .5f, 10f, 10f, seed);

			// heightMap = new HillHeightMap(size + 1, 1000, 10, size / 2,seed);
			heightMap = new MidpointDisplacementHeightMap(size + 1, 1, .5f, seed);
			heightMap.normalizeTerrain(maxHeight);

			trimTerrain(heightMap);
			// heightMap.smooth(1, 10);

			// now back it up, for "reset".
			originalHeightMap = new AbstractHeightMap() {
				@Override
				public boolean load() {
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
	}

	private void setupCamera() {
		this.flyCam.setMoveSpeed(size * scale / 4);
		this.flyCam.setRotationSpeed(size * scale / 300);

		this.getCamera().setLocation(new Vector3f(size * scale / 3, size * scale / 2, size * scale));
		this.getCamera().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
		this.getCamera().setFrustumFar(size * scale * 4);

	}

	private void setupTerrain(int size, long seed) {
		setupHeightMap(size, seed);
		terrain = new TerrainQuad("terrain", patchSize, size + 1, heightMap.getHeightMap());

		// terrain.setLocalTranslation(0, 0, 0);
		terrain.setLocalScale(scale, heightScale, scale);

		// setup LOD control.
		// TerrainLodControl control = new TerrainLodControl(terrain,
		// this.getCamera());
		// control.setLodCalculator(new DistanceLodCalculator(patchSize, 1.7f));
		// terrain.addControl(control);

	}

	private void setupRiver() {
		RiverFactory rf = new RiverFactory(20f, seed);
		float[] f = rf.createRiver(terrain.getHeightMap(), size + 1);
		updateHeights();
	}

	private void setupMesh() {
		terrain.depthFirstTraversal(new SceneGraphVisitor() {

			@Override
			public void visit(Spatial spatial) {
				Geometry g = (Geometry) spatial;
				Mesh m = g.getMesh();
				List<VertexBuffer> vbList = m.getBufferList();
				for (VertexBuffer vb : vbList) {
					System.out.println(vb.getBufferType());
				}
			}
		});
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
		waterProcessor.setWaterDepth(20);
		// lower the distortion scale if the waves appear too strong
		// waterProcessor.setDistortionScale(0.1f);
		// lower the speed of the waves if they are too fast
		waterProcessor.setWaveSpeed(0.01f);

		Quad quad = new Quad(size * scale*0.99f, size * scale*0.99f);

		// the texture coordinates define the general size of the waves
		quad.scaleTextureCoordinates(new Vector2f(6f, 6f));

		Geometry water = new Geometry("water", quad);
		water.setShadowMode(ShadowMode.Receive);
		water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
		water.setMaterial(waterProcessor.getMaterial());
		water.setLocalTranslation(-scale * size*0.99f / 2, 0, scale * size*0.99f / 2);
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
		water.setCenter(Vector3f.UNIT_Y.mult(-50));
		water.setRadius(scale * size);
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

	private void setupGrid(Vector3f pos, int size, ColorRGBA color) {
		// int scaleSize = size / gridScale;
		// if (scaleSize * gridScale < size)
		// scaleSize++;
		Geometry g = new Geometry("grid1", new Grid(size, size, 1));
		// Geometry g2 = new Geometry("grid2", new Grid(size, size, 1f));
		g.setLocalScale(scale);
		Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		gridMat.getAdditionalRenderState().setWireframe(true);
		gridMat.setColor("Color", color);

		g.setMaterial(gridMat);
		g.center().move(pos);

		// Material mat2 = mat.clone();
		// mat2.setColor("Color", ColorRGBA.LightGray);

		// g2.setMaterial(mat2);
		// Vector3f pos2 = new Vector3f(pos);
		// pos2.setY(pos2.getY() - 0.5f);
		// g2.center().move(pos2);

		Node n = new Node("grid");
		n.attachChild(g);
		// n.attachChild(g2);

		grid = n;

	}

	private void setupArrows(Vector3f pos, int size) {
		// arrows = new Node("arrows");

		Arrow arrow = new Arrow(new Vector3f(size, 0, 0));
		putWireframeShape("arrowX", arrow, ColorRGBA.Red, grid).setLocalTranslation(pos);

		arrow = new Arrow(new Vector3f(0, size, 0));
		putWireframeShape("arrowY", arrow, ColorRGBA.Green, grid).setLocalTranslation(pos);

		arrow = new Arrow(new Vector3f(0, 0, size));
		putWireframeShape("arrowZ", arrow, ColorRGBA.Blue, grid).setLocalTranslation(pos);
	}

	private Geometry putWireframeShape(String name, Mesh shape, ColorRGBA color, Node nd) {
		Geometry g = new Geometry(name, shape);
		Material mat = getWireframeMaterial(color);
		g.setMaterial(mat);
		if (showGrid) {
			if (nd == null) {
				nd = rootNode;
			}
			nd.attachChild(g);
		}
		return g;
	}

	private void setupSky() {
		rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
	}

	private void setupLight() {
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(sunDir);
		sun.setColor(ColorRGBA.White.clone().multLocal(2));
		rootNode.addLight(sun);

		AmbientLight ambLight = new AmbientLight();
		ambLight.setColor(new ColorRGBA(1f, 1f, 0.8f, 0.2f));
		rootNode.addLight(ambLight);

		PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
		pssmRenderer.setDirection(sunDir.normalizeLocal()); // light direction
		viewPort.addProcessor(pssmRenderer);
	}

	private void setupDoF() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		// fpp.setNumSamples(4);

		DepthOfFieldFilter dofFilter = new DepthOfFieldFilter();
		dofFilter.setFocusDistance(0);
		dofFilter.setFocusRange(50);
		dofFilter.setBlurScale(1.4f);
		fpp.addFilter(dofFilter);
		viewPort.addProcessor(fpp);

	}

	@Override
	public void simpleInitApp() {
		// setupApplication();
		setupCursor();
		setupHintText();
		setupKeys(); // G for grid, Equals (=) for smoothing.
		setupCamera();
		setupLight();
		setupSky();
		Material m = setupMaterial();

		setupTerrain(size, seed);

		setupWater();
		// setupMesh();
		// setupRiver();

		terrain.setMaterial(m);

		rootNode.attachChild(terrain);

		setupGrid(Vector3f.ZERO, this.size, ColorRGBA.Blue);
		setupArrows(Vector3f.ZERO, 256);
		if (showGrid) {
			rootNode.attachChild(grid);
		}

		// setupDoF();
	}

	private Vector3f getWorldIntersection() {
		Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
		Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
		direction.subtractLocal(origin).normalizeLocal();

		Ray ray = new Ray(origin, direction);
		CollisionResults results = new CollisionResults();
		int numCollisions = terrain.collideWith(ray, results);
		if (numCollisions > 0) {
			CollisionResult hit = results.getClosestCollision();
			return hit.getContactPoint();
		}
		return null;
	}

	private void setupHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
		hintText.setText("Hint");
		guiNode.attachChild(hintText);
	}

	/**
	 * Refresh the cursor with the current power and radius.
	 */
	private void updateCursor() {
		if (cursor == null) {
			setupCursor();
		} else {
			Cylinder mrk = new Cylinder(Math.max(3, cursorPower / 3), Math.max(16, cursorRadius / 3), cursorRadius, 1, cursorPower * 2, true, false);
			cursor.setMesh(mrk);
			cursor.updateModelBound();
		}
	}

	private void setupCursor() {
		// collision marker
		// Sphere mrk = new Sphere(8, 8, 100f);
		Cylinder mrk = new Cylinder(Math.max(3, cursorPower / 3), Math.max(16, cursorRadius / 3), cursorRadius, 1, cursorPower * 2, true, false);

		cursor = new Geometry("Cursor");
		cursor.setMesh(mrk);
		cursor.setQueueBucket(RenderQueue.Bucket.Transparent);

		cursor.rotate((float) (Math.PI / 2), 0, 0);
		// cursor.setLocalTranslation(0, cursorPower * 100, 0);
		// cursor.setCullHint(CullHint.Never);

		cursorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		cursorMat.setColor("Color", new ColorRGBA(251f / 255f, 130f / 255f, 130f / 255f, 0.1f));
		cursorMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		cursor.setMaterial(cursorMat);

		if (toolMode != TerrainToolMode.NONE) {
			rootNode.attachChild(cursor);
		}
		// surface normal marker
		// Arrow arrow = new Arrow(new Vector3f(0, 100, 0));
		// arrow.setLineWidth(4f);
		// cursorNormal = new Geometry("MarkerNormal");
		// cursorNormal.setMesh(arrow);
		// cursorNormal.setMaterial(matCursor);
		// cursor.attachChild(cursorNormal);
	}

	private void localSmooth(Vector3f loc, float radius, float power) {
		int radiusStepsX = (int) (radius / terrain.getLocalScale().x);
		int radiusStepsZ = (int) (radius / terrain.getLocalScale().z);

		float xStepAmount = terrain.getLocalScale().x;
		float zStepAmount = terrain.getLocalScale().z;
		// long start = System.currentTimeMillis();
		List<Vector2f> locs = new ArrayList<Vector2f>();
		List<Float> heights = new ArrayList<Float>();
		// float[] hMap = heightMap.getHeightMap();
		float sum = 0f;
		int count = 0;

		for (int z = -radiusStepsZ; z < radiusStepsZ; z++) {
			for (int x = -radiusStepsX; x < radiusStepsX; x++) {

				float locX = loc.x + (x * xStepAmount);
				float locZ = loc.z + (z * zStepAmount);

				if (isInRadius(locX - loc.x, locZ - loc.z, radius)) {
					// see if it is in the radius of the tool
					count++;

					int tX = Math.max(0, Math.min(size, (int) ((locX / xStepAmount) + ((size + 1) / 2))));
					int tZ = Math.max(0, Math.min(size, (int) ((locZ / zStepAmount) + ((size + 1) / 2))));
					float oldHeight = heightMap.getTrueHeightAtPoint(tX, tZ);// hMap[(tZ
																				// *
																				// (size
																				// +
																				// 1))
																				// +
																				// tX];

					sum += oldHeight;

				}
			}
		}
		for (int z = -radiusStepsZ; z < radiusStepsZ; z++) {
			for (int x = -radiusStepsX; x < radiusStepsX; x++) {

				float locX = loc.x + (x * xStepAmount);
				float locZ = loc.z + (z * zStepAmount);
				float avg = sum / count;

				if (locX > 0 && locZ > 0 && locX < size + 1 && locZ < size + 1) {
					if (isInRadius(locX - loc.x, locZ - loc.z, radius)) {
						int tX = Math.max(0, Math.min(size, (int) ((locX / xStepAmount) + ((size + 1) / 2))));
						int tZ = Math.max(0, Math.min(size, (int) ((locZ / zStepAmount) + ((size + 1) / 2))));
						float oldHeight = heightMap.getTrueHeightAtPoint(tX, tZ);// hMap[(tZ
																					// *
																					// (size
																					// +
																					// 1))
																					// +
																					// tX];
						float p = power / (size * scale / 8);
						float newHeight = avg * p + (oldHeight * (1 - p));

						locs.add(new Vector2f(locX, locZ));
						heights.add(newHeight - oldHeight);

						heightMap.setHeightAtPoint(newHeight, tX, tZ);
					}
				}
			}
		}
		// trimTerrain(heightMap);

		// updateHeights(heights);
		terrain.adjustHeight(locs, heights);
		// System.out.println("Modified "+locs.size()+" points, took: " +
		// (System.currentTimeMillis() - start)+" ms");
		terrain.updateModelBound();
	}

	private void adjustHeight(Vector3f loc, float radius, float delta) {

		// offset it by radius because in the loop we iterate through 2 radii
		int radiusStepsX = (int) (radius / terrain.getLocalScale().x);
		int radiusStepsZ = (int) (radius / terrain.getLocalScale().z);

		float xStepAmount = terrain.getLocalScale().x;
		float zStepAmount = terrain.getLocalScale().z;
		// long start = System.currentTimeMillis();
		List<Vector2f> locs = new ArrayList<Vector2f>();
		List<Float> heights = new ArrayList<Float>();
		// float[] hMap = heightMap.getHeightMap();

		for (int z = -radiusStepsZ; z < radiusStepsZ; z++) {
			for (int x = -radiusStepsX; x < radiusStepsX; x++) {

				float locX = loc.x + (x * xStepAmount);
				float locZ = loc.z + (z * zStepAmount);

				if (locX > 0 && locZ > 0 && locX < size + 1 && locZ < size + 1) {
					if (isInRadius(locX - loc.x, locZ - loc.z, radius)) {
						// see if it is in the radius of the tool
						float dH = calculateHeight(radius, delta, locX - loc.x, locZ - loc.z);
						// dH = Math.max(TERRAIN_MIN, Math.min(TERRAIN_MAX,
						// dH));

						int tX = (int) Math.floor(((locX / xStepAmount) + ((size + 1) / 2)));
						int tZ = (int) Math.floor(((locZ / zStepAmount) + ((size + 1) / 2)));
						// System.out.printf("locX=%f, tX=%d\n",locX,tX);
						// System.out.printf("locZ=%f, tZ=%d\n",locZ,tZ);
						if (tZ >= 0 && tZ < size + 1 && tX >= 0 && tX < size + 1) {
							locs.add(new Vector2f(locX, locZ));
							heights.add(dH);
							float oldHeight = heightMap.getTrueHeightAtPoint(tX, tZ); // hMap[(tZ
																						// *
																						// (size
																						// +
																						// 1))
																						// +
																						// tX];
							// float tHeight = terrain.getHeight(new
							// Vector2f(locX,
							// locZ));
							// System.out.printf("oldHeight=%f, tHeight=%f\n",
							// oldHeight, tHeight);
							// System.out.printf("h was %f, oh is %f\n",dH,oldHeight);
							float newHeight = oldHeight + dH; // bracket.
							// dH = newHeight - oldHeight;

							// System.out.printf("h is %f\n",dH);

							heightMap.setHeightAtPoint(newHeight, tX, tZ);
						}
					}
				}
			}
		}

		// updateHeights(heights);
		terrain.adjustHeight(locs, heights);
		// System.out.println("Modified "+locs.size()+" points, took: " +
		// (System.currentTimeMillis() - start)+" ms");
		terrain.updateModelBound();
	}

	private boolean isInRadius(float x, float y, float radius) {
		return Math.sqrt((x * x) + (y * y)) <= radius;
	}

	private float calculateHeight(float radius, float heightFactor, float x, float z) {
		return calculateHeightCos(radius, heightFactor, x, z);
	}

	private float calculateHeightCos(float radius, float heightFactor, float x, float z) {
		Vector2f point = new Vector2f(x, z);
		float xVal = point.length() / radius;
		float yVal = (float) (Math.cos(xVal * Math.PI) + 1) / 2;
		return heightFactor * yVal;
	}

	private float calculateHeightLinear(float radius, float heightFactor, float x, float z) {
		// y=(cos(x*PI)+1)/2
		// find percentage for each 'unit' in radius
		Vector2f point = new Vector2f(x, z);
		float val = point.length() / radius; // linear dropoff
		val = 1 - val;
		if (val <= 0) {
			val = 0;
		}
		return heightFactor * val;
	}

	private void updateHintText(Vector3f target) {
		int x = (int) getCamera().getLocation().x;
		int y = (int) getCamera().getLocation().y;
		int z = (int) getCamera().getLocation().z;
		String targetText = "";
		if (target != null)
			targetText = "  intersect: " + target.toString();
		hintText.setText("<LMB>:raise\n<RMB>:lower\n<=>:smooth\n<g>:grid\n<e>:erode\n<r>:reset\n<n>:normalize\n<tab>:wireframe\n<.>:lowerall\n<,>:raiseall\n<1,2,3...>: Change tool\n\ncam:" + x + "," + y + "," + z + "\ntarget:" + targetText + "\nTool:" + toolMode.toString());
	}

	public void simpleUpdate(float tpf) {

		Vector3f intersection = getWorldIntersection();

		if (intersection == null) {
			// use last known good.
			intersection = cursor.getLocalTranslation();
		}

		updateHintText(intersection);

		if (!initialized) {
			// get rid of flyCam "zoom" command (so we can use the mousewheel)
			if (inputManager.hasMapping("FLYCAM_ZoomIn")) {
				inputManager.deleteMapping("FLYCAM_ZoomIn");
				inputManager.deleteMapping("FLYCAM_ZoomOut");
				initialized = true;
			}
		}

		if (smoothing) {
			smooth();
		} else if (raiseTerrain) {
			modifyGlobalHeights(tpf * cursorPower);
		} else if (lowerTerrain) {
			modifyGlobalHeights(tpf * -cursorPower);
		} else if (toolChanged) {
			// System.out.println("toolChanged");
			if (toolMode != TerrainToolMode.NONE) {
				// show the cursor.
				rootNode.attachChild(cursor);
			} else {
				// hide the cursor
				rootNode.detachChild(cursor);
			}
			toolChanged = false;
		} else if (toolPressed) {
			// System.out.println("toolPressed");
			if (toolMode != TerrainToolMode.NONE) {

//				System.out.println(toolMode);
				switch (toolMode) {
				case NONE:
					// Do nothing.
					break;
				case RAISE_LOWER:
					if (intersection != null) {
						adjustHeight(intersection, cursorRadius, ((modifiers & ALT) != 0 ? -1 : 1) * tpf * cursorPower);
					}
					break;
				case SMOOTH:
					if (intersection != null) {
						localSmooth(intersection, cursorRadius, tpf * cursorPower);
					}
					break;
				}
			}
		}

		// update the location of the cursor.
		if (terrain != null && intersection != null) {
			float h = terrain.getHeight(new Vector2f(intersection.x, intersection.z));
			Vector3f tl = terrain.getWorldTranslation();
			cursor.setLocalTranslation(tl.add(new Vector3f(intersection.x, h + cursorPower, intersection.z)));
			// cursorNormal.setLocalTranslation(tl.add(new
			// Vector3f(intersection.x, h, intersection.z)));

			// Vector2f v = new Vector2f(intersection.x, intersection.z);
			// Vector3f normal = terrain.getNormal(v);
			// ((Arrow)markerNormal.getMesh()).setArrowExtent(normal);
		}

	}
}
