package com.megatron.application.tools;

import com.jme3.terrain.heightmap.*;
import com.megatron.application.*;

public final class Terrain {
	private Terrain() {
	}

	/**
	 * The "edges" of any HeightMap are set to TERRAIN_MIN, in order to create
	 * "walls" on the terrain (visually).
	 * 
	 * @param heightMap
	 */
	public static void trim(WorldState worldState) {
		HeightMap heightMap = worldState.getTerrainHeightMap();
		// drop the edges.
		for (int x = 0; x < worldState.getSize() + 1; x++) {
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, x, 0);
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, x, worldState.getSize());
		}
		for (int z = 0; z < worldState.getSize() + 1; z++) {
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, 0, z);
			heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, worldState.getSize(), z);
		}

	}
}
