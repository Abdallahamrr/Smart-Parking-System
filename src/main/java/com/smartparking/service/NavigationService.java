package com.smartparking.service;

import com.smartparking.algorithms.Dijkstra;
import com.smartparking.algorithms.Dijkstra.TravelerMode;
import com.smartparking.dto.PathResponseDTO;
import com.smartparking.dto.PathResponseDTO.PathStep;
import com.smartparking.dto.PathResponseDTO.TurnInstruction;
import com.smartparking.models.CellType;
import com.smartparking.models.GraphEdge;
import com.smartparking.models.GraphNode;
import com.smartparking.models.ParkingCell;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NavigationService {

    // Each weight unit = 3 seconds of travel time
    // GraphBuilder weights: road=1, parking=2, ramp=10, elevator=15
    private static final int SECONDS_PER_UNIT = 3;

    private final ParkingService parkingService;
    private final Dijkstra       dijkstra;

    public NavigationService(ParkingService parkingService, Dijkstra dijkstra) {
        this.parkingService = parkingService;
        this.dijkstra       = dijkstra;
    }

    /** Default — called by old code, uses FOOT. */
    public PathResponseDTO navigate(String fromId, String toId) {
        return navigate(fromId, toId, TravelerMode.FOOT);
    }

    /** Main entry point used by NavigationController. */
    public PathResponseDTO navigate(String fromId, String toId, TravelerMode mode) {

        List<String> path = dijkstra.findShortestPath(
                parkingService.getGraph(), fromId, toId, mode);

        if (path.isEmpty()) {
            return new PathResponseDTO(false, fromId, toId, List.of(), 0);
        }

        // ── 1. Build PathStep list ───────────────────────────────────────────
        List<PathStep> steps = path.stream().map(id -> {
            ParkingCell cell = parkingService.getGraph().getCell(id);
            return new PathStep(
                    id,
                    cell.getFloor(),
                    cell.getRow(),
                    cell.getCol(),
                    cell.getType().name(),
                    cell.getLabel());
        }).collect(Collectors.toList());

        // ── 2. Sum actual edge weights along the path ────────────────────────
        int totalEdgeWeight    = 0;
        int elevatorEdgeWeight = 0;
        int rampEdgeWeight     = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            GraphNode node = parkingService.getGraph().getNode(path.get(i));
            GraphEdge edge = findEdgeTo(node, path.get(i + 1));

            if (edge != null) {
                totalEdgeWeight += edge.getWeight();
                switch (edge.getEdgeType()) {
                    case ELEVATOR -> elevatorEdgeWeight += edge.getWeight();
                    case RAMP     -> rampEdgeWeight     += edge.getWeight();
                    default       -> { /* WALK edges — already counted in total */ }
                }
            }
        }

        int totalTimeSeconds    = totalEdgeWeight    * SECONDS_PER_UNIT;
        int elevatorWalkSeconds = elevatorEdgeWeight * SECONDS_PER_UNIT;
        int rampTimeSeconds     = rampEdgeWeight     * SECONDS_PER_UNIT;

        // ── 3. Build turn-by-turn instructions ──────────────────────────────
        List<TurnInstruction> turns = buildTurnByTurn(path);

        return new PathResponseDTO(
                true, fromId, toId, steps,
                totalEdgeWeight,
                totalTimeSeconds,
                elevatorWalkSeconds,
                rampTimeSeconds,
                turns);
    }

    // ── Edge lookup ──────────────────────────────────────────────────────────

    private GraphEdge findEdgeTo(GraphNode node, String targetId) {
        if (node == null) return null;
        for (GraphEdge edge : node.getEdges()) {
            if (edge.getToId().equals(targetId)) return edge;
        }
        return null;
    }

    // ── Turn-by-turn builder ─────────────────────────────────────────────────

    private List<TurnInstruction> buildTurnByTurn(List<String> path) {
        List<TurnInstruction> result = new ArrayList<>();
        if (path.size() < 2) return result;

        ParkingCell first = parkingService.getGraph().getCell(path.get(0));
        result.add(new TurnInstruction("📍",
                "Start at " + friendlyName(first),
                first.getType().name(), first.getFloor()));

        for (int i = 1; i < path.size(); i++) {
            ParkingCell prev    = parkingService.getGraph().getCell(path.get(i - 1));
            ParkingCell current = parkingService.getGraph().getCell(path.get(i));
            CellType    type    = current.getType();

            // Elevator — emit one instruction for the whole ride, skip ahead
            if (type == CellType.ELEVATOR) {
                int exitFloor = current.getFloor();
                for (int j = i + 1; j < path.size(); j++) {
                    ParkingCell ahead = parkingService.getGraph().getCell(path.get(j));
                    if (ahead.getType() != CellType.ELEVATOR) {
                        exitFloor = ahead.getFloor();
                        break;
                    }
                }
                if (exitFloor != current.getFloor()) {
                    result.add(new TurnInstruction("🛗",
                            "Take the elevator from Floor " + current.getFloor()
                                    + " to Floor " + exitFloor,
                            type.name(), current.getFloor()));
                }
                while (i + 1 < path.size() &&
                       parkingService.getGraph().getCell(path.get(i + 1)).getType() == CellType.ELEVATOR) {
                    i++;
                }
                continue;
            }

            // Ramp up
            if ((type == CellType.RAMP_UP || type == CellType.RAMP_BOTH)
                    && i + 1 < path.size()) {
                ParkingCell next = parkingService.getGraph().getCell(path.get(i + 1));
                if (next.getFloor() > current.getFloor()) {
                    result.add(new TurnInstruction("↑",
                            "Take the ramp up to Floor " + next.getFloor(),
                            type.name(), current.getFloor()));
                    continue;
                }
            }

            // Ramp down
            if ((type == CellType.RAMP_DOWN || type == CellType.RAMP_BOTH)
                    && i + 1 < path.size()) {
                ParkingCell next = parkingService.getGraph().getCell(path.get(i + 1));
                if (next.getFloor() < current.getFloor()) {
                    result.add(new TurnInstruction("↓",
                            "Take the ramp down to Floor " + next.getFloor(),
                            type.name(), current.getFloor()));
                    continue;
                }
            }

            // Destination
            if (type == CellType.PARKING_SPOT) {
                result.add(new TurnInstruction("🅿️",
                        "Arrive at parking spot " + current.getLabel()
                                + " on Floor " + current.getFloor(),
                        type.name(), current.getFloor()));
                continue;
            }

            // Floor change without explicit ramp/elevator cell
            if (current.getFloor() != prev.getFloor()) {
                boolean goingUp = current.getFloor() > prev.getFloor();
                result.add(new TurnInstruction(goingUp ? "↑" : "↓",
                        "Continue " + (goingUp ? "up" : "down")
                                + " to Floor " + current.getFloor(),
                        type.name(), current.getFloor()));
                continue;
            }

            // Turn detection — same floor only
            if (i >= 2) {
                ParkingCell before = parkingService.getGraph().getCell(path.get(i - 2));
                if (before.getFloor() == prev.getFloor()
                        && prev.getFloor() == current.getFloor()) {

                    int prevDRow = prev.getRow()    - before.getRow();
                    int prevDCol = prev.getCol()    - before.getCol();
                    int currDRow = current.getRow() - prev.getRow();
                    int currDCol = current.getCol() - prev.getCol();

                    if (prevDRow != currDRow || prevDCol != currDCol) {
                        String icon = turnIcon(prevDRow, prevDCol, currDRow, currDCol);
                        String desc = turnDescription(prevDRow, prevDCol, currDRow, currDCol);
                        if (icon != null) {
                            result.add(new TurnInstruction(icon,
                                    desc + " near " + friendlyName(current),
                                    type.name(), current.getFloor()));
                        }
                    }
                }
            } else {
                int dRow = current.getRow() - prev.getRow();
                int dCol = current.getCol() - prev.getCol();
                result.add(new TurnInstruction(
                        straightIcon(dRow, dCol),
                        "Go " + straightDescription(dRow, dCol) + " on Floor " + current.getFloor(),
                        type.name(), current.getFloor()));
            }
        }

        return result;
    }

    // ── Direction helpers ────────────────────────────────────────────────────

    private String turnIcon(int pR, int pC, int cR, int cC) {
        int cross = pR * cC - pC * cR;
        if (cross > 0) return "↰";
        if (cross < 0) return "↱";
        if (pR == -cR && pC == -cC) return "↩";
        return null;
    }

    private String turnDescription(int pR, int pC, int cR, int cC) {
        int cross = pR * cC - pC * cR;
        if (cross > 0) return "Turn left";
        if (cross < 0) return "Turn right";
        return "U-turn";
    }

    private String straightIcon(int dRow, int dCol) {
        if (dRow == -1) return "↑";
        if (dRow ==  1) return "↓";
        if (dCol == -1) return "←";
        if (dCol ==  1) return "→";
        return "↑";
    }

    private String straightDescription(int dRow, int dCol) {
        if (dRow == -1) return "north";
        if (dRow ==  1) return "south";
        if (dCol == -1) return "west";
        if (dCol ==  1) return "east";
        return "straight";
    }

    private String friendlyName(ParkingCell cell) {
        return switch (cell.getType()) {
            case PARKING_SPOT -> "spot " + cell.getLabel();
            case ELEVATOR     -> "elevator (Floor " + cell.getFloor() + ")";
            case RAMP_UP      -> "ramp up (Floor " + cell.getFloor() + ")";
            case RAMP_DOWN    -> "ramp down (Floor " + cell.getFloor() + ")";
            case RAMP_BOTH    -> "ramp (Floor " + cell.getFloor() + ")";
            case INTERSECTION -> "intersection";
            default           -> "road (Floor " + cell.getFloor() + ")";
        };
    }
}