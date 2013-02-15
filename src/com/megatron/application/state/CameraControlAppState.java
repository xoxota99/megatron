package com.megatron.application.state;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;

/**
 * Disables some of the "stock" FlyCam controls, in favor of app-specific ones.
 * - Right click and drag to rotate camera.
 * @author philippd
 *
 */
public class CameraControlAppState  extends AbstractAppState implements ActionListener {

	SimpleApplication app;
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		this.app=(SimpleApplication)app;
		super.initialize(stateManager, app);
		
		this.app.getFlyByCamera().setDragToRotate(true);
        app.getInputManager().deleteMapping("FLYCAM_RotateDrag");
        app.getInputManager().addMapping("FLYCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addListener(this.app.getFlyByCamera(), "FLYCAM_RotateDrag");//re-attach.
	}
	@Override
	public void cleanup() {
		super.cleanup();
	}

	// Note that update is only called while the state is both attached and
	// enabled.
	@Override
	public void update(float tpf) {
		// Where is the cursor right now?
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		// TODO Auto-generated method stub
		
	}


}
