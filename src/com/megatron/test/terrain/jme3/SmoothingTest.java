package com.megatron.test.terrain.jme3;

import java.util.*;

import com.jme3.app.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.system.*;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.lodcalc.*;
import com.jme3.terrain.heightmap.*;
import com.megatron.terrain.*;

/**
 * 
 * @author philippd
 * 
 */
public class SmoothingTest extends SimpleApplication {

	protected static final String INPUT_MAPPING_SMOOTH = SmoothingTest.class.getName() + ".INPUT_MAPPING_SMOOTH";
	protected static final String INPUT_MAPPING_GRID = SmoothingTest.class.getName() + ".INPUT_MAPPING_GRID";
	protected static final String INPUT_MAPPING_ERODE = SmoothingTest.class.getName() + ".INPUT_MAPPING_ERODE";
	protected static final String INPUT_MAPPING_RESET = SmoothingTest.class.getName() + ".INPUT_MAPPING_RESET";
	protected static final String INPUT_MAPPING_NORMALIZE = SmoothingTest.class.getName() + ".INPUT_MAPPING_NORMALIZE";

	private long seed = 0L;// System.currentTimeMillis();
	private int size = 256;
	private float maxHeight = 30f;
	private boolean showGrid = true;
	private int gridScale = 2;
	private boolean smoothing = false;
	private Node grid;
	// private Node arrows;
	private AbstractHeightMap heightMap;
	private AbstractHeightMap originalHeightMap;
	private TerrainQuad terrain;

	// private boolean eroding;

	public SmoothingTest() {
		// setShowSettings(false);
	}

	public static void main(String... args) {
		Application app = new SmoothingTest();

		AppSettings settings = new AppSettings(true);
		settings.setFullscreen(true);
		settings.setResolution(-1, -1); // current width/height
		app.setSettings(settings);

		app.start();
	}

	private ActionListener actionListener = new ActionListener() {

		public void onAction(String name, boolean keyPressed, float tpf) {
			if (keyPressed) {
				if (name.equals(INPUT_MAPPING_GRID)) {
					toggleGrid();
				}

				if (name.equals(INPUT_MAPPING_SMOOTH)) {
					// eroding = false;
					smoothing = !smoothing;
				}

				if (name.equals(INPUT_MAPPING_ERODE)) {
					smoothing = false;
					// eroding = !eroding;
					erode();
				}

				if (name.equals(INPUT_MAPPING_RESET)) {
					smoothing = false;
					// eroding = false;
					reset();
				}

				if (name.equals(INPUT_MAPPING_NORMALIZE)) {
					smoothing = false;
					// eroding = false;
					normalizeDown(); // normalize down by 10%
				}
			}
		}
	};

	private void setupKeys() {
		inputManager.addMapping(INPUT_MAPPING_SMOOTH, new KeyTrigger(KeyInput.KEY_EQUALS));
		inputManager.addListener(actionListener, INPUT_MAPPING_SMOOTH);
		inputManager.addMapping(INPUT_MAPPING_GRID, new KeyTrigger(KeyInput.KEY_G));
		inputManager.addListener(actionListener, INPUT_MAPPING_GRID);
		inputManager.addMapping(INPUT_MAPPING_ERODE, new KeyTrigger(KeyInput.KEY_E));
		inputManager.addListener(actionListener, INPUT_MAPPING_ERODE);
		inputManager.addMapping(INPUT_MAPPING_RESET, new KeyTrigger(KeyInput.KEY_R));
		inputManager.addListener(actionListener, INPUT_MAPPING_RESET);
		inputManager.addMapping(INPUT_MAPPING_NORMALIZE, new KeyTrigger(KeyInput.KEY_N));
		inputManager.addListener(actionListener, INPUT_MAPPING_NORMALIZE);
	}

	private void resetMaxHeight() {
		float[] f = heightMap.getHeightMap();
		for (int i = 0; i < f.length; i++) {
			if (f[i] > maxHeight) {
				maxHeight = f[i];
			}
		}
	}

	public void reset() {
		float[] f = originalHeightMap.getHeightMap();
		for (int i = 0; i < f.length; i++) {
			int x = (i % (size + 1));
			int z = ((i - x) / (size + 1));
			resetMaxHeight();
			if (heightMap.getTrueHeightAtPoint(x, z) != f[i]) {
				heightMap.setHeightAtPoint(f[i], x, z);
			}
		}
		updateHeights(f);
	}

	public void smooth() {
		heightMap.smooth(1f, 3);
		resetMaxHeight();
		updateHeights(heightMap.getHeightMap());
	}

	public void erode() {
		heightMap.flatten((byte) 2);
		resetMaxHeight();
		updateHeights(heightMap.getHeightMap());
	}

	public void normalizeDown() {
		maxHeight *= 0.9f;
		heightMap.normalizeTerrain(maxHeight);
		updateHeights(heightMap.getHeightMap());
	}

	public void updateHeights(float[] f) {
		if (rootNode.getChild("terrain") == terrain) {
			List<Vector2f> xz = new ArrayList<Vector2f>();
			List<Float> height = new ArrayList<Float>();
			for (int i = 0; i < f.length; i++) {
				// x and y are the indices into the heightMap.
				int x = (i % (size + 1));
				int z = ((i - x) / (size + 1));

				// But the "origin" on the terrain Spatial is in the middle (it
				// looks like?). So change the x/y coordinate, I guess...
				x = x - ((size + 1) / 2);
				z = z - ((size + 1) / 2);

				xz.add(new Vector2f(x, z));
				height.add(f[i]);
			}
			terrain.setHeight(xz, height);
			terrain.updateModelBound();
		}
	}

	/**
	 * Toggle the grid on/off. (This also toggles the arrows).
	 */
	public void toggleGrid() {
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
		Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m.setColor("Color", ColorRGBA.Green);
		m.getAdditionalRenderState().setWireframe(true);
		// m.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
		return m;
	}

	private void setupHeightMap(int size, long seed) {
		heightMap = null;
		try {
			heightMap = new FaultHeightMap(size + 1, 1000, FaultHeightMap.FAULTTYPE_COSINE, FaultHeightMap.FAULTSHAPE_LINE, 1f, 20f, seed);

			heightMap.load();

			heightMap.normalizeTerrain(maxHeight);

			heightMap.smooth(1, 10);

			// now back it up, for "reset".
			originalHeightMap = new AbstractHeightMap() {
				@Override
				public boolean load() {
					float[] f = heightMap.getHeightMap();
					float[] f2 = new float[f.length];
					System.arraycopy(f, 0, f2, 0, f.length);
					this.heightData = f2;
					// this.heightData = heightMap.getHeightMap();
					return true;
				}
			};

			originalHeightMap.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupCamera() {
		this.flyCam.setMoveSpeed(50f);
		this.flyCam.setRotationSpeed(3f);
		this.getCamera().setLocation(new Vector3f(90, 140, 250));
		this.getCamera().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
	}

	private void setupTerrain(int size, long seed) {
		int patchSize = 65;
		setupHeightMap(size, seed);
		terrain = new TerrainQuad("terrain", patchSize, size + 1, heightMap.getHeightMap());

		// setup LOD control.
		TerrainLodControl control = new TerrainLodControl(terrain, this.getCamera());
		control.setLodCalculator(new DistanceLodCalculator(patchSize, 1.7f));
		terrain.addControl(control);

	}

	private void setupRiver() {
		RiverFactory rf = new RiverFactory(20f, seed);
		float[] f = rf.createRiver(terrain.getHeightMap(), size + 1);
		updateHeights(f);
	}

	private void setupMesh() {
		terrain.depthFirstTraversal(new SceneGraphVisitor() {

			@Override
			public void visit(Spatial spatial) {
				Geometry g = (Geometry) spatial;
				Mesh m = g.getMesh();
			}
		});
	}

	private void setupGrid(Vector3f pos, int size, ColorRGBA color) {
		int scaleSize = size / gridScale;
		if (scaleSize * gridScale < size)
			scaleSize++;
		Geometry g = new Geometry("grid1", new Grid(scaleSize, scaleSize, gridScale));
		// Geometry g2 = new Geometry("grid2", new Grid(size, size, 1f));

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);

		g.setMaterial(mat);
		g.center().move(pos);

		Material mat2 = mat.clone();
		mat2.setColor("Color", ColorRGBA.LightGray);

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
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		if (showGrid) {
			if (nd == null) {
				nd = rootNode;
			}
			nd.attachChild(g);
		}
		return g;
	}

	@Override
	public void simpleInitApp() {
		setupKeys(); // G for grid, Equals (=) for smoothing.
		setupCamera();

		Material mat = setupMaterial();

		setupTerrain(size, seed);

		// setupMesh();
		// setupRiver();

		terrain.setMaterial(mat);

		rootNode.attachChild(terrain);

		setupGrid(Vector3f.ZERO, this.size, ColorRGBA.Blue);
		setupArrows(Vector3f.ZERO, 256);
		if (showGrid) {
			rootNode.attachChild(grid);
		}
	}

	public void simpleUpdate(float tpf) {
		if (smoothing) {
			smooth();
		}
		// if (eroding) {
		// erode();
		// }
	}

}
