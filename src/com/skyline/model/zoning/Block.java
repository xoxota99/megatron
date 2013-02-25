package com.skyline.model.zoning;

import java.util.List;

import com.skyline.geometry.*;
import com.skyline.transportation.model.*;

public class Block extends Polygon {
	private List<Lot> lots;
	private List<RoadSegment> roadSegments;
}
