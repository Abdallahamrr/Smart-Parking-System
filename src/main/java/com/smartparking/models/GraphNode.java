package com.smartparking.models;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    private final String cellId;         // matches ParkingCell.id
    private final int floor;
    private final int row;
    private final int col;
    private final CellType type;
    private final List<GraphEdge> edges;

    public GraphNode(ParkingCell cell) {
        this.cellId = cell.getId();
        this.floor  = cell.getFloor();
        this.row    = cell.getRow();
        this.col    = cell.getCol();
        this.type   = cell.getType();
        this.edges  = new ArrayList<>();
    }

    public void addEdge(GraphEdge edge)  { edges.add(edge); }
    public List<GraphEdge> getEdges()    { return edges; }
    public String getCellId()            { return cellId; }
    public int getFloor()                { return floor; }
    public int getRow()                  { return row; }
    public int getCol()                  { return col; }
    public CellType getType()            { return type; }
}