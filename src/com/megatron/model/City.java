package com.megatron.model;

public class City {
	private int size = 256;
	private float waterLevel = 20;
	private long seed = 0L;// System.currentTimeMillis();
	private float[][] terrain;

	public float[][] getTerrain() {
		return terrain;
	}

	public void setTerrain(float[][] terrain) {
		this.terrain = terrain;
	}

	public float getWaterLevel() {
		return waterLevel;
	}

	public void setWaterLevel(float waterLevel) {
		this.waterLevel = waterLevel;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public int getWidth() {
		return (int) Math.sqrt(size);
	}
}
