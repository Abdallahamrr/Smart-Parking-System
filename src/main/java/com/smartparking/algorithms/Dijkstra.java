package com.smartparking.algorithms;

import com.smartparking.graph.ParkingGraph;
import com.smartparking.models.GraphEdge;
import com.smartparking.models.GraphNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Dijkstra {

    public List<String> findShortestPath(ParkingGraph graph, String startId, String endId) {
        Map<String, Integer>  dist = new HashMap<>();
        Map<String, String>   prev = new HashMap<>();
        PriorityQueue<String> pq   = new PriorityQueue<>(Comparator.comparingInt(id -> dist.getOrDefault(id, Integer.MAX_VALUE)));

        for (String nodeId : graph.getAllNodes().keySet()) {
            dist.put(nodeId, Integer.MAX_VALUE);
        }
        dist.put(startId, 0);
        pq.add(startId);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (current.equals(endId)) break;

            GraphNode node = graph.getNode(current);
            if (node == null) continue;

            for (GraphEdge edge : node.getEdges()) {
                String neighbor = edge.getToId();
                int newDist = dist.get(current) + edge.getWeight();

                if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<String> path = new LinkedList<>();
        String step = endId;
        while (step != null) {
            path.add(0, step);
            step = prev.get(step);
        }

        if (path.isEmpty() || !path.get(0).equals(startId)) return Collections.emptyList();
        return path;
    }
}