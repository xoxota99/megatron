package com.megatron.util.path;

import java.util.*;

/**
 * The AStarSearch class, along with the AStarNode class, implements a generic
 * A* search algorithm. The AStarNode class should be subclassed to provide
 * searching capability.
 */
public class AStarSearch {

	/**
	 * A simple priority list, also called a priority queue. Objects in the list
	 * are ordered by their priority, determined by the object's Comparable
	 * interface. The highest priority item is first in the list.
	 */
	@SuppressWarnings("serial")
	public static class PriorityList<T extends Comparable<T>> extends LinkedList<T> {

		public boolean add(T object) {
			for (int i = 0; i < size(); i++) { // well with a list of 80
												// thousand nodes, this is going
												// to be slow...
				if (object.compareTo(get(i)) <= 0) {
					add(i, object);
					return true;
				}
			}
			addLast(object);
			return true;
		}
	}

	/**
	 * Construct the path, not including the start node.
	 */
	protected List<AStarNode> constructPath(AStarNode node) {
		LinkedList<AStarNode> path = new LinkedList<AStarNode>();
		while (node.pathParent != null) {
			path.addFirst(node);
			node = node.pathParent;
		}
		return path;
	}

	/**
	 * Find the path from the start node to the end node. A list of AStarNodes
	 * is returned, or null if the path is not found.
	 */
	public List<AStarNode> findPath(AStarNode startNode, AStarNode goalNode) {

		PriorityList<AStarNode> openList = new PriorityList<AStarNode>();
		LinkedList<AStarNode> closedList = new LinkedList<AStarNode>();

		startNode.costFromStart = 0;
		startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
		startNode.pathParent = null;
		openList.add(startNode);

		while (!openList.isEmpty()) {
			if (closedList.size() % 1000 == 0) {
				System.out.printf("%d / %d \n", openList.size(), closedList.size());
			}
			// pop the cheapest node off the list.
			AStarNode node = (AStarNode) openList.removeFirst();
			if (node == goalNode) {
				// construct the path from start to goal
				return constructPath(goalNode);
			}

			List<AStarNode> neighbors = node.getNeighbors();
			for (int i = 0; i < neighbors.size(); i++) {
				AStarNode neighborNode = (AStarNode) neighbors.get(i);
				boolean isOpen = openList.contains(neighborNode);
				boolean isClosed = closedList.contains(neighborNode);
				float costFromStart = node.costFromStart + node.getCost(neighborNode);

				// check if the neighbor node has not been
				// traversed or if a shorter path to this
				// neighbor node is found.
				if ((!isOpen && !isClosed) ||
						costFromStart < neighborNode.costFromStart)
				{
					neighborNode.pathParent = node;
					neighborNode.costFromStart = costFromStart; // running total
																// of the cost
																// at this node
																// (from the
																// start node).
					neighborNode.estimatedCostToGoal = neighborNode.getEstimatedCost(goalNode);
					if (isClosed) {
						closedList.remove(neighborNode);
					}
					if (!isOpen) {
						openList.add(neighborNode);
					}
				}
			}
			closedList.add(node);
		}

		// no path found
		return null;
	}

}