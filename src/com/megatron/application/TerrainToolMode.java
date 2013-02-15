package com.megatron.application;

import com.megatron.application.tools.*;

public enum TerrainToolMode {
	SMOOTH("Smooth Terrain", CursorType.CONE), 
	RAISE_LOWER("Raise/Lower Terrain", CursorType.CONE), 
	NONE("(None)", CursorType.NONE);
	
	private String name;
	private CursorType cursorType;

	private TerrainToolMode(String name, CursorType cursorType) {
		this.name = name;
		this.cursorType = cursorType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CursorType getCursorType() {
		return cursorType;
	}

	public void setCursorType(CursorType cursorType) {
		this.cursorType = cursorType;
	}
}
