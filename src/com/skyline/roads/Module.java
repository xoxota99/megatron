package com.skyline.roads;

import javax.vecmath.*;

public class Module implements Comparable<Module> {
	public char id;
	public int delay = 0;// paramIndex 0
	public int originVertexId = 0;// param index 1
	public SegmentType mainSegmentType = SegmentType.ROAD;// param index 2
	public ModuleStatus status = ModuleStatus.UNASSIGNED;// param index 3
	public SegmentType offshootSegmentType = SegmentType.ROAD;// param index 4 (for
														// Branch Modules)
	public Vector2d direction = new Vector2d(); // formerly *also* param 2 and 3?
	public ModuleType moduleType = ModuleType.ROAD;

	public Module(char id, ModuleType moduleType) {
		this.id = id;
		this.moduleType = moduleType;
	}

	public Module(Module m) {
		this.id = m.id;
		this.delay = m.delay;
		this.direction.set(m.direction);
		this.originVertexId = m.originVertexId;
		this.mainSegmentType = m.mainSegmentType;
		this.offshootSegmentType = m.offshootSegmentType;
		this.status = m.status;
	}

	@Override
	public int compareTo(Module m) {
		return this.id - m.id;
	}

	public boolean equals(Module m) {
		return this.id == m.id;
	}
}
