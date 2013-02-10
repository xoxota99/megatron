package com.megatron.test.terrain.jme3;

import com.jme3.app.*;
import com.jme3.app.state.*;
import com.jme3.scene.*;

public class TerrainModeAppState extends AbstractAppState {
	 private SimpleApplication app;
	 
	    private Node x = new Node("x");  // some custom class fields...    
	    public Node getX(){ return x; }  // some custom methods... 
	 
	    @Override
	    public void initialize(AppStateManager stateManager, Application app) {
	      super.initialize(stateManager, app); 
	      this.app = (SimpleApplication)app;          // cast to a more specific class
	 
	      // init stuff that is independent of whether state is PAUSED or RUNNING
	      this.app.getRootNode().attachChild(getX()); // modify scene graph...
//	      this.app.doSomething();                     // call custom methods...
	   }
	 
	   @Override
	    public void cleanup() {
	      super.cleanup();
	      // unregister all my listeners, detach all my nodes, etc...
	      this.app.getRootNode().detachChild(getX()); // modify scene graph...
//	      this.app.doSomethingElse();                 // call custom methods...
	    }
	 
	    @Override
	    public void setEnabled(boolean enabled) {
	      // Pause and unpause
	      super.setEnabled(enabled);
	      if(enabled){
	        // init stuff that is in use while this state is RUNNING
	        this.app.getRootNode().attachChild(getX()); // modify scene graph...
//	        this.app.doSomethingElse();                 // call custom methods...
	      } else {
	        // take away everything not needed while this state is PAUSED
	      }
	    }
	 
	    // Note that update is only called while the state is both attached and enabled.
	    @Override
	    public void update(float tpf) {
	      // do the following while game is RUNNING
	      this.app.getRootNode().getChild("blah").scale(tpf); // modify scene graph...
//	      x.setUserData(...);                                 // call some methods...
	    }
	 
}
