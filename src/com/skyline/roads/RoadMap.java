package com.skyline.roads;

import java.util.*;

import javax.vecmath.*;

/**
 * This is the "Map" that holds road, vertex, edge, and curb data.
 * 
 * @author philippd
 * 
 */
public class RoadMap {

	List<Vertex> vertices = new ArrayList<Vertex>();
	List<Edge> edges = new ArrayList<Edge>();
	List<List<Integer>> attachedEdges = new ArrayList<List<Integer>>();

	/**
	 * Add an vertex to the vertex list.
	 * 
	 * @param vertex
	 *            the vertex to add.
	 * @return index of the vertex, added to the vertex list.
	 */
	public int addVertex(Vertex vertex) {
		List<Integer> vec = new ArrayList<Integer>(); // empty IntVector
		vertices.add(vertex); // add vertex to list
		attachedEdges.add(vec); // add IntVector to list; vertex hasn't
								// any attached edges yet
		return (getVertexCount() - 1); // return as index (size - 1)
	}

	/**
	 * Add an edge to the edge list.
	 * 
	 * @param edge
	 *            the edge to add
	 * @return the index of the newly added edge, in the edge collection.
	 */
	public int addEdge(Edge edge) {
		// throw an exception if first specified vertex doesn't exist
		assert edge.startPointId >= 0 && edge.startPointId < getVertexCount() : "StartPoint Vertex doesn't exist";
		assert edge.endPointId >= 0 && edge.endPointId < getVertexCount() : "EndPoint Vertex doesn't exist";
		assert edge.startPointId != edge.endPointId : "First vertex equals second vertex!";
		// check if edge is already in list, if it is throw an exception
		assert !isConnected(edge.startPointId, edge.endPointId) : "Tried to add already existing edge!";

		edges.add(edge);

		// edge index, which is (size - 1)
		int index = getEdgeCount() - 1;

		// add edge to attached edge list for each vertex
		attachedEdges.get(edge.startPointId).add(index);
		attachedEdges.get(edge.endPointId).add(index);

		// return edge index, which is (size - 1)
		return index;
	}

	/**
	 * Remove a vertex, and any attached edges. This is an expensive operation.
	 * 
	 * @param index
	 *            index of the vertex to delete
	 */
	public void deleteVertex(int index) {
		// check index and throw an exception if it's out of bounds
		assert (index >= 0) && (index < getVertexCount()) : "Tried to access non-existant vertex!";
		// get attached edges
		List<Integer> attEdges = attachedEdges.get(index);

		// delete vertex
		vertices.remove(index);

		// delete attached edges vector
		attachedEdges.remove(index);

		// sort attached edges (ascending)
		Collections.sort(attEdges);

		// delete them
		for (int idx : attEdges) {
			edges.remove(idx);
		}

		// clear attached edges vectors
		for (List<Integer> l : attachedEdges) {
			l.clear();
		}

		// update edges
		for (int m = 0; m < (int) edges.size(); m++) {
			Edge edge = edges.get(m);
			if (edge.startPointId >= index)
				edge.startPointId--;
			if (edge.endPointId >= index)
				edge.endPointId--;
			attachedEdges.get(edge.startPointId).add(m);
			attachedEdges.get(edge.endPointId).add(m);
		}

	}

	/**
	 * Delete the specified edge.
	 * 
	 * @param index
	 *            the edge to delete.
	 */
	public void deleteEdge(int index) {
		// check index and throw an exception if it's out of bounds
		assert (index >= 0) && (index < getEdgeCount()) : "Tried to access non-existant edge!";

		// delete edge
		edges.remove(index);

		// clear attached edge list
		for (List<Integer> attEdges : attachedEdges) {
			// Before you ask: No, you CAN'T just do
			// "for (Integer idx : attEdges)"
			for (int j = attEdges.size() - 1; j > 0; j--) {
				Integer idx = attEdges.get(j);
				if (idx == index)
					attEdges.remove(j);
				else {
					if (idx > index)
						attEdges.set(j, idx - 1);
				}
			}
		}
	}

	/**
	 * Get the specified vertex.
	 * 
	 * @param index
	 *            index of the vertex to return.
	 * @return
	 */
	public Vertex getVertex(int index) {
		// check index and throw an exception if it's out of bounds
		assert (index >= 0) && (index < getVertexCount()) : "Tried to access non-existant vertex!";

		return vertices.get(index); // return vertex
	}

	/**
	 * return the specified edge
	 * 
	 * @param index
	 *            index of the edge to return.
	 * @return
	 */
	public Edge getEdge(int index) {
		// check index and throw an exception if it's out of bounds
		assert (index >= 0) && (index < getEdgeCount()) : "Tried to access non-existant vertex!";

		return edges.get(index); // return edge
	}

	/**
	 * Return a list of the edges attached to a specific vertex.
	 * 
	 * @param index
	 *            the index of the vertex to examine.
	 * @return a list containing the indices of all attached edges.
	 */

	public List<Integer> getAttachedEdges(int index) {
		// check index and throw an exception if it's out of bounds
		assert (index >= 0) && (index < attachedEdges.size()) : "Tried to access non-existant vertex!";

		return attachedEdges.get(index); // return edge
	}

	/**
	 * 
	 * @return count of all vertices.
	 */
	public int getVertexCount() {
		return vertices.size();
	}

	/**
	 * 
	 * @return count of all edges.
	 */
	public int getEdgeCount() {
		return edges.size();
	}

	/**
	 * 
	 * @param index
	 *            index of the vertex to check
	 * @return the number of edges attached to the specified vertex.
	 */
	public int getAttachedEdgeCount(int index) {
		return attachedEdges.get(index).size();
	}

	/**
	 * Search for nearby vertices. Search will omit Z coordinate, so it is
	 * basically 2D in the XY-Plane.
	 * 
	 * @param point
	 * @param roadSearchRadius
	 * @return
	 */
	public List<Integer> searchNearbyVertices(Point3d point, double radius) {
		// clear result vector
		List<Integer> result = new ArrayList<Integer>();
		// calculate radius * radius so we can omit sqrt
		double radius2 = radius * radius;
		// for each vertex do...
		for (int i = 0; i < vertices.size() - 1; i++) {
			Vertex v = vertices.get(i);
			double dx = point.x - v.x;
			double dy = point.y - v.y;
			double dist2 = dx * dx + dy * dy;
			if (dist2 <= radius2) {
				// close enough.
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Search for intersecting edges to a given one. Search will omit Z
	 * coordinate, so it is basically 2D in the XY-Plane.
	 * 
	 * @param startPoint
	 *            startpoint of the edge.
	 * @param endPoint
	 *            endpoint of the edge.
	 * @return a list of Intersections, including the edgeId of the intersected
	 *         edge, and the X/Y location of the intersection.
	 */
	public List<Intersection> searchIntersectingEdges(Point3d v1, Point3d v2) {
		List<Intersection> result = new ArrayList<Intersection>();
		// get vertices positions
		double a[] = { v1.x, v1.y };
		double b[] = { v2.x, v2.y };
		// calculate bounding box for a & b
		double boundAB[] = new double[4];
		if (a[0] <= b[0]) {
			boundAB[0] = a[0];
			boundAB[2] = b[0];
		}
		else {
			boundAB[2] = a[0];
			boundAB[0] = b[0];
		}
		if (a[1] <= b[1]) {
			boundAB[1] = a[1];
			boundAB[3] = b[1];
		}
		else {
			boundAB[3] = a[1];
			boundAB[1] = b[1];
		}
		// calculate direction vector
		double u[] = { b[0] - a[0], b[1] - a[1] };
		// for each edge do...
		for (int count = 0; count < (int) edges.size(); count++) {
			// get vertices positions
			int v1index = edges.get(count).startPointId;
			int v2index = edges.get(count).endPointId;
			double c[] = { vertices.get(v1index).x, vertices.get(v1index).y };
			double d[] = { vertices.get(v2index).x, vertices.get(v2index).y };
			// calculate bounding box for c & d
			double boundCD[] = new double[4];
			if (c[0] <= d[0]) {
				boundCD[0] = c[0];
				boundCD[2] = d[0];
			}
			else {
				boundCD[2] = c[0];
				boundCD[0] = d[0];
			}
			if (c[1] <= d[1]) {
				boundCD[1] = c[1];
				boundCD[3] = d[1];
			}
			else {
				boundCD[3] = c[1];
				boundCD[1] = d[1];
			}

			// if boxes don't overlap, intersection of lines isn't possible
			double eps = 0.1;
			if (boundCD[0] > boundAB[2] + eps)
				continue;
			if (boundCD[2] + eps < boundAB[0])
				continue;
			if (boundCD[1] > boundAB[3] + eps)
				continue;
			if (boundCD[3] + eps < boundAB[1])
				continue;

			// calculate direction vector
			double v[] = { d[0] - c[0], d[1] - c[1] };
			// calculate common divisor
			double div = u[0] * v[1] - u[1] * v[0];
			// if divisor is near zero the two lines are parallel
			if ((div > 0.0000001) || (div < -0.0000001)) {
				// calculate intersection
				double j = -(-u[0] * a[1] + u[0] * c[1] + u[1] * a[0] - u[1] * c[0]) / div;
				double i = -(a[0] * v[1] - c[0] * v[1] - v[0] * a[1] + v[0] * c[1]) / div;
				// if intersection occurs in the part of the line which we are
				// interested in...
				if ((i >= 0.0) && (i <= 1.0) && (j >= 0.0) && (j <= 1.0)) {
					// calculate intersection record and add it to result
					Intersection t = new Intersection();
					t.edgeIndex = count;
					t.x = a[0] + i * u[0];
					t.y = a[1] + i * u[1];
					result.add(t);
				}
			}
		}
		return result;
	}

	/**
	 * Return if a vertex is directly connected to another.
	 * 
	 * @param index1
	 *            index of the first vertex
	 * @param index2
	 *            index of the second vertex.
	 * @return
	 */
	public boolean isConnected(int index1, int index2) {
		assert index1 != index2 : "Trying to determine if a vertex is attached to itself!";
		List<Integer> attEdge = attachedEdges.get(index1);
		for (int c = 0; c < (int) attEdge.size(); c++) {
			if ((edges.get(attEdge.get(c)).startPointId == index2) ||
					(edges.get(attEdge.get(c)).endPointId == index2))
				return true;
		}
		return false;
	}

	/**
	 * Sort attached Edges, according to their angle from the vertex.
	 * 
	 * At first glance, this seems like an extremely costly operation.
	 */
	public void sortAttachedEdges() {
		// for each vertex...
		for (int i = 0; i < (int) vertices.size(); i++) {
			// some variables
			List<Integer> attEdges = attachedEdges.get(i); // attached edges for
															// this vertex.
			double angle[] = new double[10];
			// for each attached edge calculate its angle...
			for (int j = 0; j < (int) attEdges.size(); j++) {
				// some variables
				Vertex v1, v2;
				Edge edge = edges.get(attEdges.get(j));
				// get edge end vertices, v1 is vertex for which we are
				// currently sorting
				if (edge.startPointId == i) {
					v1 = vertices.get(edge.startPointId);
					v2 = vertices.get(edge.endPointId);
				}
				else {
					v2 = vertices.get(edge.startPointId);
					v1 = vertices.get(edge.endPointId);
				}
				// calculate normalized direction vector
				double x = v2.x - v1.x;
				double y = v2.y - v1.y;
				double s = 1.0 / Math.sqrt(x * x + y * y);
				x *= s;
				y *= s;
				// calculate angle and write it in angle array
				if ((x >= 0.0) && (y >= 0.0))
					angle[j] = Math.asin(y);
				if ((x < 0.0) && (y >= 0.0))
					angle[j] = Math.PI - Math.asin(y);
				if ((x < 0.0) && (y < 0.0))
					angle[j] = Math.PI + Math.asin(-y);
				if ((x >= 0.0) && (y < 0.0))
					angle[j] = 2 * Math.PI - Math.asin(-y);
			}

			// do a bubble sort to sort attached edges
			System.out.println("Bubble sorting " + attEdges.size() + " edges.");
			for (int a = 0; a < (int) attEdges.size() - 1; a++) {
				for (int b = 0; b < (int) attEdges.size() - a - 1; b++) {
					if (angle[b] > angle[b + 1]) {
						// swap angle array elements
						double t1 = angle[b];
						angle[b] = angle[b + 1];
						angle[b + 1] = t1;

						// swap attached edges indices
						int t2 = attEdges.get(b);
						attEdges.set(b, attEdges.get(b + 1));
						attEdges.set(b + 1, t2);
					}
				}
			}
		}
	}

	public int searchInAttachedEdges(int vIndex, int eIndex) {
		List<Integer> attEdges = attachedEdges.get(vIndex);
		for (Integer i : attEdges) {
			if (i == eIndex)
				return i;
		}
		return -1;
	}

	public void dump() {
		System.out.printf("\nRoadmap object dump =====================\n");
		int sizev = getVertexCount();
		int sizee = getEdgeCount();
		System.out.printf("%d vertices, %d edges\n", sizev, sizee);
		System.out.printf("Vertices-----------------------------------------\n");
		int i = 0;
		for (Vertex v : vertices) {
			++i;
			System.out.printf("%d: (%d | %d | %d) -> ", i, v.x, v.y, v.z);

			List<Integer> p = getAttachedEdges(i);
			for (Integer eIdx : p) {
				System.out.printf("%d ", eIdx);
			}
			System.out.printf("\n");
		}
		System.out.printf("Edges--------------------------------------------\n");
		i = 0;
		for (Edge e : edges) {
			++i;
			System.out.printf("%d: (%d <-> %d)\n", i, e.startPointId, e.endPointId);
		}
		System.out.printf("=========================================\n\n");
	}
}
