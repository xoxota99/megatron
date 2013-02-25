package com.skyline.transportation.model;

import java.util.*;

public class Road {

	public enum RoadType {
		STREET(1),
		HIGHWAY(5);
		
		private int size;

		RoadType(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}
	}
	LinkedList<ControlPoint> nodes = new LinkedList<ControlPoint>();
	
}
