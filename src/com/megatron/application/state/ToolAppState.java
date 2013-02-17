package com.megatron.application.state;

import java.util.*;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.collision.*;
import com.jme3.font.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.renderer.queue.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.scene.shape.*;
import com.megatron.application.*;
import com.megatron.application.tools.*;
import com.megatron.model.*;

/**
 * ToolAppState allows you to:
 * <ul>
 * <li>Select from the list of currently available tools (in the currently
 * active toolset).</li>
 * <li>Trigger a tool action - Track and render the cursor (if the currently
 * selected tool supports it).</li>
 * <li>Modify the tool action by tracking modifier (shift, control, alt) keys.</li>
 * </ul>
 * 
 * ToolAppState should:
 * <ul>
 * <li>Maintain the current {@link Tool} in use.</li>
 * <li>Execute the current {@link Tool} when necessary</li>
 * </ul>
 * 
 * Every tool should:
 * <ul>
 * <li>Implement a common {@link Tool} interface</li>
 * <li>Specify a {@link CursorType}</li>
 * <li>Have a human-friendly name (such as "Raise/Lower Terrain")</li>
 * <li>Act either "globally" (extend {@link GlobalTool}) or "locally" (extend
 * {@link LocalTool})</li>
 * <li>Roll up into a {@link ToolSet} (Terrain Tools, Road Tools, Building
 * Tools, etc.)</li>
 * </ul>
 * 
 * @author philippd
 * 
 */
public class ToolAppState extends AbstractAppState implements ActionListener {

	// Use the currently selected tool, in the currently selected mode.
	private static final String INPUT_TOOL = ToolAppState.class.getName() + ".INPUT_TOOL";
	// "Decrease" the tool.
	private static final String INPUT_TOOL_DECREASE = ToolAppState.class.getName() + ".INPUT_TOOL_DECREASE";
	// "Increase" the tool.
	private static final String INPUT_TOOL_SELECT_PREFIX = ToolAppState.class.getName() + ".INPUT_TOOL_SELECT_";
	private static final String INPUT_TOOLSET_SELECT_PREFIX = ToolAppState.class.getName() + ".INPUT_TOOLSET_SELECT_";
	private static final String INPUT_TOOL_INCREASE = ToolAppState.class.getName() + ".INPUT_TOOL_INCREASE";
	private static final String MOD_CTRL = ToolAppState.class.getName() + ".MOD_CTRL";
	private static final String MOD_SHIFT = ToolAppState.class.getName() + ".MOD_SHIFT";
	private static final String MOD_ALT = ToolAppState.class.getName() + ".MOD_ALT";

	private static final String TOGGLE_WIREFRAME = ToolAppState.class.getName() + ".TOGGLE_WIREFRAME";

	private static final ColorRGBA cursorColor = new ColorRGBA(251f / 255f, 130f / 255f, 130f / 255f, 0.2f);

	// This is the plane on which the land block "rests".
	// private static final Plane basePlane = new Plane(Vector3f.UNIT_Y, -125);

	private MegatronApplication app;
	// private BulletAppState physics;
	// private int modifiers = 0;
	private boolean toolPressed;
	private Node cursor;
	// private Geometry cursorNormal;
	private Material cursorMaterial;
	private Material wireframeMaterial;

	private boolean showWireframe = false;
	private boolean cursorVisible = false;
	private BitmapText hintText;
	private BitmapFont guiFont;
	private boolean showHintText = true;
	private Vector3f cursorPosition;
	public static final int CURSOR_RENDER_HEIGHT_MAX = 64;
	public static final int CURSOR_RENDER_HEIGHT_MIN = 20;
	public static final int CURSOR_RENDER_RADIUS_MAX = 64;
	public static final int CURSOR_RENDER_RADIUS_MIN = 10;

	// When we are dragging to move, no tool is selected. We've "suspended" tool
	// selection.
	// private static final int SUSPEND_TOOL = -1;
	private boolean looking = false; // true while we're looking around.
	// private int cursorRadius = CURSOR_POWER_MAX / 2;
	// private int cursorPower = CURSOR_RADIUS_MAX / 2;
	// private int oldTool;
	private WorldState worldState;
	private List<ToolSet> toolBox;
	private Tool currentTool;
	private ToolSet currentToolSet;

	@Override
	public void initialize(AppStateManager stateManager, Application app) {

		// System.out.println("initialize");
		this.app = (MegatronApplication) app; // can cast Application to
												// something
												// more specific
												// this.physics =
												// this.app.getStateManager().getState(BulletAppState.class);
		this.worldState = this.app.getWorldState();

		if (!super.isInitialized()) {

			// get rid of flyCam "zoom" command (so we can use the mousewheel)
			if (app.getInputManager().hasMapping("FLYCAM_ZoomIn")) {
				app.getInputManager().deleteMapping("FLYCAM_ZoomIn");
				app.getInputManager().deleteMapping("FLYCAM_ZoomOut");
			}

			guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

			setupMappings();
			setupHintText();
			setupToolSets();
			setupCursorGeometry();

			setCursorVisible(currentTool != null && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE);
		}

		super.initialize(stateManager, app);
	}

	private void setupToolSets() {
		this.toolBox = new ArrayList<ToolSet>();
		ToolSet terrainTools = new ToolSet("Terrain");

		Tool et = new LocalElevationTool();
		terrainTools.addTool(et);
		terrainTools.addTool(new LocalSmoothTool());
		terrainTools.addTool(new GlobalTool("Zero Out", "Zeros out all terrain") {

			@Override
			public void execute(WorldState context) {
				System.out.println("Ad Hoc Tool Alert!");
			}

			@Override
			public boolean isContinuous() {
				return false;
			}
		});

		worldState.addObserver(terrainTools);
		toolBox.add(terrainTools);

		// Population Tools
		ToolSet popTools = new ToolSet("Population");
		worldState.addObserver(popTools);
		toolBox.add(popTools);

		// Street Tools
		ToolSet streetTools = new ToolSet("Streets");
		worldState.addObserver(streetTools);
		toolBox.add(streetTools);

		// Zoning Tools
		ToolSet zoneTools = new ToolSet("Zoning");
		worldState.addObserver(zoneTools);
		toolBox.add(zoneTools);

		// Construction Tools
		ToolSet conTools = new ToolSet("Construction");
		worldState.addObserver(conTools);
		toolBox.add(conTools);

		currentToolSet = terrainTools;
		currentTool = et;
	}

	private void toggleWireframe() {
		showWireframe = !showWireframe;
		if (showWireframe) {
			cursor.setMaterial(wireframeMaterial);
		} else {
			cursor.setMaterial(cursorMaterial);
		}
	}

	private void tearDownMappings() {
		InputManager inputManager = app.getInputManager();
		inputManager.deleteMapping(INPUT_TOOL);
		inputManager.deleteMapping(INPUT_TOOL_DECREASE);
		inputManager.deleteMapping(INPUT_TOOL_INCREASE);

		// Keys, 0-9, F1-F12
		for (int i = 0; i < 12; i++) {
			if (inputManager.hasMapping(INPUT_TOOL_SELECT_PREFIX + i))
				inputManager.deleteMapping(INPUT_TOOL_SELECT_PREFIX + i);
			if (inputManager.hasMapping(INPUT_TOOLSET_SELECT_PREFIX + i))
				inputManager.deleteMapping(INPUT_TOOLSET_SELECT_PREFIX + i);
		}

		// listen for modifier keys.
		inputManager.deleteMapping(MOD_SHIFT);
		inputManager.deleteMapping(MOD_CTRL);
		inputManager.deleteMapping(MOD_ALT);
		inputManager.deleteMapping(TOGGLE_WIREFRAME);

		inputManager.removeListener(this);
	}

	private void setupMappings() {
		InputManager inputManager = app.getInputManager();
		// Use Tool. (Left Click)
		inputManager.addMapping(INPUT_TOOL, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, INPUT_TOOL);

		// Decrease Tool. (Scroll Down)
		inputManager.addMapping(INPUT_TOOL_DECREASE, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addListener(this, INPUT_TOOL_DECREASE);

		// Increase tool. (Scroll Up)
		inputManager.addMapping(INPUT_TOOL_INCREASE, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addListener(this, INPUT_TOOL_INCREASE);

		// Select Tool. (0-9).
		int[] toolTrigs = {
				KeyInput.KEY_0,
				KeyInput.KEY_1,
				KeyInput.KEY_2,
				KeyInput.KEY_3,
				KeyInput.KEY_4,
				KeyInput.KEY_5,
				KeyInput.KEY_6,
				KeyInput.KEY_7,
				KeyInput.KEY_8,
				KeyInput.KEY_9
		};

		for (int i = 0; i < 10; i++) {
			// System.out.println("handle key " + i + " = " + keyTrigs[i]);
			inputManager.addMapping(INPUT_TOOL_SELECT_PREFIX + i, new KeyTrigger(toolTrigs[i]));
			inputManager.addListener(this, INPUT_TOOL_SELECT_PREFIX + i);
		}

		int[] toolSetTrigs = {
				KeyInput.KEY_F1,
				KeyInput.KEY_F2,
				KeyInput.KEY_F3,
				KeyInput.KEY_F4,
				KeyInput.KEY_F5,
				KeyInput.KEY_F6,
				KeyInput.KEY_F7,
				KeyInput.KEY_F8,
				KeyInput.KEY_F9,
				KeyInput.KEY_F10,
				KeyInput.KEY_F11,
				KeyInput.KEY_F12
		};
		for (int i = 0; i < 10; i++) {
			inputManager.addMapping(INPUT_TOOLSET_SELECT_PREFIX + i, new KeyTrigger(toolSetTrigs[i]));
			inputManager.addListener(this, INPUT_TOOLSET_SELECT_PREFIX + i);
		}

		// listen for modifier keys.
		inputManager.addMapping(MOD_SHIFT, new KeyTrigger(KeyInput.KEY_LSHIFT));
		inputManager.addMapping(MOD_SHIFT, new KeyTrigger(KeyInput.KEY_RSHIFT));
		inputManager.addListener(this, MOD_SHIFT);

		inputManager.addMapping(MOD_CTRL, new KeyTrigger(KeyInput.KEY_LCONTROL));
		inputManager.addMapping(MOD_CTRL, new KeyTrigger(KeyInput.KEY_RCONTROL));
		inputManager.addListener(this, MOD_CTRL);

		inputManager.addMapping(MOD_ALT, new KeyTrigger(KeyInput.KEY_LMENU));
		inputManager.addMapping(MOD_ALT, new KeyTrigger(KeyInput.KEY_RMENU));
		inputManager.addListener(this, MOD_ALT);

		// Toggle Wireframe
		inputManager.addMapping(TOGGLE_WIREFRAME, new KeyTrigger(KeyInput.KEY_TAB));
		inputManager.addListener(this, TOGGLE_WIREFRAME);

		inputManager.addListener(this, "FLYCAM_RotateDrag");

	}

	@Override
	public void cleanup() {
		super.cleanup();
		tearDownMappings();
		tearDownHintText();
		tearDownCursorGeometry();
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Pause and unpause
		super.setEnabled(enabled);
	}

	// Note that update is only called while the state is both attached and
	// enabled.
	@Override
	public void update(float tpf) {
		// System.out.println(tpf);
		// Where is the cursor right now?
		cursorPosition = calculateCursorPosition();
		updateHintText(cursorPosition);
		// update the location of the cursor.
		if (currentTool != null
				&& currentTool instanceof LocalTool
				&& ((LocalTool) currentTool).getCursorType() != CursorType.NONE
				&& cursor != null
				&& cursorPosition != null) {
			cursor.setLocalTranslation(cursorPosition.clone().add(0, ((LocalTool) currentTool).getPower() * app.getWorldRenderScale() / 2, 0));
		}
		if (toolPressed && currentTool != null && currentTool.isContinuous()) {
			applyTool(currentTool, worldState, cursorPosition);
		}
	}

	private void applyTool(Tool t, WorldState state, Vector3f pos) {
		if (t instanceof LocalTool) {
			// Translate rendering cordinates into heightMap coordinates.
			int cursorX = (int) ((pos.x / app.getWorldRenderScale()) + (state.getSize() / 2));
			int cursorY = (int) ((pos.z / app.getWorldRenderScale()) + (state.getSize() / 2));
			((LocalTool) t).execute(state, cursorX, cursorY);
		} else if (t instanceof GlobalTool) {
			((GlobalTool) t).execute(state);
		}
	}

	private void updateHintText(Vector3f intersection) {
		if (showHintText && intersection != null && hintText != null) {
			StringBuilder sb = new StringBuilder();
			if (currentToolSet != null) {
				sb.append(currentToolSet.getName() + " : ");
			}
			if (currentTool != null) {
				sb.append(currentTool.getName());
				if (currentTool instanceof LocalTool) {
					sb.append(String.format(" (P: %f,R: %f)", ((LocalTool) currentTool).getPower(), ((LocalTool) currentTool).getRadius()));
				}
				sb.append(", ");
			}
			sb.append("intersect: " + intersection.toString());
			hintText.setText(sb.toString());
		}
	}

	private void tearDownHintText() {
		app.getGuiNode().detachChild(hintText);
	}

	private void setupHintText() {
		hintText = new BitmapText(guiFont, false);
		hintText.setLocalTranslation(0, app.getCamera().getHeight(), 0);
		hintText.setText("Hint");
		app.getGuiNode().attachChild(hintText);
	}

	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (!looking) {
			// System.out.println("not Looking. So processing " + name + ", " +
			// value);
			if (name.equals(INPUT_TOOL)) { // Use your tool.
				// System.out.println("Use your tool.");
				toolPressed = value;
				if (toolPressed && currentTool != null) {
					applyTool(currentTool, worldState, cursorPosition);
				}
			} else if (value && name.startsWith(INPUT_TOOL_SELECT_PREFIX)) {
				if (currentToolSet != null && currentToolSet.size() > Integer.parseInt(name.substring(name.length() - 1))) {
					currentTool = currentToolSet.getTool(Integer.parseInt(name.substring(name.length() - 1)));
					updateCursorGeometry();
					setCursorVisible(currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE);
				}
			} else if (value && name.startsWith(INPUT_TOOLSET_SELECT_PREFIX)) {
				currentToolSet = toolBox.get(Integer.parseInt(name.substring(name.length() - 1)));

				// TODO: Only do this if the toolset actually changes.
				// TODO: Keep track of currently selected tools by toolset, and
				// go back to that tool if we go back to the toolset.
				currentTool = null;
				setCursorVisible(false);
			} else if (name.equals(INPUT_TOOL_DECREASE)) {
				if ((currentTool != null && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE)) { // legacy
																																					// mode.
					if ((worldState.getInputModifiers() & WorldState.ALT) > 0) {
						System.out.println("Decrease Power.");
						// ((LocalTool) currentTool).setPower((int)
						// Math.max(((LocalTool) currentTool).getPower() - 1,
						// CURSOR_RENDER_HEIGHT_MIN));
						((LocalTool) currentTool).setPower(Math.max(((LocalTool) currentTool).getPower() - 0.01f, 0.01f));
					} else {
						System.out.println("Decrease Radius.");
						// ((LocalTool) currentTool).setRadius((int)
						// Math.max(((LocalTool) currentTool).getRadius() - 1,
						// CURSOR_RENDER_RADIUS_MIN));
						((LocalTool) currentTool).setRadius(Math.max(((LocalTool) currentTool).getRadius() - 0.01f, 0.01f));
					}
					updateCursorGeometry();
				}
			} else if (name.equals(INPUT_TOOL_INCREASE)) {
				if ((currentTool != null && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE)) { // legacy
																																					// mode.
					if ((worldState.getInputModifiers() & WorldState.ALT) > 0) {
						System.out.println("Increase Power.");
						// ((LocalTool) currentTool).setPower((int)
						// Math.min(((LocalTool) currentTool).getPower() + 1,
						// CURSOR_RENDER_HEIGHT_MAX));
						((LocalTool) currentTool).setPower(Math.min(((LocalTool) currentTool).getPower() + 0.01f, 1.0f));
					} else {
						System.out.println("Increase Radius.");
						// ((LocalTool) currentTool).setRadius((int)
						// Math.min(((LocalTool) currentTool).getRadius() + 1,
						// CURSOR_RENDER_RADIUS_MAX));
						((LocalTool) currentTool).setRadius(Math.min(((LocalTool) currentTool).getRadius() + 0.01f, 1.0f));
					}
					updateCursorGeometry();
				}
			}
			// } else {
			// System.out.println("Looking, so ignoring input " + name + ", " +
			// value);
		}
		if (name.equals(MOD_SHIFT)) {
			if (value) {
				worldState.setInputModifier(WorldState.SHIFT);
			} else {
				worldState.clearInputModifier(WorldState.SHIFT);
			}
		} else if (name.equals(MOD_CTRL)) {
			if (value) {
				worldState.setInputModifier(WorldState.CTRL);
			} else {
				worldState.clearInputModifier(WorldState.CTRL);
			}
		} else if (name.equals(MOD_ALT)) {
			if (value) {
				worldState.setInputModifier(WorldState.ALT);
			} else {
				worldState.clearInputModifier(WorldState.ALT);
			}
		} else if (value && name.equals(TOGGLE_WIREFRAME)) {
			toggleWireframe();
		} else if (name.equals("FLYCAM_RotateDrag")) {
			if (looking != value) {
				System.out.println("Looking = " + value);
				looking = value;
				if (looking) {
					// hide the cursor.
					setCursorVisible(false);
				} else {
					// show the cursor.
					setCursorVisible(currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE);
					// oldTool = SUSPEND_TOOL;
				}
			}
		}

	}

	/**
	 * Refresh the cursor with the current power and radius.
	 */
	private void updateCursorGeometry() {
		System.out.println("updateCursorGeometry");
		if (currentTool != null && currentTool instanceof LocalTool) {
			if (cursor == null) {
				setupCursorGeometry();
			} else {
				float renderRadius = CURSOR_RENDER_RADIUS_MIN + ((CURSOR_RENDER_RADIUS_MAX - CURSOR_RENDER_RADIUS_MIN) * ((LocalTool) currentTool).getRadius() * app.getWorldRenderScale() / 2);
				float renderHeight = CURSOR_RENDER_HEIGHT_MIN + ((CURSOR_RENDER_HEIGHT_MAX - CURSOR_RENDER_HEIGHT_MIN) * ((LocalTool) currentTool).getPower() * app.getWorldRenderScale());
				Cylinder mrk = new Cylinder(10, 32, renderRadius, 1, renderHeight, true, false);

				Geometry cone = (Geometry) cursor.getChild("Cone");
				cone.setLocalTranslation(0, renderHeight / 2, 0);
				cone.setMesh(mrk);

				Geometry cursorNormal = (Geometry) cursor.getChild("Arrow");
				// cursorNormal.setLocalTranslation(0, -renderHeight / 2, 0);
				cursorNormal.setLocalTranslation(0, 0, 0);
				cursorNormal.setLocalScale(renderHeight / 2);

				System.out.printf("renderHeight: %f", renderHeight);

				cursor.updateModelBound();
			}
		}
	}

	private void tearDownCursorGeometry() {
		setCursorVisible(false); // detaches from rootNode.
	}

	private void setupCursorGeometry() {
		System.out.println("setupCursor");
		if (currentTool != null && currentTool instanceof LocalTool) {
			float renderRadius = CURSOR_RENDER_RADIUS_MIN + ((CURSOR_RENDER_RADIUS_MAX - CURSOR_RENDER_RADIUS_MIN) * ((LocalTool) currentTool).getRadius() * app.getWorldRenderScale() / 2);
			float renderHeight = CURSOR_RENDER_HEIGHT_MIN + ((CURSOR_RENDER_HEIGHT_MAX - CURSOR_RENDER_HEIGHT_MIN) * ((LocalTool) currentTool).getPower() * app.getWorldRenderScale());
			Cylinder mrk = new Cylinder(10, 32, renderRadius, 1, renderHeight, true, false);

			Geometry cone = new Geometry("Cone");
			cone.setMesh(mrk);
			cone.setQueueBucket(RenderQueue.Bucket.Transparent);

			cone.rotate((float) (Math.PI / 2), 0, 0);
			cone.setLocalTranslation(0, renderHeight / 2, 0);
			// cursor.setCullHint(CullHint.Never);

			cursorMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			cursorMaterial.setColor("Color", cursorColor);
			cursorMaterial.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);

			cone.setMaterial(cursorMaterial);

			wireframeMaterial = Util.getWireframeMaterial(app.getAssetManager(), cursorColor);

			// surface normal marker
			Arrow arrow = new Arrow(Vector3f.UNIT_Y);
			arrow.setLineWidth(4f);

			Geometry norm = new Geometry("Arrow");
			norm.setMesh(arrow);
			norm.setMaterial(cursorMaterial);
			norm.setLocalTranslation(0, 0, 0);
			norm.setLocalScale(renderHeight / 2);

			System.out.printf("renderHeight: %f", renderHeight);

			cursor = new Node("Cursor");
			cursor.attachChild(cone);
			cursor.attachChild(norm);

			setCursorVisible(cursorVisible && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE); // force
																																					// re-attach
		}
	}

	private Vector3f calculateCursorPosition() {
		Vector3f origin = null;// app.getCamera().getWorldCoordinates(new
								// Vector2f(app.getCamera().getWidth() / 2,
								// app.getCamera().getHeight() / 2), 0f);
		Vector3f direction = null;
		if (app.getInputManager().isCursorVisible()) {
			origin = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f);
			direction = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0.3f);
		} else {
			origin = app.getCamera().getWorldCoordinates(new Vector2f(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2), 0f);
			direction = app.getCamera().getWorldCoordinates(new Vector2f(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2), 0.3f);
		}

		direction.subtractLocal(origin).normalizeLocal();

		Ray ray = new Ray(origin, direction);
		CollisionResults results = new CollisionResults();

		// This will cause collision with Non/terrain objects. (colliding with
		// rootNode)
		int numCollisions = app.getRootNode().collideWith(ray, results);

		if (numCollisions > 0) {
			// since we're colliding with everything, we want to make sure we
			// don't collide with the cursor itself...
			for (CollisionResult hit : results) {
				// these will be ordered closest to farthest.
				if (cursor == null || !cursor.hasChild(hit.getGeometry())) {
					return hit.getContactPoint();
				}
			}
			// CollisionResult hit = results.getClosestCollision();
			// return hit.getContactPoint();
		}
		// System.out.println("No Collision. Extrapolating to XZ-plane.");
		Vector3f holder = Vector3f.ZERO;
		Plane basePlane = new Plane(Vector3f.UNIT_Y, WorldState.TERRAIN_MIN * app.getHeightScale());
		if (ray.intersectsWherePlane(basePlane, holder)) {
			return holder;
		}

		return null;
	}

	public boolean isShowWireframe() {
		return showWireframe;
	}

	public void setShowWireframe(boolean showWireframe) {
		this.showWireframe = showWireframe;
	}

	public void setCursorVisible(boolean isVisible) {
		setCursorVisible(isVisible, app.getRootNode());
	}

	public void setCursorVisible(boolean isVisible, Node n) {
		if (n != null && cursor != null) {
			if (isVisible) {
				if (!n.hasChild(cursor)) {
					n.attachChild(cursor);
				}
			} else if (app.getRootNode().hasChild(cursor)) {
				// System.out.println("detaching cursor");
				n.detachChild(cursor);
			}
		}
	}

	public boolean isCursorVisible() {
		return cursorVisible;
	}

	public Vector3f getCursorPosition() {
		return cursorPosition;
	}

	public void setCursorPosition(Vector3f cursorPosition) {
		// System.out.println(String.format("setCursorPosition (%d,%d,%d)",
		// cursorPosition.x, cursorPosition.y, cursorPosition.z));
		this.cursorPosition = cursorPosition;
	}

	public boolean isToolPressed() {
		return toolPressed;
	}

	public void setToolPressed(boolean toolPressed) {
		this.toolPressed = toolPressed;
	}
}
