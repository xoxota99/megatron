package com.megatron.terrain;

import com.jme3.terrain.heightmap.*;
import com.megatron.model.*;

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
		if (worldState != null) {
			System.out.println("TrimTerrain(worldState)");
			HeightMap heightMap = worldState.getTerrainHeightMap();
		}

	}

	protected static void trim(AbstractHeightMap heightMap) {
		// drop the edges.
		if (heightMap != null) {
			int size = heightMap.getSize();
			for (int i = 0; i < size; i++) {
				heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, i, 0);
				heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, i, size - 1);
				heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, 0, i);
				heightMap.setHeightAtPoint(WorldState.TERRAIN_MIN, size - 1, i);
			}
		}
	}

	/**
	 * Create a "flat" heightMap, of the specified size initialized to the
	 * specified height.
	 * 
	 * @param newSize
	 *            Size of one side of the heightMap. Must be a power of two.
	 * @param initialHeight
	 *            initial height of each cell in the heightMap.
	 * @return
	 */
	public static AbstractHeightMap createHeightMap(final int newSize, final float initialHeight) {
		assert newSize > 0 && (newSize & (newSize - 1)) == 0 : "newSize must be a power of two!";

		AbstractHeightMap terrainHeightMap = new AbstractHeightMap() {
			@Override
			public boolean load() {
				this.size = newSize + 1;
				this.heightData = new float[(newSize + 1) * (newSize + 1)];
				for (int i = 0; i < heightData.length; i++) {
					heightData[i] = initialHeight;
				}
				return true;
			}
		};
		terrainHeightMap.load();
		trim(terrainHeightMap);
		return terrainHeightMap;
	}

	public static AbstractHeightMap createRandomHeightMap(int newSize, float maxTerrainHeight, long seed) {
		assert newSize > 0 && (newSize & (newSize - 1)) == 0 : "newSize must be a power of two!";
		AbstractHeightMap terrainHeightMap;
		try {
			terrainHeightMap = new MidpointDisplacementHeightMap(newSize + 1, 1, .5f, seed);
			terrainHeightMap.normalizeTerrain(maxTerrainHeight);
		} catch (Exception x) {
			x.printStackTrace();
			terrainHeightMap = createHeightMap(newSize, 1f);
		}

		trim(terrainHeightMap);
		return terrainHeightMap;
	}
}
