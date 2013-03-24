package com.skyline.roads.condition;

import com.skyline.roads.*;


/**
 * An L-System Condition. Evaluates the Module at the specified index in the
 * specified Module String, and returns true if the condition is met, otherwise
 * false.
 * 
 * @author philippd
 * 
 */
public interface Condition {

	/**
	 * Evaluates the Module at the specified index in the specified Module
	 * String, and returns true if the condition is met, otherwise false.
	 * 
	 * @param i
	 * @param mString
	 * @return true if the condition is met, otherwise false;
	 */
	public boolean evaluate(int i, ModuleString mString);
}
