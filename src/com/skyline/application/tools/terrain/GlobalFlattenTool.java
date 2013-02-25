package com.skyline.application.tools.terrain;

import com.jme3.terrain.heightmap.*;
import com.skyline.application.events.*;
import com.skyline.application.i18n.*;
import com.skyline.application.tools.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

/**
 * "Flattens" the terrain by normalizing the height map to 90% of the current maximum height.
 * @author philippd
 *
 */
public class GlobalFlattenTool extends GlobalTool {

	public GlobalFlattenTool() {
		super(Messages.getString(GlobalFlattenTool.class, "toolName"), Messages.getString(GlobalFlattenTool.class, "toolTip"));
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void execute(WorldState worldState, int modifiers) {
		AbstractHeightMap terrainHeightMap = (AbstractHeightMap) worldState.getTerrainHeightMap();
		float value = worldState.getMaxTerrainHeight() * 0.9f;
		worldState.setMaxTerrainHeight(value);

        float currentMin, currentMax;
        float height;
        float[] heightData = terrainHeightMap.getHeightMap();
        int size = terrainHeightMap.getSize();

        currentMin = heightData[size+1];	//skips the terrain "skirt".
        currentMax = currentMin;

        //find the min/max values of the height fTemptemptempBuffer
        for (int i = 1; i < size-1; i++) {
            for (int j = 1; j < size-1; j++) {
                if (heightData[i + j * size] > currentMax) {
                    currentMax = heightData[i + j * size];
                } else if (heightData[i + j * size] < currentMin) {
                    currentMin = heightData[i + j * size];
                }
            }
        }

        //find the range of the altitude
        if (currentMax <= currentMin) {
            return;
        }
        
        height = currentMax - currentMin;

        //scale the values to a range of 0-255
        for (int i = 1; i < size-1; i++) {
            for (int j = 1; j < size-1; j++) {
//            	float newVal=((heightData[i + j * size] - currentMin) / height) * value;
//            	float oldVal = heightData[i + j * size];
//            	if(oldVal<newVal){
//            		RoadEngine.out.printf("oldVal=%f, newVal=%f\n",oldVal,newVal);
//            	}
                heightData[i + j * size] = ((heightData[i + j * size] - currentMin) / height) * value;
            }
        }

        
//		Terrain.trim(worldState);
		TerrainEvent evt=new TerrainEvent(this);

		worldState.triggerChangeEvent(evt);
	}

}
