package com.skyline.roads.modifier;

import com.skyline.roads.*;
/**
 * L-System parameter modifier. Does nothing.
 * 
 * @param i
 *            index
 * @param pred
 *            predecessor Module String
 * @param succ
 *            successor Module String
 */
public class ZeroModifier implements Modifier{
	public void modify(int i, ModuleString pred, ModuleString succ) {
		// Do nothing.
	}
}
