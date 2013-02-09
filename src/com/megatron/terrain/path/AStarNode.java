package com.megatron.terrain.path;

import java.util.*;

public abstract class AStarNode implements Comparable<AStarNode> {

	AStarNode pathParent;
	float costFromStart;
	float estimatedCostToGoal;

	public float getCost() {
		return costFromStart + estimatedCostToGoal;
	}

	public int compareTo(AStarNode other) {
		float thisValue = this.getCost();
		float otherValue = ((AStarNode) other).getCost();

		float v = thisValue - otherValue;
		return (v > 0) ? 1 : (v < 0) ? -1 : 0; // sign function
	}

	/**
	 * Gets the cost between this node and the specified adjacent (AKA
	 * "neighbor" or "child") node.
	 */
	public abstract float getCost(AStarNode node);

	/**
	 * Gets the estimated cost between this node and the specified node. The
	 * estimated cost should never exceed the true cost. The better the
	 * estimate, the more efficient the search.
	 */
	public abstract float getEstimatedCost(AStarNode node);

	/**
	 * Gets the children (AKA "neighbors" or "adjacent nodes") of this node.
	 */
	public abstract List<AStarNode> getNeighbors();
}