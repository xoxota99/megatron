package com.skyline.application.state;

import java.util.*;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.collision.*;
import com.jme3.font.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.*;
import com.jme3.renderer.queue.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import com.jme3.scene.shape.*;
import com.jme3.water.*;
import com.skyline.application.*;
import com.skyline.application.events.*;
import com.skyline.application.tools.*;
import com.skyline.application.tools.population.*;
import com.skyline.application.tools.terrain.*;
import com.skyline.model.*;

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

	private static final ColorRGBA cursorColor = new ColorRGBA(251f / 255f, 130f / 255f, 130f / 255f, 1f);

	// This is the plane on which the land block "rests".
	// private static final Plane basePlane = new Plane(Vector3f.UNIT_Y, -125);

	private boolean showCone = false;
	private boolean showSpotlight = false;
	private MegatronApplication app;
	// private BulletAppState physics;
	// private int modifiers = 0;
	private boolean toolPressed;
	private Node cursor;
	// private Geometry cursorNormal;
	private Material cursorMaterial;
	private Material wireframeMaterial;
	private SpotLight cursorSpotlight = null;

	private boolean showWireframe = false;
	private boolean cursorVisible = false;
	private BitmapText coordText;
	private BitmapFont guiFont;
	private boolean showCoordinates = true;
	private Vector3f cursorPosition;
	public static final int CURSOR_HEIGHT = 25; // Height of the cursor above
												// the ground.
	// public static final int CURSOR_RENDER_HEIGHT_MIN = 20;
	public static final int CURSOR_RENDER_RADIUS_MAX = 256; // maximum radius of
															// the cursor
															// "circle".
	public static final int CURSOR_RENDER_RADIUS_MIN = 20; // minimum radius of
															// the cursor
															// "circle".

	public static class ModifierKey {
		public static final int SHIFT = 0x1;
		public static final int CTRL = 0x10;
		public static final int ALT = 0x100;
	}

	private int inputModifiers = 0;
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

			// guiFont =
			// app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
			guiFont = app.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

			setupMappings();
			setupCoordText();
			setupMenuText();
			setupToolSets();
			setupCursor();

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
		terrainTools.addTool(new GlobalRandomizationTool());
		terrainTools.addTool(new GlobalZeroTool());
		terrainTools.addTool(new GlobalFlattenTool());

		worldState.addObserver(terrainTools);
		toolBox.add(terrainTools);

		// Population Tools
		ToolSet popTools = new ToolSet("Population");
		popTools.addTool(new LocalPopDensityTool());
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
		tearDownCoordText();
		tearDownMenuText();
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
		Vector3f newPos = calculateCursorPosition();
//		if (!newPos.equals(cursorPosition)) {
			updateCoordText(cursorPosition);
			cursorPosition = newPos;
			worldState.triggerChangeEvent(new MouseMoveEvent(this, newPos));
//		}
		// update the location of the cursor.
		if (currentTool != null
				&& currentTool instanceof LocalTool
				&& ((LocalTool) currentTool).getCursorType() != CursorType.NONE
				&& cursor != null
				&& cursorPosition != null) {
			cursor.setLocalTranslation(cursorPosition.add(0, CURSOR_HEIGHT * app.getWorldRenderScale(), 0));
			if (cursorSpotlight != null) {
				cursorSpotlight.setPosition(cursorPosition.add(0, CURSOR_HEIGHT * app.getWorldRenderScale(), 0));
			}
			// position the "halo".

			float p = ((LocalTool) currentTool).getPower();
			float r = ((LocalTool) currentTool).getRadius();
			// float renderHeight = CURSOR_RENDER_HEIGHT_MIN +
			// ((CURSOR_RENDER_HEIGHT_MAX - CURSOR_RENDER_HEIGHT_MIN) * p *
			// app.getWorldRenderScale());
			// float renderRadius = CURSOR_RENDER_RADIUS_MIN +
			// ((CURSOR_RENDER_RADIUS_MAX - CURSOR_RENDER_RADIUS_MIN) * r);
			float spotConeAngle = .2f + ((1.5f - .2f) * r);

			// System.out.println("renderRadius=" + renderRadius);
			// // Vector3f loc = cursorPosition.add(0, CURSOR_RENDER_HEIGHT_MAX,
			// // 0);
			// // TODO: spotlight is always at the same Y, but changes angle for
			// // radius, and changes intensity for power? (maybe play with
			// inner
			// // vs. outer angle?)
			// // Geometry lightMark = (Geometry) cursor.getChild("lightMark");
			// // lightMark.setLocalTranslation(0, renderHeight, 0);
			//
			// float a, b, cosA;
			// b = (float) Math.sqrt((renderRadius * renderRadius) +
			// (CURSOR_RENDER_HEIGHT_MAX * CURSOR_RENDER_HEIGHT_MAX));
			// a = renderRadius;
			//
			// cosA = ((2 * b * b) - (a * a)) / (2 * b * b);
			//
			// float spotOuterAngle = (float) Math.acos(cosA);
			// float spotInnerAngle = spotOuterAngle * 0.8f;
			//
			// System.out.printf("spotOuterAngle is %f\n", spotOuterAngle);

			// System.out.println("power is " + p);
			if (cursorSpotlight != null) {
				cursorSpotlight.setSpotOuterAngle(spotConeAngle);
				cursorSpotlight.setSpotInnerAngle(spotConeAngle * .8f);
				cursorSpotlight.setColor(cursorColor.mult((0.4f + (p * 1.6f)) * 2f));
			}
		}
		if (toolPressed && currentTool != null && cursorPosition != null && currentTool.isContinuous()) {
			applyTool(currentTool, worldState, cursorPosition);
		}
	}

	private void applyTool(Tool t, WorldState worldState, Vector3f pos) {
		if (t instanceof LocalTool) {
			// Translate rendering coordinates into Map coordinates.
			int cursorX = (int) ((pos.x / app.getWorldRenderScale()) + (worldState.getSize() / 2));
			int cursorY = (int) ((pos.z / app.getWorldRenderScale()) + (worldState.getSize() / 2));
			// System.out.printf("(%d,%d)\n",cursorX,cursorY);
			((LocalTool) t).execute(worldState, cursorX, cursorY, inputModifiers);
		} else if (t instanceof GlobalTool) {
			((GlobalTool) t).execute(worldState, inputModifiers);
		}
	}

	private void updateCoordText(Vector3f intersection) {
		if (showCoordinates && intersection != null && coordText != null) {
			coordText.setText(intersection.toString());
			coordText.setLocalTranslation(app.getCamera().getWidth() - coordText.getLineWidth(), coordText.getLineHeight(), 0);

			// StringBuilder sb = new StringBuilder();
			// if (currentToolSet != null) {
			// sb.append(currentToolSet.getName() + " : ");
			// }
			// if (currentTool != null) {
			// sb.append(currentTool.getName());
			// if (currentTool instanceof LocalTool) {
			// sb.append(String.format(" (P: %f,R: %f)", ((LocalTool)
			// currentTool).getPower(), ((LocalTool) currentTool).getRadius()));
			// }
			// sb.append(", ");
			// }
			// sb.append("intersect: " + intersection.toString());
			// coordText.setText(sb.toString());
		}
	}

	private void tearDownCoordText() {
		app.getGuiNode().detachChild(coordText);
	}

	private void setupCoordText() {
		coordText = new BitmapText(guiFont, false);
		coordText.setLocalTranslation(app.getCamera().getWidth() - 100, coordText.getLineHeight(), 0);

		app.getGuiNode().attachChild(coordText);
	}

	private void setupMenuText() {

	}

	private void tearDownMenuText() {

	}

	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (!looking) {
			float inc = .05f;
			// System.out.println("not Looking. So processing " + name + ", " +
			// value);
			if (name.equals(INPUT_TOOL)) { // Use your tool.
				// System.out.println("Use your tool.");
				toolPressed = value;
				if (toolPressed && currentTool != null && cursorPosition != null) {
					applyTool(currentTool, worldState, cursorPosition);
				}
			} else if (value && name.startsWith(INPUT_TOOL_SELECT_PREFIX)) {
				if (currentToolSet != null && currentToolSet.size() > Integer.parseInt(name.substring(name.length() - 1))) {
					Tool newTool = currentToolSet.getTool(Integer.parseInt(name.substring(name.length() - 1)));
					if (newTool instanceof GlobalTool) {
						// just execute it and forget it.
						((GlobalTool) newTool).execute(worldState, inputModifiers);
					} else {
						currentTool = newTool;
						updateCursorGeometry();
						setCursorVisible(currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE);
					}
				}
			} else if (value && name.startsWith(INPUT_TOOLSET_SELECT_PREFIX)) {
				currentToolSet = toolBox.get(Integer.parseInt(name.substring(name.length() - 1)));

				// TODO: Only do this if the toolset actually changes.
				// TODO: Keep track of currently selected tools by toolset, and
				// go back to that tool if we go back to the toolset.
				currentTool = null;
				setCursorVisible(false);
			} else if (name.equals(INPUT_TOOL_DECREASE)) {
				if ((currentTool != null && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE)) {
					LocalTool t = (LocalTool) currentTool;
					if (getInputModifier(ModifierKey.ALT)) {
						// System.out.println("Decrease Power.");
						// ((LocalTool) currentTool).setPower((int)
						// Math.max(((LocalTool) currentTool).getPower() - 1,
						// CURSOR_RENDER_HEIGHT_MIN));
						((LocalTool) currentTool).setPower(Math.max(((LocalTool) currentTool).getPower() - inc, 0f));
					} else {
						// System.out.println("Decrease Radius.");
						// ((LocalTool) currentTool).setRadius((int)
						// Math.max(((LocalTool) currentTool).getRadius() - 1,
						// CURSOR_RENDER_RADIUS_MIN));
						((LocalTool) currentTool).setRadius(Math.max(((LocalTool) currentTool).getRadius() - inc, 0f));
					}
					updateCursorGeometry();
				}
			} else if (name.equals(INPUT_TOOL_INCREASE)) {
				if ((currentTool != null && currentTool instanceof LocalTool && ((LocalTool) currentTool).getCursorType() != CursorType.NONE)) {
					LocalTool t = (LocalTool) currentTool;
					if (getInputModifier(ModifierKey.ALT)) {
						// System.out.println("Increase Power.");
						// ((LocalTool) currentTool).setPower((int)
						// Math.min(((LocalTool) currentTool).getPower() + 1,
						// CURSOR_RENDER_HEIGHT_MAX));
						((LocalTool) currentTool).setPower(Math.min(((LocalTool) currentTool).getPower() + inc, 1f));
					} else {
						// System.out.println("Increase Radius.");
						// ((LocalTool) currentTool).setRadius((int)
						// Math.min(((LocalTool) currentTool).getRadius() + 1,
						// CURSOR_RENDER_RADIUS_MAX));
						((LocalTool) currentTool).setRadius(Math.min(((LocalTool) currentTool).getRadius() + inc, 1f));
					}
					updateCursorGeometry();
				}
			}
			// } else {
			// System.out.println("Looking, so ignoring input " + name + ", " +
			// value);
		}
		if (name.equals(MOD_SHIFT)) {
			toggleInputModifier(ModifierKey.SHIFT, value);
		} else if (name.equals(MOD_CTRL)) {
			toggleInputModifier(ModifierKey.CTRL, value);
		} else if (name.equals(MOD_ALT)) {
			toggleInputModifier(ModifierKey.ALT, value);
		} else if (value && name.equals(TOGGLE_WIREFRAME)) {
			toggleWireframe();
		} else if (name.equals("FLYCAM_RotateDrag")) {
			if (looking != value) {
				// System.out.println("Looking = " + value);
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
		// System.out.println("updateCursorGeometry");
		if (currentTool != null && currentTool instanceof LocalTool) {
			if (cursor == null) {
				setupCursor();
			} else {
				if (showCone) {
					float renderRadius = CURSOR_RENDER_RADIUS_MIN + ((CURSOR_RENDER_RADIUS_MAX - CURSOR_RENDER_RADIUS_MIN) * ((LocalTool) currentTool).getRadius() * app.getWorldRenderScale() / 2);
					// float renderHeight = CURSOR_RENDER_HEIGHT_MIN +
					// ((CURSOR_RENDER_HEIGHT_MAX - CURSOR_RENDER_HEIGHT_MIN) *
					// ((LocalTool) currentTool).getPower() *
					// app.getWorldRenderScale());
					Cylinder mrk = new Cylinder(10, 32, renderRadius, 1, CURSOR_HEIGHT * app.getWorldRenderScale(), true, false);

					Geometry cone = (Geometry) cursor.getChild("Cone");
					cone.setLocalTranslation(0, CURSOR_HEIGHT * app.getWorldRenderScale() / 2, 0);
					cone.setMesh(mrk);
				}
				Geometry cursorNormal = (Geometry) cursor.getChild("Arrow");
				// cursorNormal.setLocalTranslation(0, -renderHeight / 2, 0);
				cursorNormal.setLocalTranslation(0, -CURSOR_HEIGHT * app.getWorldRenderScale(), 0);
				cursorNormal.setLocalScale(CURSOR_HEIGHT * app.getWorldRenderScale());

				// System.out.printf("renderHeight: %f", renderHeight);

				// System.out.println("renderRadius = " + renderRadius);
				// float a = renderRadius;
				// System.out.println("a=" + a);
				// float b = (float) Math.sqrt((renderRadius * renderRadius / 4)
				// + (renderHeight * renderHeight));
				// System.out.println("b=" + b);
				// float c = b;
				// System.out.println("c=" + c);
				// float cosA = ((b * b) + (c * c) - (a * a)) / (2 * b * c);
				// System.out.println("cosA=" + cosA);
				// float A = (float) Math.acos(cosA);
				// System.out.println("A=" + A + " (in Degrees, that's " + (A *
				// FastMath.RAD_TO_DEG) + ")");
				//
				// System.out.printf("@height %f, angle = %f\n", renderHeight,
				// A);

				// cursorSpotlight.setSpotOuterAngle(renderRadius *
				// FastMath.DEG_TO_RAD);
				// cursorSpotlight.setSpotInnerAngle(renderRadius * .75f *
				// FastMath.DEG_TO_RAD);
				// cursorSpotlight.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
				// cursorSpotlight.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
				cursor.updateModelBound();
			}
		}
	}

	private void tearDownCursorGeometry() {
		setCursorVisible(false); // detaches from rootNode.
	}

	private void setupCursor() {
		// System.out.println("setupCursor");

		if (currentTool != null && currentTool instanceof LocalTool) {
			// float renderHeight = CURSOR_RENDER_HEIGHT_MIN +
			// ((CURSOR_RENDER_HEIGHT_MAX - CURSOR_RENDER_HEIGHT_MIN) *
			// ((LocalTool) currentTool).getPower() *
			// app.getWorldRenderScale());

			wireframeMaterial = Util.getWireframeMaterial(app.getAssetManager(), cursorColor);

			cursorMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			cursorMaterial.setColor("Color", cursorColor);
			cursorMaterial.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);

			cursor = new Node("Cursor");
			if (showCone) {
				float renderRadius = CURSOR_RENDER_RADIUS_MIN + ((CURSOR_RENDER_RADIUS_MAX - CURSOR_RENDER_RADIUS_MIN) * ((LocalTool) currentTool).getRadius() * app.getWorldRenderScale() / 2);
				Cylinder mrk = new Cylinder(10, 32, renderRadius, 1, CURSOR_HEIGHT * app.getWorldRenderScale(), true, false);
				Geometry cone = new Geometry("Cone");
				cone.setMesh(mrk);
				cone.setQueueBucket(RenderQueue.Bucket.Transparent);

				cone.rotate((float) (Math.PI / 2), 0, 0);
				cone.setLocalTranslation(0, CURSOR_HEIGHT * app.getWorldRenderScale() / 2, 0);
				// cursor.setCullHint(CullHint.Never);
				cone.setMaterial(wireframeMaterial);
				cursor.attachChild(cone);
			}

			// surface normal marker
			Arrow arrow = new Arrow(Vector3f.UNIT_Y);
			Geometry norm = new Geometry("Arrow");
			norm.setMesh(arrow);
			norm.setMaterial(wireframeMaterial);
			norm.setLocalTranslation(0, -CURSOR_HEIGHT * app.getWorldRenderScale(), 0);
			norm.setLocalScale(-CURSOR_HEIGHT * app.getWorldRenderScale());
			cursor.attachChild(norm);

			int r = Math.max(10,(int) (CURSOR_HEIGHT / 10 * app.getWorldRenderScale()));
			Sphere sphere = new Sphere(r, r, r);
			Geometry lightMark = new Geometry("lightMark");
			lightMark.setMesh(sphere);
			lightMark.setMaterial(cursorMaterial);
			lightMark.setLocalTranslation(0, 0, 0);
			cursor.attachChild(lightMark);

			// System.out.printf("renderHeight: %f", renderHeight);

			if (showSpotlight) {
				cursorSpotlight = new SpotLight();
				cursorSpotlight.setName("cursorSpotlight");
				cursorSpotlight.setSpotRange(0);
				cursorSpotlight.setDirection(Vector3f.UNIT_Y.mult(-1));
				cursorSpotlight.setColor(cursorColor);
				cursorSpotlight.setSpotOuterAngle(16 * FastMath.DEG_TO_RAD);
				cursorSpotlight.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
				cursorSpotlight.setPosition(Vector3f.UNIT_Y.mult(CURSOR_HEIGHT * app.getWorldRenderScale()));
				cursorSpotlight.setSpotRange(CURSOR_HEIGHT * 2 * app.getWorldRenderScale());
			}
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
					// System.out.println("adding cursor");
					n.attachChild(cursor);
					if (cursorSpotlight != null) {
						n.addLight(cursorSpotlight);
					}
				}
			} else if (app.getRootNode().hasChild(cursor)) {
				// System.out.println("detaching cursor");
				n.detachChild(cursor);
				if (cursorSpotlight != null) {
					n.removeLight(cursorSpotlight);
				}
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

	/**
	 * 
	 * @param modifier
	 * @return true if the specified modifier is "on", otherwise false.
	 */
	public boolean getInputModifier(int modifier) {
		return (inputModifiers & modifier) > 0;
	}

	/**
	 * Toggle the specified modifier key (ALT, SHIFT, or CTRL) on or off.
	 * 
	 * @param modifier
	 */
	public void toggleInputModifier(int modifier, boolean val) {
		if (val) {
			this.inputModifiers |= modifier;
		} else {
			this.inputModifiers &= ~modifier;
		}
	}

	public int getInputModifiers() {
		return inputModifiers;
	}

	public void setInputModifiers(int inputModifiers) {
		this.inputModifiers = inputModifiers;
	}

	public Tool getCurrentTool() {
		return currentTool;
	}
}
