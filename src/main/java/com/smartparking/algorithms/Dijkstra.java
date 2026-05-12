package com.smartparking.algorithms;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class Dijkstra {

    // Hardcoded facility graph
    // Nodes represent junctions, ramps, and spot zones
    private final Map<String, List<int[]>> graph = new HashMap<>();

    public Dijkstra() {
        buildGraph();
    }

    private void buildGraph() {
        // Format: addEdge(from, to, weight in seconds)
        addEdge("ENTRY", "F1-A", 10);
        addEdge("ENTRY", "F1-B", 15);
        addEdge("F1-A", "F1-B", 8);
        addEdge("F1-B", "RAMP-1", 12);
        addEdge("RAMP-1", "F2-C", 20);
        addEdge("RAMP-1", "F2-D", 25);
        addEdge("F2-C", "F2-D", 8);
        addEdge("F2-D", "RAMP-2", 12);
        addEdge("RAMP-2", "F3-E", 20);
        addEdge("F3-E", "EXIT", 30);
        addEdge("F2-C", "EXIT", 35);
        addEdge("F1-B", "EXIT", 20);
    }

    private void addEdge(String from, String to, int weight) {
        graph.computeIfAbsent(from, k -> new ArrayList<>())
                .add(new int[]{nodeIndex(to), weight});
        graph.computeIfAbsent(to, k -> new ArrayList<>())
                .add(new int[]{nodeIndex(from), weight});
    }

    public Map<String, Object> findPath(String spotZone) {
        List<String> nodes = List.of(
                "ENTRY", "F1-A", "F1-B", "RAMP-1",
                "F2-C", "F2-D", "RAMP-2", "F3-E", "EXIT"
        );

        int n = nodes.size();
        int[] dist = new int[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);

        int src = nodeIndex("ENTRY");
        dist[src] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{src, 0});

        boolean[] visited = new boolean[n];

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0];
            if (visited[u]) continue;
            visited[u] = true;

            String nodeName = nodes.get(u);
            List<int[]> neighbors = graph.getOrDefault(nodeName, new ArrayList<>());

            for (int[] neighbor : neighbors) {
                int v = neighbor[0];
                int weight = neighbor[1];
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }

        // Reconstruct path to target zone
        int target = nodeIndex(spotZone);
        List<String> path = new ArrayList<>();
        for (int at = target; at != -1; at = prev[at]) {
            path.add(0, nodes.get(at));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("path", path);
        result.put("totalTimeSeconds", dist[target]);
        result.put("instructions", buildInstructions(path));
        return result;
    }

    private List<String> buildInstructions(List<String> path) {
        List<String> instructions = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            if (to.startsWith("RAMP")) {
                instructions.add("Take ramp from " + from + " to " + to);
            } else if (to.equals("EXIT")) {
                instructions.add("Proceed to EXIT");
            } else {
                instructions.add("Drive from " + from + " to " + to);
            }
        }
        return instructions;
    }

    private int nodeIndex(String node) {
        List<String> nodes = List.of(
                "ENTRY", "F1-A", "F1-B", "RAMP-1",
                "F2-C", "F2-D", "RAMP-2", "F3-E", "EXIT"
        );
        return nodes.indexOf(node);
    }
}