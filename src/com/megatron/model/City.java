package com.megatron.model;

import com.oddlabs.procedurality.Channel;


public class City {
	private int size = 1024;
	private Channel terrain;
	private long seed = 0L; // System.getCurrentTimeMillis();

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public Channel getTerrain() {
		return terrain;
	}

	public void setTerrain(Channel terrain) {
		this.terrain = terrain;
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
