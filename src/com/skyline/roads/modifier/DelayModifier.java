package com.skyline.roads.modifier;

import com.skyline.roads.*;

/**
 * L-System parameter modifier. Increments delay of a branch module by a
 * provided delta (-1 by default).
 **/
public class DelayModifier implements Modifier {

	private int delta = -1;

	public DelayModifier() {
	}

	public DelayModifier(int delta) {
		this.delta = delta;
	}

	@Override
	public void modify(int i, ModuleString pred, ModuleString succ) {
		// get predecessor module
		Module pModule = pred.get(i);
		// get successor modules
		Module sModule = succ.get(i);
		// set parameters of successors
		sModule.delay = pModule.delay + this.delta;
		sModule.originVertexId = pModule.originVertexId;
		sModule.mainSegmentType = pModule.mainSegmentType;
		sModule.status = pModule.status;
		sModule.offshootSegmentType = pModule.offshootSegmentType;
	}
}
