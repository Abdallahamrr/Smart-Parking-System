package com.smartparking.algorithms;

import com.smartparking.graph.ParkingGraph;
import com.smartparking.models.GraphEdge;
import com.smartparking.models.GraphNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Dijkstra {

    public enum TravelerMode { CAR, FOOT }

    /** Defaults to FOOT when no mode provided — nothing else breaks. */
    public List<String> findShortestPath(ParkingGraph graph,
                                         String startId,
                                         String endId) {
        return findShortestPath(graph, startId, endId, TravelerMode.FOOT);
    }

    /**
     * CAR  → WALK + RAMP edges only  (no elevators)
     * FOOT → WALK + ELEVATOR edges only (no ramps)
     */
    public List<String> findShortestPath(ParkingGraph graph,
                                         String startId,
                                         String endId,
                                         TravelerMode mode) {

        Map<String, Integer>  dist = new HashMap<>();
        Map<String, String>   prev = new HashMap<>();
        PriorityQueue<String> pq   = new PriorityQueue<>(
                Comparator.comparingInt(id -> dist.getOrDefault(id, Integer.MAX_VALUE)));

        for (String nodeId : graph.getAllNodes().keySet()) {
            dist.put(nodeId, Integer.MAX_VALUE);
        }
        dist.put(startId, 0);
        pq.add(startId);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (current.equals(endId)) break;

            int currentDist = dist.getOrDefault(current, Integer.MAX_VALUE);
            GraphNode node = graph.getNode(current);
            if (node == null) continue;

            for (GraphEdge edge : node.getEdges()) {
                // ── Mode filter ──────────────────────────────────────────────
                if (!isEdgeAllowed(edge.getEdgeType(), mode)) continue;
                // ─────────────────────────────────────────────────────────────

                String neighbor = edge.getToId();
                int newDist = currentDist + edge.getWeight();

                if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        List<String> path = new LinkedList<>();
        String step = endId;
        while (step != null) {
            path.add(0, step);
            step = prev.get(step);
        }

        if (path.isEmpty() || !path.get(0).equals(startId)) {
            return Collections.emptyList();
        }
        return path;
    }

    /**
     * CAR  → WALK allowed, RAMP allowed, ELEVATOR blocked
     * FOOT → WALK allowed, ELEVATOR allowed, RAMP blocked
     */
    private boolean isEdgeAllowed(GraphEdge.EdgeType edgeType, TravelerMode mode) {
        if (edgeType == GraphEdge.EdgeType.WALK) return true;
        if (mode == TravelerMode.CAR)  return edgeType == GraphEdge.EdgeType.RAMP;
        else                           return edgeType == GraphEdge.EdgeType.ELEVATOR;
    }
}