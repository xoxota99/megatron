package com.skyline.roads;

import com.skyline.roads.condition.*;
import com.skyline.roads.modifier.*;

public class Production {

	private Module predecessor;
	private ModuleString successor;
	private ModuleString leftContext;
	private ModuleString rightContext;
	private Condition condition;
	private Modifier modifier;

	private static final Condition zeroCondition = new Condition() {
		@Override
		public boolean evaluate(int i, ModuleString mString) {
			return true;
		}
	};

	private static final Modifier zeroModifier = new Modifier() {
		@Override
		public void modify(int i, ModuleString pred, ModuleString succ) {
			// Do nothing.
		}
	};

	/**
	 * Except for the condition and the modifier the created object will work on
	 * its own copies of the given parameters. If you want to use only part of
	 * the features provide null pointers for unneeded parameters. Predecessor
	 * and successor are minimum requirements.
	 * 
	 * @param predecessor
	 *            Module which represents the predecessor, the left side of a
	 *            production.
	 * @param successor
	 *            Module string which represents the successor, the right side
	 *            of a production.
	 * @param condition
	 *            Optional. A {@link Condition} instance indicating whether the
	 *            production will be executed, or null to always execute.
	 * @param modifier
	 *            Optional. A {@Modifier} instance which modifies the
	 *            parameters of a derived module string, or null to use the
	 *            parameters unmodified.
	 * @param leftContext
	 *            Optional. Left context that must be met for the production to
	 *            be actually executed, or null to ignore left context.
	 * @param rightContext
	 *            Optional. Right context that must be met for the production to
	 *            be actually executed, or null to ignore right context.
	 */
	public Production(Module predecessor, ModuleString successor, Condition condition, Modifier modifier, ModuleString leftContext, ModuleString rightContext) {
		// make local copies for predecessor and successor
		this.predecessor = new Module(predecessor);
		this.successor = new ModuleString(successor);
		// now the optional parameters
		// if given pointer equals null create new object
		// else make a copy
		// left context
		if (leftContext == null)
			this.leftContext = new ModuleString();
		else
			this.leftContext = new ModuleString(leftContext);
		// right context
		if (rightContext == null)
			this.rightContext = new ModuleString();
		else
			this.rightContext = new ModuleString(rightContext);
		// set function pointers
		// condition evaluator
		if (condition == null)
			this.condition = zeroCondition;
		else
			this.condition = condition;
		// parameter modifier
		if (modifier == null)
			this.modifier = zeroModifier;
		else
			this.modifier = modifier;
	}

	public Production(Production p) {
		this.condition = p.condition;
		this.leftContext = p.leftContext;
		this.modifier = p.modifier;
		this.predecessor = p.predecessor;
		this.rightContext = p.rightContext;
		this.successor = p.successor;
	}

	/**
	 * Do not call this method directly. It's used by the production manager.
	 **/
	/*
	 * Applies production at a given index of a module string. If the production
	 * can be applied (that is the module at the given index equals the left
	 * side of the production, both contexts and the condition is met) the
	 * actual replacement with the right side of the production will be carried
	 * out on the successor module string (can initially be treated as a copy of
	 * the predecessor module string). The method returns true if the production
	 * was sucessfully applied, else false. However the user should not call
	 * this method directly. It is called by the production manager.
	 * 
	 * @param i the index to the module to apply.
	 * 
	 * @param predecessor predecessor
	 * 
	 * @param successor successor
	 * 
	 * @return true if production was successfully applied, else false
	 */
	public boolean applyAtIndex(int index, ModuleString predecessor, ModuleString successor) {
		// if module at index equals left side of production...
		if (predecessor.get(index).equals(this.predecessor)) {
			// check if both contexts are true
			boolean lCon = checkLContext(index, predecessor);
			boolean rCon = checkRContext(index, predecessor);
			// check if condition is true
			boolean cond = this.condition.evaluate(index, predecessor);
			// if condition and both contexts are true...
			if (lCon && rCon && cond) {
				// replace the module at the given index with a copy
				// of the right side in the successor string
				successor.replace(index, new ModuleString(this.successor));
				// alter parameters
				this.modifier.modify(index, predecessor, successor);
				return true;
			}
		}
		return false;
	}

	/**
	 * Left context checker.
	 * 
	 * @param index
	 * @param mString
	 * @return true if context is ok, otherwise false.
	 */
	private boolean checkLContext(int index, ModuleString mString) {
		
		// get length of context
		int length = leftContext.size();
		// set result: true if length is zero, else false
		boolean result = (length == 0);
		// check if length of context is greater than zero and length
		// is less or equal than index (does the context actually fit?)
		if ((length > 0) && (length <= index)) {
			// make a copy of the sub string that should contain the context
			ModuleString temp = new ModuleString(index - length, index - 1, mString);
			// check if context is met
			result = (temp.equals(leftContext));
		}
		return result;
	}

	/**
	 * Right context checker.
	 * 
	 * @param index
	 * @return
	 */
	private boolean checkRContext(int index, ModuleString mString) {
		// get length of context
		int length = rightContext.size();
		// set result: true if length is zero, else false
		boolean result = (length == 0);
		// check if length of context is greater than zero and length + index
		// is less than the length of the module stirng (does the context
		// actually fit?)
		if ((length > 0) && (index + length < mString.size())) {
			// make a copy of the sub string that should contain the context
			ModuleString temp = new ModuleString(index + 1, index + length, mString);
			// check if context is met
			result = (temp.equals(rightContext));
		}
		return result;
	}

}
