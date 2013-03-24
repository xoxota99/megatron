package com.skyline.roads.condition;

import com.skyline.roads.*;

public class ZeroDelayCondition implements Condition {

	private boolean invert = false;
	private boolean lookOnlyAtRoads = false;

	public ZeroDelayCondition() {
	}

	public ZeroDelayCondition(boolean invert) {
		this.invert = invert;
	}

	@Override
	public boolean evaluate(int i, ModuleString mString) {
		if (invert) {
			// get module
			Module module = mString.get(i);
			// return condition value
			return (module.delay > 0) && ((!lookOnlyAtRoads) || module.offshootSegmentType == SegmentType.ROAD);
		} else {
			// get module
			Module module = mString.get(i);
			// return condition value
			return (module.delay == 0);
		}
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	public boolean isLookOnlyAtRoads() {
		return lookOnlyAtRoads;
	}

	public void setLookOnlyAtRoads(boolean lookOnlyAtRoads) {
		this.lookOnlyAtRoads = lookOnlyAtRoads;
	}

}
