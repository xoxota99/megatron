package com.skyline.model;

import java.util.*;

import com.jme3.terrain.heightmap.*;
import com.skyline.application.events.*;

/**
 * WorldState holds (what else?) the state of the world. Tools and toolsets will
 * modify the WorldState, and the application will display the worldstate.
 * 
 * The application observes the worldState, and so can be notified of "Events"
 * that occur within the world.
 * 
 * @author philippd
 * 
 */
public class WorldState extends Observable {

	public static final float TERRAIN_MIN = -50f; // terrain can never be lower
													// than this.
	public static final float TERRAIN_MAX = 255f; // terrain can never be higher
													// than this.
	public static final float TERRAIN_MAX_INIT = 64f; // Initial maxHeight on
														// the map.

	private HeightMap terrainHeightMap;
	// private HeightMap originalHeightMap; // keep this, for "resetting".
	private float maxTerrainHeight = TERRAIN_MAX_INIT; // height of the current
														// highest point on the
														// map.
	// private int inputModifiers = 0; // SHIFT, CTRL, or ALT;
	private int size = 256;

	/*
	 * Seeds for various random processes.
	 */
	private long terrainSeed = 0L;
	private long populationSeed = 0L;
	private long roadSeed = 0L;
	private long zoningSeed = 0L;
	private long constructionSeed = 0L;
	private float waterLevel = 0f;
	private float[][] popDensity = new float[size][size];
	private float maxPop = 0f;

	/**
	 * Notify observers that something has changed.
	 * 
	 * @param evt
	 */
	public void triggerChangeEvent(Event evt) {
		this.setChanged();
		this.notifyObservers(evt);
	}

	public WorldState() {
		this(System.currentTimeMillis());
	}

	public WorldState(long seed) {
		this.terrainSeed = seed;
	}

	public HeightMap getTerrainHeightMap() {
		return terrainHeightMap;
	}

	/**
	 * Note: Setting the terrainHeightMap resets the population Density map.
	 * 
	 * @param terrainHeightMap
	 */
	public void setTerrainHeightMap(final HeightMap terrainHeightMap) {
		this.terrainHeightMap = terrainHeightMap;
		popDensity = new float[terrainHeightMap.getSize()][terrainHeightMap.getSize()];
	}

	public long getTerrainSeed() {
		return terrainSeed;
	}

	public void setTerrainSeed(long heightMapSeed) {
		this.terrainSeed = heightMapSeed;
	}

	public int getSize() {
		return size;
	}

	// public HeightMap getOriginalHeightMap() {
	// return originalHeightMap;
	// }

	public float getMaxTerrainHeight() {
		return maxTerrainHeight;
	}

	public void setMaxTerrainHeight(float maxTerrainHeight) {
		this.maxTerrainHeight = maxTerrainHeight;
	}

	public long getPopulationSeed() {
		return populationSeed;
	}

	public void setPopulationSeed(long populationSeed) {
		this.populationSeed = populationSeed;
	}

	public long getRoadSeed() {
		return roadSeed;
	}

	public void setRoadSeed(long roadSeed) {
		this.roadSeed = roadSeed;
	}

	public long getZoningSeed() {
		return zoningSeed;
	}

	public void setZoningSeed(long zoningSeed) {
		this.zoningSeed = zoningSeed;
	}

	public long getConstructionSeed() {
		return constructionSeed;
	}

	public void setConstructionSeed(long constructionSeed) {
		this.constructionSeed = constructionSeed;
	}

	public float getWaterLevel() {
		return waterLevel;
	}

	public void setWaterLevel(float waterLevel) {
		this.waterLevel = waterLevel; //TODO: Zero out pop density in places that are below water.
	}

	public float[][] getPopDensity() {
		// TODO Auto-generated method stub
		return popDensity;
	}

	public void setPopDensity(float[][] popDensity) {
		this.popDensity = popDensity;
		//TODO: Zero out pop density in places that are below water.
		updateMaxPop();
	}

	private void updateMaxPop() {
		this.maxPop = 0f;
		for (int i = 0; i < popDensity.length; i++) {
			for (int j = 0; j < popDensity[i].length; j++) {
				if (popDensity[i][j] > this.maxPop) {
					this.maxPop = popDensity[i][j];
				}
			}
		}
	}

	public float getMaxPop() {
		return maxPop;
	}
}
