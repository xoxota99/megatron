package com.skyline.transportation.model;

public enum RoadType {
	STREET(1, 10),
	HIGHWAY(2, 50);

	private int width;			//width of the road (in relative units)
	private int segmentLength;	//length of individual segments of this type of road (in relative units)

	RoadType(int size, int segmentLength) {
		this.width = size;
		this.segmentLength = segmentLength;
	}

	public int getWidth() {
		return width;
	}

	public int getSegmentLength() {
		return segmentLength;
	}
}
