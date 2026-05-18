package com.smartparking.graph;

import com.smartparking.models.GraphNode;
import com.smartparking.models.ParkingCell;

import java.util.Map;

public class ParkingGraph {
    private final Map<String, ParkingCell> cells;
    private final Map<String, GraphNode>   nodes;

    public ParkingGraph(Map<String, ParkingCell> cells, Map<String, GraphNode> nodes) {
        this.cells = cells;
        this.nodes = nodes;
    }

    public ParkingCell getCell(String id)  { return cells.get(id); }
    public GraphNode   getNode(String id)  { return nodes.get(id); }
    public Map<String, ParkingCell> getAllCells() { return cells; }
    public Map<String, GraphNode>   getAllNodes() { return nodes; }
}