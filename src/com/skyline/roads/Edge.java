package com.skyline.roads;

public class Edge {

	public int startPointId;
	public int endPointId;
	public SegmentType segmentType;
	public int leftCurbId;
	public int rightCurbId;

	public Edge(int startPointId, int endPointId, SegmentType segmentType) {
		this.startPointId = startPointId;
		this.endPointId = endPointId;
		this.segmentType = segmentType;
	}

	public Edge() {
		// TODO Auto-generated constructor stub
	}

	public boolean equals(Edge e) {
		return (startPointId == e.startPointId
				&& endPointId == e.endPointId)
				|| (startPointId == e.endPointId
				&& endPointId == e.startPointId);
	}
}
