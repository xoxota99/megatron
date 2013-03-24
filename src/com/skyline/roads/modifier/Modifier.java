package com.skyline.roads.modifier;

import com.skyline.roads.*;


/**
 * L-System parameter modifier.
 * 
 * @author philippd
 * 
 */
public interface Modifier {
	/**
	 * Examine the predecessor ModuleString, extract the specified module, and
	 * modify the successor string to conform to some condition.
	 * 
	 * @param i
	 *            the index of the predecessor
	 * @param pred
	 *            the predecessor string
	 * @param succ
	 *            the successor string
	 */
	public void modify(int i, ModuleString pred, ModuleString succ);
}
