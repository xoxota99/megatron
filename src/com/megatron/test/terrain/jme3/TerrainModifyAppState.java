package com.megatron.test.terrain.jme3;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.util.BufferUtils;

public class TerrainModifyAppState extends AbstractAppState {

	public static final String INPUT_MAPPING_CAMERA_POS = "SIMPLEAPP_CameraPos";
	public static final String INPUT_MAPPING_MEMORY = "SIMPLEAPP_Memory";

	private Application app;
	private TerrainModifyKeyListener keyListener = new TerrainModifyKeyListener();
	private InputManager inputManager;

	public TerrainModifyAppState() {
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.app = app;
		this.inputManager = app.getInputManager();

		if (app.getInputManager() != null) {

			inputManager.addMapping(INPUT_MAPPING_CAMERA_POS, new KeyTrigger(KeyInput.KEY_C));
			inputManager.addMapping(INPUT_MAPPING_MEMORY, new KeyTrigger(KeyInput.KEY_M));

			inputManager.addListener(keyListener,
					INPUT_MAPPING_CAMERA_POS,
					INPUT_MAPPING_MEMORY);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();

		if (inputManager.hasMapping(INPUT_MAPPING_CAMERA_POS))
			inputManager.deleteMapping(INPUT_MAPPING_CAMERA_POS);
		if (inputManager.hasMapping(INPUT_MAPPING_MEMORY))
			inputManager.deleteMapping(INPUT_MAPPING_MEMORY);

		inputManager.removeListener(keyListener);
	}

	private class TerrainModifyKeyListener implements ActionListener {

		public void onAction(String name, boolean value, float tpf) {
			if (!value) {
				return;
			}

			if (name.equals(INPUT_MAPPING_CAMERA_POS)) {
				Camera cam = app.getCamera();
				if (cam != null) {
					Vector3f loc = cam.getLocation();
					Quaternion rot = cam.getRotation();
					System.out.println("Camera Position: ("
							+ loc.x + ", " + loc.y + ", " + loc.z + ")");
					System.out.println("Camera Rotation: " + rot);
					System.out.println("Camera Direction: " + cam.getDirection());
				}
			} else if (name.equals(INPUT_MAPPING_MEMORY)) {
				BufferUtils.printCurrentDirectMemory(null);
			}
		}
	}
}
