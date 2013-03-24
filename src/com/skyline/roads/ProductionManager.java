package com.skyline.roads;

import java.util.*;

public class ProductionManager extends ArrayList<Production> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -712249988522379099L;

	public boolean apply(ModuleString mString) {
		// set result to false
		boolean altered = false;
		// make copy of work string
		ModuleString mPredString = new ModuleString(mString);
		// for each module in string (from back)...
		for (int index = mString.size(); index >= 0; index--) {
			// for each production
			Iterator<Production> it = super.iterator();
			while (it.hasNext()) {
				Production prod = it.next();
				// apply production at index
				altered = prod.applyAtIndex(index, mPredString, mString);
				// if altered we have to break
				if (altered)
					break;
			}
		}
		// return result
		return altered;
	}
}
