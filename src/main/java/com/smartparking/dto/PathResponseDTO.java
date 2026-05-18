package com.smartparking.dto;

import java.util.List;

public class PathResponseDTO {

    private boolean found;
    private String fromId;
    private String toId;
    private List<PathStep> path;
    private int totalCost;

    // ── NEW fields ──────────────────────────────────────────────────────────
    private int totalTimeSeconds;      // sum of all edge weights along the path
    private int elevatorWalkSeconds;   // total time spent on elevator edges
    private int rampTimeSeconds;       // total time spent on ramp edges
    private List<TurnInstruction> turnByTurn;  // human-readable directions
    // ────────────────────────────────────────────────────────────────────────

    // ── Constructor for "not found" case ────────────────────────────────────
    public PathResponseDTO(boolean found, String fromId, String toId,
                           List<PathStep> path, int totalCost) {
        this.found               = found;
        this.fromId              = fromId;
        this.toId                = toId;
        this.path                = path;
        this.totalCost           = totalCost;
        this.totalTimeSeconds    = 0;
        this.elevatorWalkSeconds = 0;
        this.rampTimeSeconds     = 0;
        this.turnByTurn          = List.of();
    }

    // ── Constructor for "found" case ─────────────────────────────────────────
    public PathResponseDTO(boolean found, String fromId, String toId,
                           List<PathStep> path, int totalCost,
                           int totalTimeSeconds, int elevatorWalkSeconds,
                           int rampTimeSeconds, List<TurnInstruction> turnByTurn) {
        this.found               = found;
        this.fromId              = fromId;
        this.toId                = toId;
        this.path                = path;
        this.totalCost           = totalCost;
        this.totalTimeSeconds    = totalTimeSeconds;
        this.elevatorWalkSeconds = elevatorWalkSeconds;
        this.rampTimeSeconds     = rampTimeSeconds;
        this.turnByTurn          = turnByTurn;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public boolean isFound()                        { return found; }
    public String getFromId()                       { return fromId; }
    public String getToId()                         { return toId; }
    public List<PathStep> getPath()                 { return path; }
    public int getTotalCost()                       { return totalCost; }
    public int getTotalTimeSeconds()                { return totalTimeSeconds; }
    public int getElevatorWalkSeconds()             { return elevatorWalkSeconds; }
    public int getRampTimeSeconds()                 { return rampTimeSeconds; }
    public List<TurnInstruction> getTurnByTurn()    { return turnByTurn; }

    // ── Nested: one step in the path ─────────────────────────────────────────
    public static class PathStep {
        public String id;
        public int floor;
        public int row;
        public int col;
        public String type;
        public String label;

        public PathStep(String id, int floor, int row, int col,
                        String type, String label) {
            this.id    = id;
            this.floor = floor;
            this.row   = row;
            this.col   = col;
            this.type  = type;
            this.label = label;
        }
    }

    // ── Nested: one turn instruction ─────────────────────────────────────────
    public static class TurnInstruction {
        public String icon;         // "↑" "↓" "←" "→" "🛗" "↪" "📍"
        public String description;  // human-readable text
        public String cellType;     // ROAD, RAMP_UP, ELEVATOR, PARKING_SPOT …
        public int floor;           // which floor this instruction is on

        public TurnInstruction(String icon, String description,
                               String cellType, int floor) {
            this.icon        = icon;
            this.description = description;
            this.cellType    = cellType;
            this.floor       = floor;
        }
    }
}