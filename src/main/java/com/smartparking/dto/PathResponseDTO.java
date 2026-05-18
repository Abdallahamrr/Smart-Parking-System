package com.smartparking.dto;

import java.util.List;

public class PathResponseDTO {
    private boolean found;
    private String fromId;
    private String toId;
    private List<PathStep> path;
    private int totalCost;

    public PathResponseDTO(boolean found, String fromId, String toId,
                           List<PathStep> path, int totalCost) {
        this.found     = found;
        this.fromId    = fromId;
        this.toId      = toId;
        this.path      = path;
        this.totalCost = totalCost;
    }

    // Getters
    public boolean isFound()         { return found; }
    public String getFromId()        { return fromId; }
    public String getToId()          { return toId; }
    public List<PathStep> getPath()  { return path; }
    public int getTotalCost()        { return totalCost; }

    public static class PathStep {
        public String id;
        public int floor, row, col;
        public String type, label;

        public PathStep(String id, int floor, int row, int col, String type, String label) {
            this.id = id; this.floor = floor; this.row = row;
            this.col = col; this.type = type; this.label = label;
        }
    }
}