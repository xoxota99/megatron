package com.skyline.application.tools.terrain;

import com.jme3.terrain.heightmap.*;
import com.skyline.application.events.*;
import com.skyline.application.i18n.*;
import com.skyline.application.tools.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

public class GlobalZeroTool extends GlobalTool {

	public GlobalZeroTool() {
		super(Messages.getString(GlobalZeroTool.class, "toolName"), Messages.getString(GlobalZeroTool.class, "toolTip"));
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void execute(WorldState worldState, int modifiers) {
		System.out.println("Zero out");
//		long seed = RoadEngine.currentTimeMillis();
//		worldState.setTerrainSeed(seed);
		worldState.setMaxTerrainHeight(WorldState.TERRAIN_MAX_INIT);
		AbstractHeightMap terrainHeightMap = Terrain.createHeightMap(worldState.getSize(), worldState.getWaterLevel()+1);
		worldState.setTerrainHeightMap(terrainHeightMap);
		
		TerrainEvent evt=new TerrainEvent(this);
		evt.setNewTerrain(true);
		worldState.triggerChangeEvent(evt);
	}

}
