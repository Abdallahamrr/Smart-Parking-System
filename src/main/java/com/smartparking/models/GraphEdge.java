package com.smartparking.models;

public class GraphEdge {
    private final String fromId;
    private final String toId;
    private final int weight;          // cost in seconds or distance units
    private final EdgeType edgeType;

    public enum EdgeType { WALK, DRIVE, RAMP, ELEVATOR }

    public GraphEdge(String fromId, String toId, int weight, EdgeType edgeType) {
        this.fromId   = fromId;
        this.toId     = toId;
        this.weight   = weight;
        this.edgeType = edgeType;
    }

    public String getFromId()      { return fromId; }
    public String getToId()        { return toId; }
    public int getWeight()         { return weight; }
    public EdgeType getEdgeType()  { return edgeType; }
}