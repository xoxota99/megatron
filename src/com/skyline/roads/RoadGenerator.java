package com.skyline.roads;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roads.condition.*;
import com.skyline.roads.modifier.*;
import com.skyline.roads.util.*;

/**
 * This is the actual workhorse that generates all the roads. Only one per
 * roadmap.
 * 
 * @author philippd
 * 
 */
public class RoadGenerator {

	// private long seed = 0L;
	// private Random random;

	private ValueMap2d<Double> heightMap;
	private ValueMap2d<Double> densityMap;
	private ValueMap2d<Byte> waterMap;
	private ValueMap2d<Byte> blockedMap;
	private ValueMap2d<Byte> patternMap;

	/**
	 * production manager of L-System
	 */
	private ProductionManager roadSys;

	private ModuleString mString; // L-System module string (really just a list
									// of Modules.

	private RoadMap roadMap = new RoadMap();	// road map

	private Condition nonZeroDelayCond = new ZeroDelayCondition(true);

	private long seed;


	public RoadGenerator(long seed, double startX, double startY, double endX, double endY) {
		this.seed = seed;

		// L-System setup.
		// create modules
		Module roadModule = new Module('R', ModuleType.ROAD);	//roads have 4 parameters.
		Module branchModule = new Module('B', ModuleType.BRANCH);	//branches have an additional roadType parameter (to describe the second branching).
		// create successors (right sides of productions)
		// for production p1
		ModuleString p1succ = new ModuleString();

		// for production p2
		ModuleString p2succ = new ModuleString();

		p2succ.add(branchModule);
		p2succ.add(branchModule);
		p2succ.add(branchModule);

		// for production p3
		ModuleString p3succ = new ModuleString();
		p3succ.add(branchModule);

		// for production p4
		ModuleString p4succ = new ModuleString();
		p4succ.add(roadModule);
		// create productions
		// production p1
		Production p1 = new Production(roadModule, p1succ, new StatusCondition(ModuleStatus.DELETE), new ZeroModifier(), null, null);
		// production p2
		Production p2 = new Production(roadModule, p2succ, new StatusCondition(ModuleStatus.ACCEPT), new DelayModifier(), null, null);
		// production p3
		Production p3 = new Production(branchModule, p3succ, nonZeroDelayCond, new BranchInitializer(roadMap, heightMap, densityMap, patternMap, this.seed), null, null);
		// production p4
		Production p4 = new Production(branchModule, p4succ, new ZeroDelayCondition(), new BranchModifier(roadMap, heightMap, densityMap, waterMap, blockedMap), null, null);
		// add productions to manager
		roadSys.add(p1);
		roadSys.add(p2);
		roadSys.add(p3);
		roadSys.add(p4);

		// create initial road map
		Vertex v1 = new Vertex(), v2 = new Vertex();
		Edge edge = new Edge();
		double z1 = heightMap.getInterpolatedValue(startX, startY);
		double z2 = heightMap.getInterpolatedValue(endX, endY);
		if (z1 == -1 || z2 == -1) {
			throw new RuntimeException("Error Setting initial road segment!");
		}
		v1.x = startX;
		v1.y = startY;
		v1.z = (float) (z1 * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));
		v1.belongsTo = -1;
		v2.x = endX;
		v2.y = endY;
		v2.z = (float) (z2 * (Config.TERRAIN_MAX_HEIGHT - Config.TERRAIN_MIN_HEIGHT));
		v2.belongsTo = -1;
		edge.startPointId = roadMap.addVertex(v1);
		edge.endPointId = roadMap.addVertex(v2);
		roadMap.addEdge(edge);
		edge.segmentType = SegmentType.ROAD;
		// create mString
		Module Rt = new Module(roadModule);
		Rt.delay = edge.startPointId;
		Rt.originVertexId = edge.endPointId;
		Rt.mainSegmentType = SegmentType.ROAD;
		Rt.status = ModuleStatus.ACCEPT;
		mString.add(Rt);
	}

	public void execute() {
		// apply L-System, generate road map, first only for roads
		System.out.printf("\nPhase 1: Creating initial road map ----------------------------------------\n");
		((ZeroDelayCondition) nonZeroDelayCond).setLookOnlyAtRoads(true);
		generateBaseRoadmap();
		// apply L-System, complete road map
		System.out.printf("\nPhase 2: Completing road map ----------------------------------------------\n");
		((ZeroDelayCondition) nonZeroDelayCond).setLookOnlyAtRoads(false);
		generateBaseRoadmap();
		// print some status information
		System.out.printf("\nGenerated road map has %d vertices and %d edges.\n", roadMap.getVertexCount(), roadMap.getEdgeCount());

		// do some filtering
		System.out.printf("\nFiltering -----------------------------------------------------------------\n");
		filter();
		System.out.printf("\nFiltered road map has %d vertices and %d edges.\n", roadMap.getVertexCount(), roadMap.getEdgeCount());

		// generate curbmap
		System.out.printf("\nGenerating curbs ----------------------------------------------------------\n");
		generateCurbs();
		System.out.printf("\nRoad map with curbs has %d vertices and %d edges.\n", roadMap.getVertexCount(), roadMap.getEdgeCount());

	}

	/**
	 * Filter road map. Remove dead ends and intersecting edges.
	 */
	private void filter() {
		// remove dead ends.
		int count = 0;
		for (int i = roadMap.getEdgeCount() - 1; i >= 0; i--) { // for every
																// edge
			Edge edge1 = roadMap.getEdge(i);
			for (int j = i - 1; j >= 0; j--) { // for every remaining edge
				Edge edge2 = roadMap.getEdge(j);
				if (((edge1.startPointId == edge2.startPointId) && (edge1.endPointId == edge2.endPointId)) ||
						((edge1.startPointId == edge2.endPointId) && (edge1.endPointId == edge2.startPointId))) {
					roadMap.deleteEdge(j); // de-dupe.
					i--;
					count++;
				}
			}
		}
		System.out.println("Removed " + count + " duplicate edges.");

		// remove all edges which still have intersections
		Set<Integer> badEdges = new TreeSet<Integer>(new Comparator<Integer>() {
			// sort in descending order.
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1; // descending.
			}
		});
		for (int i = 0; i < roadMap.getEdgeCount(); i++) {
			Vertex v1 = roadMap.getVertex(roadMap.getEdge(i).startPointId);
			Vertex v2 = roadMap.getVertex(roadMap.getEdge(i).endPointId);
			// List<Integer> attEdges1 =
			// roadMap.getAttachedEdges(roadMap.getEdge(i).startPointId);
			// List<Integer> attEdges2 =
			// roadMap.getAttachedEdges(roadMap.getEdge(i).endPointId);
			List<Intersection> intersections = roadMap.searchIntersectingEdges(v1, v2);
			for (Intersection x : intersections) {
				if ((roadMap.getEdge(x.edgeIndex).segmentType == SegmentType.STREET) &&
						(roadMap.searchInAttachedEdges(roadMap.getEdge(i).startPointId, x.edgeIndex) == -1) &&
						(roadMap.searchInAttachedEdges(roadMap.getEdge(i).endPointId, x.edgeIndex) == -1)) {
					badEdges.add(x.edgeIndex);
				}
			}
		}

		// remove these edges
		count = badEdges.size();
		for (Integer idx : badEdges) {
			roadMap.deleteEdge(idx);
		}
		System.out.println("Removed " + count + " 'bad' edges.");

		// remove all vertices which only have a degree of 1 or 0, except vertex
		// 0
		count = 0;
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = roadMap.getVertexCount() - 1; i > 0; i--) { // we go
																		// backwards,
																		// to
																		// "eat"
																		// the
																		// dead
																		// ends
																		// from
																		// the
																		// end.
				if (roadMap.getAttachedEdgeCount(i) < 2) {
					roadMap.deleteVertex(i);
					changed = true;
					count++;
				}
			}
		}
		System.out.println("Removed " + count + " edges of less than 2 degrees.");
	}

	/**
	 * Generate base road map by applying the L-System until no more changes
	 * occur.
	 */
	private void generateBaseRoadmap() {
		boolean changed = true;
		int step = 0;
		// repeat until roadMap doesn't change anymore
		while (changed) {
			// print some status information
			System.out.printf("%d:\t\t", step);
			System.out.printf("Vertices: %d", roadMap.getVertexCount());
			System.out.printf("\t\tEdges: %d\n", roadMap.getEdgeCount());

			// get some variables
			int numVertices = roadMap.getVertexCount();
			int numEdges = roadMap.getEdgeCount();

			// apply rules branchDelay + 2 times
			for (int i = 0; i <= Config.ROAD_BRANCH_DELAY + 1; i++) {
				roadSys.apply(mString);
				step++;
			}
			// look if changes have ocurred
			changed = (numVertices != roadMap.getVertexCount()) || (numEdges != roadMap.getEdgeCount());
		}
	}

	/**
	 * Generate curbs.
	 */
	void generateCurbs() {
		// sort attached edges
		roadMap.sortAttachedEdges();
		// generate curb vertices and info telling which vertex belongs to which
		// edges etc.
		List<CurbVertexInfo> cvInfo = new ArrayList<CurbVertexInfo>();
		generateCurbVertices(cvInfo);
		// generate curb edges
		generateCurbEdges(cvInfo);
	}

	/**
	 * Generate curb vertices.
	 * 
	 * @param cvInfo
	 *            curb vertex information
	 */
	void generateCurbVertices(List<CurbVertexInfo> cvInfo) {
		int numVertices = roadMap.getVertexCount();
		for (int i = 0; i < numVertices; i++) {
			if (roadMap.getAttachedEdgeCount(i) == 1) {
				Vector2d eDir = calculateEdgeDirection(i, roadMap.getAttachedEdges(i).get(0));

				// get two direction vectors
				Vector2d eDir1 = new Vector2d(eDir.y, -eDir.x);
				Vector2d eDir2 = new Vector2d(-eDir.y, eDir.x);

				// add 2 vertices
				Vertex v1 = new Vertex(), v2 = new Vertex();
				v1.x = roadMap.getVertex(i).x + eDir1.x * Config.ROAD_LANE_WIDTH;
				v1.y = roadMap.getVertex(i).y + eDir1.y * Config.ROAD_LANE_WIDTH;
				v1.z = roadMap.getVertex(i).z;
				v1.belongsTo = i;

				v2.x = roadMap.getVertex(i).x + eDir2.x * Config.ROAD_LANE_WIDTH;
				v2.y = roadMap.getVertex(i).y + eDir2.y * Config.ROAD_LANE_WIDTH;
				v2.z = roadMap.getVertex(i).z;
				v2.belongsTo = i;

				int index1 = roadMap.addVertex(v1);
				int index2 = roadMap.addVertex(v2);

				// add 2 curb vertex infos
				CurbVertexInfo cv1 = new CurbVertexInfo(), cv2 = new CurbVertexInfo();

				if (VecMath.angle(eDir1.x, eDir1.y) < 180.0) {
					cv1.eIndex[0] = roadMap.getAttachedEdges(i).get(0);
					cv1.eIndex[1] = -1;

					cv1.vIndex[0] = i;
					cv1.vIndex[1] = index1;

					cv2.eIndex[0] = -1;
					cv2.eIndex[1] = roadMap.getAttachedEdges(i).get(0);

					cv2.vIndex[0] = i;
					cv2.vIndex[1] = index2;
				}
				else {
					cv1.eIndex[0] = -1;
					cv1.eIndex[1] = roadMap.getAttachedEdges(i).get(0);
					cv1.vIndex[0] = i;
					cv1.vIndex[1] = index1;
					cv2.eIndex[0] = roadMap.getAttachedEdges(i).get(0);
					cv2.eIndex[1] = -1;
					cv2.vIndex[0] = i;
					cv2.vIndex[1] = index2;
				}
				cvInfo.add(cv1);
				cvInfo.add(cv2);
			}
			else {
				List<Integer> attEdges = roadMap.getAttachedEdges(i);
				for (int j = 0; j < (int) attEdges.size(); j++) {
					Vector2d e1Dir = calculateEdgeDirection(i, attEdges.get(j));
					Vector2d e2Dir = calculateEdgeDirection(i, attEdges.get((j + 1) % (int) attEdges.size()));

					double alpha1 = VecMath.angle(e1Dir.x, e1Dir.y);
					double alpha2 = VecMath.angle(e2Dir.x, e2Dir.y);
					double alpha, beta;
					if (alpha2 >= alpha1) {
						beta = (alpha2 - alpha1) / 2.0;
						alpha = alpha1 + beta;
					}
					else {
						beta = (360.0 - alpha2 + alpha1) / 2.0;
						alpha = alpha2 + beta;
					}
					Vector2d dir = VecMath.rotateNormalizedVector(alpha);

					// calculate scale factor
					double scale = 1.0;
					if (beta == 90.0) {
						scale = Config.ROAD_LANE_WIDTH;
					}
					else {
						scale = Math.abs(Config.ROAD_LANE_WIDTH / Math.sin(beta * VecMath.PI180));
					}

					// add a vertex
					Vertex vertex = new Vertex();
					vertex.x = roadMap.getVertex(i).x + dir.x * scale;
					vertex.y = roadMap.getVertex(i).y + dir.y * scale;
					vertex.z = roadMap.getVertex(i).z;
					vertex.belongsTo = i;
					int index = roadMap.addVertex(vertex);

					// add curb vertex info
					CurbVertexInfo cv = new CurbVertexInfo();
					cv.eIndex[0] = attEdges.get(j);
					cv.eIndex[1] = attEdges.get((j + 1) % (int) attEdges.size());
					cv.vIndex[0] = i;
					cv.vIndex[1] = index;
					cvInfo.add(cv);
				}
			}
		}
	}

	/**
	 * Generate curb edges.
	 * 
	 * @param cvInfo
	 *            curb vertex information
	 */
	void generateCurbEdges(List<CurbVertexInfo> cvInfo) {
		int numEdges = roadMap.getEdgeCount();
		// for each edge...
		for (int i = 0; i < numEdges; i++) {
			Edge edge = roadMap.getEdge(i);

			// search indices of curb vertices belonging to this road segment
			int[] ind1 = searchIndices(cvInfo, edge.startPointId, i);
			int[] ind2 = searchIndices(cvInfo, edge.endPointId, i);
			// make dead ends if necessary
			if (roadMap.getAttachedEdgeCount(edge.startPointId) == 1) {
				Edge edge1 = new Edge(), edge2 = new Edge();
				edge1.startPointId = ind1[0];
				edge1.endPointId = edge.startPointId;
				edge1.segmentType = SegmentType.CURB;
				edge1.leftCurbId = -1;
				edge1.rightCurbId = -1;
				edge2.startPointId = ind1[1];
				edge2.endPointId = edge.startPointId;
				edge2.segmentType = SegmentType.CURB;
				edge2.leftCurbId = -1;
				edge2.rightCurbId = -1;
				roadMap.addEdge(edge1);
				roadMap.addEdge(edge2);
			}

			if (roadMap.getAttachedEdgeCount(edge.endPointId) == 1) {
				Edge edge1 = new Edge(), edge2 = new Edge();
				edge1.startPointId = ind2[0];
				edge1.endPointId = edge.startPointId;
				edge1.segmentType = SegmentType.CURB;
				edge1.leftCurbId = -1;
				edge1.rightCurbId = -1;

				edge2.startPointId = ind2[1];
				edge2.endPointId = edge.startPointId;
				edge2.segmentType = SegmentType.CURB;
				edge2.leftCurbId = -1;
				edge2.rightCurbId = -1;

				roadMap.addEdge(edge1);
				roadMap.addEdge(edge2);
			}

			// add two curbs on each side of the edge
			Edge edge1 = new Edge(), edge2 = new Edge();
			edge1.startPointId = ind1[0];
			edge1.endPointId = ind2[1];
			edge1.segmentType = SegmentType.CURB;
			edge1.leftCurbId = -1;
			edge1.rightCurbId = -1;

			edge2.startPointId = ind1[1];
			edge2.endPointId = ind2[0];
			edge2.segmentType = SegmentType.CURB;
			edge2.leftCurbId = -1;
			edge2.rightCurbId = -1;

			int index1 = roadMap.addEdge(edge1);
			int index2 = roadMap.addEdge(edge2);
			roadMap.getEdge(i).leftCurbId = index1;
			roadMap.getEdge(i).rightCurbId = index2;
		}
	}

	// ///////////////////////////////////////////////////////////////////////
	// / Calculate direction of an edge from given vertex.
	// / \param vIndex vertex index
	// / \param eIndex edge index
	// / \param dirX direction
	// / \param dirY direction
	// ///////////////////////////////////////////////////////////////////////
	private Vector2d calculateEdgeDirection(int vIndex, int eIndex) {
		Vector2d dir = new Vector2d(0d, 0d);
		Edge edge = roadMap.getEdge(eIndex);
		if (edge.startPointId == vIndex) {
			Vertex v1 = roadMap.getVertex(vIndex);
			Vertex v2 = roadMap.getVertex(edge.endPointId);
			dir.x = v2.x - v1.x;
			dir.y = v2.y - v1.y;
		}
		if (edge.endPointId == vIndex) {
			Vertex v1 = roadMap.getVertex(vIndex);
			Vertex v2 = roadMap.getVertex(edge.startPointId);
			dir.x = v2.x - v1.x;
			dir.y = v2.y - v1.y;
		}
		dir.normalize();
		return dir;
	}

	/**
	 * Search two vertices in curb vertex information belonging to the given
	 * vertex with given attached edge (road/street).
	 * 
	 * @param cvInfo
	 *            curb vertex information
	 * @param vIndex
	 *            vertex index
	 * @param eIndex
	 *            edge index
	 * @return and array with exactly two (2) integers, retval[0] is
	 */
	int[] searchIndices(List<CurbVertexInfo> cvInfo, int vIndex, int eIndex) {
		int v1 = -1, v2 = -1;
		int edgeIndex1 = -1;
		int edgeIndex2 = -1;
		if (roadMap.getAttachedEdgeCount(vIndex) > 1) {
			int index = roadMap.searchInAttachedEdges(vIndex, eIndex);
			List<Integer> attEdges = roadMap.getAttachedEdges(vIndex);
			edgeIndex1 = attEdges.get((index + 1) % (int) attEdges.size());
			edgeIndex2 = attEdges.get((index - 1 + (int) attEdges.size()) % (int) attEdges.size());
		}
		for (CurbVertexInfo cvi : cvInfo) {
			if (cvi.vIndex[0] == vIndex) {
				if ((cvi.eIndex[0] == eIndex) && (cvi.eIndex[1] == edgeIndex1)) {
					v1 = cvi.vIndex[1];
				}
				// }
				// }
				// for (CurbVertexInfo cvi : cvInfo) {
				// if (cvi.vIndex[0] == vIndex) {
				if ((cvi.eIndex[1] == eIndex) && (cvi.eIndex[0] == edgeIndex2)) {
					v2 = cvi.vIndex[1];
				}
			}
		}
		return new int[] { v1, v2 };
	}

}
