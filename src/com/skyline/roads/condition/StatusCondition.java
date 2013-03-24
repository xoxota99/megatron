package com.skyline.roads.condition;

import com.skyline.roads.*;

/**
 * L-System condition. Checks if a road/street segment has a particular
 * {@link ModuleStatus}.
 * 
 */
public class StatusCondition implements Condition {
	private ModuleStatus status = ModuleStatus.ACCEPT;

	public StatusCondition(ModuleStatus status) {
		this.status = status;
	}

	public boolean evaluate(int i, ModuleString ms) {
		// return condition value
		return ms.get(i).status == this.status;
	}

}
