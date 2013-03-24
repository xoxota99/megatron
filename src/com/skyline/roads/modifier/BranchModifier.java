package com.skyline.roads.modifier;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roads.*;
import com.skyline.roads.util.*;

/**
 * L-System parameter modifier. Converts branch module to a road/street and adds
 * a segment to the road map. Several things have to be done and checked here:
 * <ul>
 * <li>Parameter adaption</li>
 * <li>Intersection with other edges</li>
 * <li>Merge with nearby vertices</li>
 * </ul>
 * 
 * @author philippd
 * 
 */
public class BranchModifier implements Modifier {
	private RoadMap roadMap;
	private ValueMap2d<Double> heightMap;
	private ValueMap2d<Byte> waterMap;
	private ValueMap2d<Double> densityMap;
	private ValueMap2d<Byte> blockedMap;

	public BranchModifier(RoadMap roadMap, ValueMap2d<Double> heightMap, ValueMap2d<Double> densityMap, ValueMap2d<Byte> waterMap, ValueMap2d<Byte> blockedMap) {
		this.roadMap = roadMap;
		this.heightMap = heightMap;
		this.waterMap = waterMap;
		this.densityMap = densityMap;
		this.blockedMap=blockedMap;
	}

	public void modify(int i, ModuleString pred, ModuleString succ) {

		// get predecessor module
		Module pModule = pred.get(i);
		// get successor modules
		Module sModule = succ.get(i);
		// some variables
		int originIndex = pModule.originVertexId;
		SegmentType segType = pModule.offshootSegmentType;
		// set default value
		sModule.originVertexId = originIndex;
		sModule.mainSegmentType = segType;
		sModule.status = ModuleStatus.DELETE;
		// some variables
		int vertexIndex = -1;
		int edgeIndex = -1;
		// discard segmnet if origin vertex has already maximum number of
		// edges
		// attached
		if (roadMap.getAttachedEdgeCount(originIndex) >= Config.ROAD_MAX_BRANCHES)
			return;
		// discard segment if sector is already occupied
		if (sectorOccupied(originIndex, pModule.direction))
			return;
		// calculate position of end vertex
		Vertex v = roadMap.getVertex(originIndex);
		Point2d temp = VecMath.calculatePoint(Config.ROAD_SEGMENT_LENGTH, v.x, v.y, pModule.direction.x, pModule.direction.y);

		// set the height
		Point3d endPos = new Point3d(temp.x, temp.y, heightMap.getInterpolatedValue(temp.x, temp.y) * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));

		// adjust parameters until segment is in legal area
		// if unsuccessful exit
		boolean lookAtDensity = ((segType == SegmentType.STREET) || Config.ROAD_DENSITY_SENSITIVE);
		if (!adjustSegment(roadMap.getVertex(originIndex), pModule.direction, endPos, lookAtDensity))
			return;

		// look if segment can be attached to a nearby vertex
		// double endPosV[] = { endPos[0], endPos[1], endPos[2] };
		Point3d endPosV = new Point3d(endPos);
		// #BYREF
		vertexIndex = attachToNearbyVertex(endPosV);

		// look if segment intersects with existing edges
		// double endPosE[] = { endPos[0], endPos[1], endPos[2] };
		Point3d endPosE = new Point3d(endPos);
		if (vertexIndex != -1) {
			endPosE.set(endPosV);
		}
		// #BYREF
		edgeIndex = intersectWithOtherEdges(originIndex, pModule.direction, endPosE);

		// adjust roadMap
		if (vertexIndex == -1) {
			if (edgeIndex == -1) {
				// no nearby vertex or intersection
				// simply add a new vertex and a edge
				addVertexAndEdge(originIndex, endPos, segType, sModule);
			}
			else {
				// no nearby vertex, but an intersection
				// add an intersection
				addIntersection(originIndex, edgeIndex, endPosE, segType, sModule);
			}
		}
		else {
			if (edgeIndex == -1) {
				// nearby vertex, but no intersection
				// add edge to nearby vertex
				addEdge(originIndex, vertexIndex, segType, sModule);
			}
			else {
				// nearby vertex and an intersection
				double distanceV = roadMap.getVertex(originIndex).distance(endPosV);
				double distanceE = roadMap.getVertex(originIndex).distance(endPosE);
				if (distanceE <= distanceV) {
					// add edge to nearby vertex
					addEdge(originIndex, vertexIndex, segType, sModule);
				}
				else {
					// add an intersection
					addIntersection(originIndex, edgeIndex, endPosE, segType, sModule);
				}
			}
		}
	}

	/**
	 * Looks if a road/street segment is attached to a vertex in the given
	 * direction
	 * 
	 * @param originIndex
	 *            the origin vertex of the segment
	 * @param dir
	 *            direction
	 * @return
	 */
	private boolean sectorOccupied(int originIndex, Vector2d dir) {
		List<Integer> attachedEdges = roadMap.getAttachedEdges(originIndex);
		for (int idx : attachedEdges) {
			Edge edge = roadMap.getEdge(idx);
			double posX, posY;
			if (originIndex == edge.startPointId) {
				posX = roadMap.getVertex(edge.endPointId).x;
				posY = roadMap.getVertex(edge.endPointId).y;
			} else {
				posX = roadMap.getVertex(edge.startPointId).x;
				posY = roadMap.getVertex(edge.startPointId).y;
			}

			Vector2d vec = new Vector2d(posX - roadMap.getVertex(originIndex).x, posY - roadMap.getVertex(originIndex).y);
			vec.normalize();

			if (vec.dot(dir) > 0.7) { // magic.
				return true;
			}
		}
		return false;
	}

	/**
	 * Adapts road/street segment's direction and end point to fullfill all
	 * parameters. That is:
	 * <ul>
	 * <li>End point is in bounds of generated city.</li>
	 * <li>End point is in populated area with some exceptions.</li>
	 * <li>End point is on land.</li>
	 * <li>Road/street segment's gradient is not to steep.</li>
	 * <li>End point is not in a blocked area.</li>
	 * </ul>
	 * 
	 * @param start
	 *            start Point of the segment
	 * @param dir
	 *            direction of the segment. this will be modified in place.
	 * @param end
	 *            segment endPoint. This will be modified in place.
	 * @param lookAtDensity
	 *            Should we consider Population Density?
	 * @return
	 */
	private boolean adjustSegment(Point3d start, Vector2d dir, Point3d end, boolean lookAtDensity) {
		// some variables
		double baseAng = VecMath.angle(dir.x, dir.y);
		double curSegShorten = 1.0;
		double curSegAng = baseAng;
		int shStep = 0;
		int anStep = 0;
		double deltaShorten = (1.0 - Config.ROAD_MAX_SHORTENING) / (double) Config.ROAD_SAMPLING_RATE;
		double deltaAngle = Config.ROAD_SAMPLING_RATE / (double) Config.ROAD_SAMPLING_RATE;
		// temporary variables
		// conditions if segment is legal
		boolean inBounds = (end.x >= 0.0) && (end.x <= Config.RESOLUTION_X) && (end.y >= 0.0) && (end.y <= Config.RESOLUTION_Y); // end
		// point
		// is
		// in
		// bounds
		// #BYREF
		boolean populated = (densityMap.getInterpolatedValue(end.x, end.y) > 0.0); // end
		// point
		// is
		// in
		// populated
		// area
		boolean onLand = (waterMap.getRealValue(end.x, end.y) < 255); // end
																		// point
																		// is
		// on land.

		boolean notSteep = (VecMath.slope(start.x, start.y, start.z, end.x, end.y, end.z) <= Config.ROAD_MAX_GRADIENT); // segment
		// not
		// too
		// steep
		boolean notBlocked = (blockedMap.getRealValue(end.x, end.y) < 255); // not
		// blocked
		boolean isLegal = inBounds && ((!lookAtDensity) || (populated)) && onLand && notSteep && notBlocked;
		// while segment is not legal
		while (!isLegal) {
			// adjust shStep and anStep
			if (anStep > Config.ROAD_SAMPLING_RATE) {
				return false;
			}

			shStep = adjustShortStep(shStep);
			anStep = adjustAngleStep(anStep);
			// calculate shortening and angle
			curSegShorten = 1.0 - shStep * deltaShorten;

			curSegAng = baseAng + anStep * deltaAngle;
			// recalculate direction vector
			// #BYREF
			Vector2d vec = VecMath.rotateNormalizedVector(curSegAng);
			dir.set(vec);

			// recalculate end point
			Point2d temp = VecMath.calculatePoint(Config.ROAD_SEGMENT_LENGTH * curSegShorten, start.x, start.y, vec.x, vec.y);
			// set the height.
			end.set(temp.x, temp.y, heightMap.getInterpolatedValue(end.x, end.y));
			// #BYREF
			end.z = (float) (end.z * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));
			// recalculate condition s
			inBounds = (end.x >= 0.0) && (end.x <= Config.RESOLUTION_X) && (end.y >= 0.0) && (end.y <= Config.RESOLUTION_Y); // end
			// point
			// is
			// in
			// bounds
			populated = (densityMap.getInterpolatedValue(end.x, end.y) > 0.0); // end
			// point
			// is
			// in a
			// populated
			// area
			onLand = (waterMap.getRealValue(end.x, end.y) < 255); // end point
																	// is on
			// land
			notSteep = (VecMath.slope(start.x, start.y, start.z, end.x, end.y, end.z) <= Config.ROAD_MAX_GRADIENT); // segment
			// not
			// too
			// steep
			notBlocked = (blockedMap.getRealValue(end.x, end.y) < 255); // not
																		// blocked
			isLegal = inBounds && ((!lookAtDensity) || (populated)) && onLand && notSteep && notBlocked;
		}
		return true;
	}

	private int adjustShortStep(int shStep) {
		return shStep < Config.ROAD_SAMPLING_RATE ? ++shStep : 0;
	}

	private int adjustAngleStep(int angleStep) {
		if (angleStep < Config.ROAD_SAMPLING_RATE) {
			if (angleStep < 0) {
				angleStep = 1;
			} else if (angleStep == 0) {
				angleStep = -angleStep + 1;
			} else if (angleStep > 0) {
				angleStep = -angleStep;
			}
		}
		return angleStep;
	}

	/**
	 * Adds an intersection to the road map. Used if a new road segment
	 * intersects an existing edge.
	 * 
	 * @param originIndex
	 * @param edgeIndex
	 * @param x
	 * @param y
	 * @param z
	 * @param segType
	 * @param sModule
	 */
	private void addIntersection(int originIndex, int edgeIndex, Point3d point, SegmentType segType, Module sModule) {
		int vertexIndex1 = roadMap.getEdge(edgeIndex).startPointId;
		int vertexIndex2 = roadMap.getEdge(edgeIndex).endPointId;
		double distance1 = point.distance(roadMap.getVertex(vertexIndex1)); // 3D
																			// distance
																			// (original
																			// used
																			// 2D
																			// distance).
		double distance2 = point.distance(roadMap.getVertex(vertexIndex2)); // 3D
																			// distance
																			// (original
																			// used
																			// 2D
																			// distance).
		if ((distance1 > Config.ROAD_SEARCH_RADIUS) && (distance2 > Config.ROAD_SEARCH_RADIUS)) {
			Vertex vertex = new Vertex(point);

			int vertexIndex = roadMap.addVertex(vertex);

			// create edge 1
			Edge edge1 = new Edge(vertexIndex1, vertexIndex, roadMap.getEdge(edgeIndex).segmentType);

			// create edge 2
			Edge edge2 = new Edge(vertexIndex, vertexIndex2, roadMap.getEdge(edgeIndex).segmentType);

			// create edge 3
			Edge edge3 = new Edge(originIndex, vertexIndex, segType);

			// delete old edge and add new ones
			roadMap.deleteEdge(edgeIndex);
			roadMap.addEdge(edge1);
			roadMap.addEdge(edge2);
			roadMap.addEdge(edge3);

			// set missing parameters of successor module
			sModule.originVertexId = vertexIndex;
			sModule.status = ModuleStatus.ACCEPT;
		} else {
			if (distance1 <= Config.ROAD_SEARCH_RADIUS) {
				addEdge(originIndex, vertexIndex1, segType, sModule);
			}
			else {
				addEdge(originIndex, vertexIndex2, segType, sModule);
			}
		}

	}

	/**
	 * Adds a vertex and an edge to the road map. Used if a new road segment is
	 * not affected by existing ones.
	 * 
	 * @param originIndex
	 *            origin vertex
	 * @param point
	 *            the coordinates of the new vertex.
	 * @param segType
	 *            segment type
	 * @param sModule
	 *            current road/street segment module
	 */
	private void addVertexAndEdge(int originIndex, Point3d point, SegmentType segType, Module sModule) {
		// simply add a new vertex and a edge
		Vertex vertex = new Vertex(point);
		int vertexIndex = roadMap.addVertex(vertex);

		Edge edge = new Edge(originIndex, vertexIndex, segType);

		roadMap.addEdge(edge);
		// set missing parameters of successor module
		sModule.originVertexId = vertexIndex;
		sModule.status = ModuleStatus.ACCEPT;
	}

	/**
	 * Adds an edge to the road map. Used if a new road segment ends near an
	 * existing vertex.
	 * 
	 * @param originIndex
	 *            start point
	 * @param vertexIndex
	 *            end point
	 * @param segType
	 *            segment Type
	 * @param sModule
	 *            current Module.
	 */
	private void addEdge(int originIndex, int vertexIndex, SegmentType segType, Module sModule) {
		Edge edge = new Edge(originIndex, vertexIndex, segType);

		// add edge if not already in road map
		if ((roadMap.getAttachedEdgeCount(vertexIndex) < Config.ROAD_MAX_BRANCHES) &&
				(!roadMap.isConnected(originIndex, vertexIndex))) {
			// add edge
			roadMap.addEdge(edge);
			// set missing parameters of successor module
			sModule.originVertexId = vertexIndex;
			sModule.status = ModuleStatus.ACCEPT;
		}
	}

	/**
	 * Looks if a new segment's end point is near enough to a vertex so that
	 * both can be merged.
	 * 
	 * @param endPoint
	 *            end point position
	 * @return -1 if end point is not near another vertex, else the index of the
	 *         nearby vertex
	 */
	private int attachToNearbyVertex(Point3d endPoint) {
		// temporary vertex
		// Vertex vertex = new Vertex(endPoint);
		// some variables
		int iVertex = -1;
		double curdist = Config.ROAD_SEARCH_RADIUS;

		// search for nearby vertices
		List<Integer> nearby = roadMap.searchNearbyVertices(endPoint, Config.ROAD_SEARCH_RADIUS);
		// search for nearest vertex in the set
		for (int idx : nearby) {
			// 3D distance (original used 2D distance).
			double dist = endPoint.distance(roadMap.getVertex(idx));
			// check if target vertex is "good"
			if (roadMap.getAttachedEdgeCount(idx) < Config.ROAD_MAX_BRANCHES
					&& curdist > dist) {
				iVertex = idx;
				curdist = dist;
				// #BYREF
				endPoint.x = roadMap.getVertex(idx).x;
				endPoint.y = roadMap.getVertex(idx).y;
				endPoint.z = roadMap.getVertex(idx).z;
			}
		}
		// return index of vertex if found
		return iVertex;
	}

	/**
	 * Verify if a new segment intersects an existing segment.
	 * 
	 * @param origin
	 * @param dir
	 * @param e
	 * @return -1 if no intersection exists, otherwise the index of the
	 *         intersecting edge.
	 */
	private int intersectWithOtherEdges(int origin, Vector2d dir, Point3d e) {
		// temporary vertex
		Vertex vertex = new Vertex();
		vertex.x = e.x + dir.x * Config.ROAD_SEARCH_RADIUS;
		vertex.y = e.y + dir.y * Config.ROAD_SEARCH_RADIUS;
		vertex.z = 0.0;
		// some variables
		// double sx = roadMap.getVertex(origin).x;
		// double sy = roadMap.getVertex(origin).y;
		int iEdge = -1;
		double curdist = roadMap.getVertex(origin).distance(vertex); // 3D
																		// instead
																		// of
																		// 2D.
		// search intersections
		List<Intersection> intersections = roadMap.searchIntersectingEdges(vertex, roadMap.getVertex(origin));
		// search for nearest intersection
		for (Intersection intersection : intersections) {
			double dist = roadMap.getVertex(origin).distance(intersection); // 3D
																			// instead
																			// of
																			// 2D.
			if ((dist <= curdist) &&
					(roadMap.searchInAttachedEdges(origin, intersection.edgeIndex) == -1)) {
				iEdge = intersection.edgeIndex;
				// #BYREF
				e.x = intersection.x;
				e.y = intersection.y;
				e.z = heightMap.getInterpolatedValue(e.x, e.y);
				e.z = (float) (e.z * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));
			}
		}
		return iEdge;
	}

}
