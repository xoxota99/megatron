package com.skyline.application.tools.terrain;

import com.jme3.terrain.heightmap.*;
import com.skyline.application.events.*;
import com.skyline.application.i18n.*;
import com.skyline.application.tools.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

public class GlobalRandomizationTool extends GlobalTool {

	public GlobalRandomizationTool() {
		super(Messages.getString(GlobalRandomizationTool.class, "toolName"), Messages.getString(GlobalRandomizationTool.class, "toolTip"));
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void execute(WorldState worldState, int modifiers) {
		long seed = System.currentTimeMillis();
		worldState.setTerrainSeed(seed);
		worldState.setMaxTerrainHeight(WorldState.TERRAIN_MAX_INIT);
		AbstractHeightMap terrainHeightMap = Terrain.createRandomHeightMap(worldState.getSize(), worldState.getMaxTerrainHeight(), seed);
		worldState.setTerrainHeightMap(terrainHeightMap);
		
		TerrainEvent evt=new TerrainEvent(this);
		evt.setNewTerrain(true);
		worldState.triggerChangeEvent(evt);
	}

}
