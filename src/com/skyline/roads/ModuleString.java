package com.skyline.roads;

import java.util.*;

public class ModuleString extends ArrayList<Module> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -797778963417011813L;

	public ModuleString(ModuleString successor) {
		super(successor);
	}

	public ModuleString() {
		super();
	}

	public ModuleString(int a, int b, ModuleString mString) {
		// check if start index smaller than stop index and swap if not
		if (a > b) {
			int t = a;
			a = b;
			b = t;
		}

		for (int c = a; c < b; c++) {
			// add each wanted module to our module string
			add(mString.get(c));
		}
	}

	public boolean equals(ModuleString ms) {
		if (this.size() == ms.size()) {
			for (int i = 0; i < size(); ++i) {
				if (!get(i).equals(ms.get(i))) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Starting at the specified index, replace Modules in this string with the
	 * Modules from another string.
	 * 
	 * @param index
	 * @param successor
	 */
	public void replace(int index, ModuleString successor) {
		super.remove(index);
		addAll(index,successor);
	}
}
