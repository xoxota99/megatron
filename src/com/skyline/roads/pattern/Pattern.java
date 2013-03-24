package com.skyline.roads.pattern;

import com.skyline.roads.*;

/**
 * Adapts Branch modules to a particular street pattern.
 * 
 * @author philippd
 * 
 */
public interface Pattern {
	public void adapt(int i, ModuleString pred, ModuleString succ);
}
