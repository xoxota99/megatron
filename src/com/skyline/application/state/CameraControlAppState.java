package com.skyline.application.state;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;

/**
 * Disables some of the "stock" FlyCam controls, in favor of app-specific ones.
 * - Right click and drag to rotate camera.
 * 
 * @author philippd
 * 
 */
public class CameraControlAppState extends AbstractAppState implements ActionListener {

	private static final String CAM_ROT_LEFT=CameraControlAppState.class.getName()+".CAM_ROT_LEFT";
	private static final String CAM_ROT_RIGHT=CameraControlAppState.class.getName()+".CAM_ROT_RIGHT";
	private static final String CAM_ROT_UP=CameraControlAppState.class.getName()+".CAM_ROT_UP";
	private static final String CAM_ROT_DOWN=CameraControlAppState.class.getName()+".CAM_ROT_DOWN";
	
	SimpleApplication app;

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		this.app = (SimpleApplication) app;
		super.initialize(stateManager, app);

		this.app.getFlyByCamera().setDragToRotate(true);
		app.getInputManager().deleteMapping("FLYCAM_RotateDrag");
		app.getInputManager().addMapping("FLYCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		app.getInputManager().addListener(this.app.getFlyByCamera(), "FLYCAM_RotateDrag");// re-attach.

		app.getInputManager().addMapping(CAM_ROT_LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
		app.getInputManager().addMapping(CAM_ROT_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
		app.getInputManager().addMapping(CAM_ROT_UP, new KeyTrigger(KeyInput.KEY_UP));
		app.getInputManager().addMapping(CAM_ROT_DOWN, new KeyTrigger(KeyInput.KEY_DOWN));

		app.getInputManager().addListener(this, CAM_ROT_LEFT);// re-attach.
		app.getInputManager().addListener(this, CAM_ROT_RIGHT);// re-attach.
		app.getInputManager().addListener(this, CAM_ROT_UP);// re-attach.
		app.getInputManager().addListener(this, CAM_ROT_DOWN);// re-attach.

	}

	@Override
	public void cleanup() {
		super.cleanup();
	}

	// Note that update is only called while the state is both attached and
	// enabled.
	@Override
	public void update(float tpf) {

	}

	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (value) {
			if (name.equals(CAM_ROT_LEFT)) {
				rotateCamera(FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
			} else if (name.equals(CAM_ROT_RIGHT)) {
				rotateCamera(-FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
			} else if (name.equals(CAM_ROT_UP)) {
				rotateCamera(-FastMath.DEG_TO_RAD, this.app.getCamera().getLeft());
			} else if (name.equals(CAM_ROT_DOWN)) {
				rotateCamera(FastMath.DEG_TO_RAD, this.app.getCamera().getLeft());
			}
		}
	}

	// Copied from FlyByCam, since it's not accessible there.
	protected void rotateCamera(float value, Vector3f axis) {
		Camera cam = this.app.getCamera();
		Matrix3f mat = new Matrix3f();
		mat.fromAngleNormalAxis(this.app.getFlyByCamera().getRotationSpeed() * value, axis);

		Vector3f up = cam.getUp();
		Vector3f left = cam.getLeft();
		Vector3f dir = cam.getDirection();

		mat.mult(up, up);
		mat.mult(left, left);
		mat.mult(dir, dir);

		Quaternion q = new Quaternion();
		q.fromAxes(left, up, dir);
		q.normalizeLocal();

		cam.setAxes(q);
	}

}
