package com.zerohub.challenge.graph_search;

import com.google.common.graph.ValueGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraWithPriorityQueue {

  public static <N> List<N> findShortestPath(final ValueGraph<N, Integer> graph, N source, N target) {
    Map<N, NodeWrapper<N>> nodeWrappers = new HashMap<>();
    PriorityQueue<NodeWrapper<N>> queue = new PriorityQueue<>();
    Set<N> shortestPathFound = new HashSet<>();

    NodeWrapper<N> sourceWrapper = new NodeWrapper<>(source, 0, null);
    nodeWrappers.put(source, sourceWrapper);
    queue.add(sourceWrapper);

    while (!queue.isEmpty()) {
      NodeWrapper<N> nodeWrapper = queue.poll();
      N node = nodeWrapper.getNode();
      shortestPathFound.add(node);

      if (node.equals(target)) {
        return buildPath(nodeWrapper);
      }

      Set<N> neighbors = graph.adjacentNodes(node);
      for (N neighbor : neighbors) {
        if (shortestPathFound.contains(neighbor)) {
          continue;
        }

        Integer distance = graph.edgeValueOrDefault(node, neighbor, null);
        if (null == distance) {
          throw new IllegalStateException();
        }
        int totalDistance = nodeWrapper.getTotalDistance() + distance;

        NodeWrapper<N> neighborWrapper = nodeWrappers.get(neighbor);
        if (neighborWrapper == null) {
          neighborWrapper = new NodeWrapper<>(neighbor, totalDistance, nodeWrapper);
          nodeWrappers.put(neighbor, neighborWrapper);
          queue.add(neighborWrapper);
        }

        else if (totalDistance < neighborWrapper.getTotalDistance()) {
          neighborWrapper.setTotalDistance(totalDistance);
          neighborWrapper.setPredecessor(nodeWrapper);

          queue.remove(neighborWrapper);
          queue.add(neighborWrapper);
        }
      }
    }

    return null;
  }

  private static <N> List<N> buildPath(NodeWrapper<N> nodeWrapper) {
    List<N> path = new ArrayList<>();
    while (nodeWrapper != null) {
      path.add(nodeWrapper.getNode());
      nodeWrapper = nodeWrapper.getPredecessor();
    }
    Collections.reverse(path);
    return path;
  }
}

class NodeWrapper<N> implements Comparable<NodeWrapper<N>> {
  private final N node;
  private int totalDistance;
  private NodeWrapper<N> predecessor;

  NodeWrapper(N node, int totalDistance, NodeWrapper<N> predecessor) {
    this.node = node;
    this.totalDistance = totalDistance;
    this.predecessor = predecessor;
  }

  N getNode() {
    return node;
  }

  void setTotalDistance(int totalDistance) {
    this.totalDistance = totalDistance;
  }

  public int getTotalDistance() {
    return totalDistance;
  }

  public void setPredecessor(NodeWrapper<N> predecessor) {
    this.predecessor = predecessor;
  }

  public NodeWrapper<N> getPredecessor() {
    return predecessor;
  }

  @Override
  public int compareTo(NodeWrapper<N> o) {
    return Integer.compare(this.totalDistance, o.totalDistance);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}