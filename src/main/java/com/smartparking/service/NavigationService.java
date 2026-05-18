package com.smartparking.service;

import com.smartparking.algorithms.Dijkstra;
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

    // Edge weights — must match GraphBuilder values exactly
    private static final int WEIGHT_ROAD         = 1;
    private static final int WEIGHT_PARKING_SPOT = 2;
    private static final int WEIGHT_RAMP         = 10;
    private static final int WEIGHT_ELEVATOR      = 15;

    // Approximate seconds per weight unit
    // Weight 1 ≈ 3 seconds of walking (roughly 1 grid cell at normal pace)
    private static final int SECONDS_PER_UNIT = 3;

    private final ParkingService parkingService;
    private final Dijkstra       dijkstra;

    public NavigationService(ParkingService parkingService, Dijkstra dijkstra) {
        this.parkingService = parkingService;
        this.dijkstra       = dijkstra;
    }

    public PathResponseDTO navigate(String fromId, String toId) {
        List<String> path = dijkstra.findShortestPath(
                parkingService.getGraph(), fromId, toId);

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

        // ── 2. Walk the edges and accumulate times ───────────────────────────
        int totalEdgeWeight      = 0;
        int elevatorEdgeWeight   = 0;
        int rampEdgeWeight       = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            String      currentId = path.get(i);
            String      nextId    = path.get(i + 1);
            GraphNode   node      = parkingService.getGraph().getNode(currentId);
            GraphEdge   edge      = findEdgeTo(node, nextId);

            if (edge != null) {
                totalEdgeWeight += edge.getWeight();

                if (edge.getEdgeType() == GraphEdge.EdgeType.ELEVATOR) {
                    elevatorEdgeWeight += edge.getWeight();
                } else if (edge.getEdgeType() == GraphEdge.EdgeType.RAMP) {
                    rampEdgeWeight += edge.getWeight();
                }
            }
        }

        int totalTimeSeconds    = totalEdgeWeight    * SECONDS_PER_UNIT;
        int elevatorWalkSeconds = elevatorEdgeWeight * SECONDS_PER_UNIT;
        int rampTimeSeconds     = rampEdgeWeight     * SECONDS_PER_UNIT;

        // ── 3. Build turn-by-turn instructions ──────────────────────────────
        List<TurnInstruction> turns = buildTurnByTurn(path);

        return new PathResponseDTO(
                true,
                fromId,
                toId,
                steps,
                totalEdgeWeight,
                totalTimeSeconds,
                elevatorWalkSeconds,
                rampTimeSeconds,
                turns);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Find the specific edge from a node that leads to targetId. */
    private GraphEdge findEdgeTo(GraphNode node, String targetId) {
        if (node == null) return null;
        for (GraphEdge edge : node.getEdges()) {
            if (edge.getToId().equals(targetId)) return edge;
        }
        return null;
    }

    /**
     * Produces human-readable turn instructions by looking at each consecutive
     * triple of cells (prev → current → next) to determine direction changes.
     *
     * Direction is encoded as (dRow, dCol):
     *   UP    = (-1,  0)
     *   DOWN  = ( 1,  0)
     *   LEFT  = ( 0, -1)
     *   RIGHT = ( 0,  1)
     */
    private List<TurnInstruction> buildTurnByTurn(List<String> path) {
        List<TurnInstruction> result = new ArrayList<>();

        if (path.size() < 2) return result;

        // First instruction — always "start moving"
        ParkingCell first = parkingService.getGraph().getCell(path.get(0));
        result.add(new TurnInstruction(
                "📍",
                "Start at " + friendlyName(first),
                first.getType().name(),
                first.getFloor()));

        for (int i = 1; i < path.size(); i++) {
            ParkingCell prev    = parkingService.getGraph().getCell(path.get(i - 1));
            ParkingCell current = parkingService.getGraph().getCell(path.get(i));

            CellType type = current.getType();

            // ── Elevator ────────────────────────────────────────────────────
            if (type == CellType.ELEVATOR) {
                int targetFloor = current.getFloor();
                // Look ahead to find which floor we exit on
                for (int j = i + 1; j < path.size(); j++) {
                    ParkingCell ahead = parkingService.getGraph().getCell(path.get(j));
                    if (ahead.getType() != CellType.ELEVATOR) {
                        targetFloor = ahead.getFloor();
                        break;
                    }
                }
                result.add(new TurnInstruction(
                        "🛗",
                        "Take the elevator to Floor " + targetFloor,
                        type.name(),
                        current.getFloor()));
                continue;
            }

            // ── Ramp ────────────────────────────────────────────────────────
            if (type == CellType.RAMP_UP || type == CellType.RAMP_BOTH) {
                result.add(new TurnInstruction(
                        "↑",
                        "Take the ramp up to Floor " + (current.getFloor() + 1),
                        type.name(),
                        current.getFloor()));
                continue;
            }
            if (type == CellType.RAMP_DOWN) {
                result.add(new TurnInstruction(
                        "↓",
                        "Take the ramp down to Floor " + (current.getFloor() - 1),
                        type.name(),
                        current.getFloor()));
                continue;
            }

            // ── Destination spot ─────────────────────────────────────────────
            if (type == CellType.PARKING_SPOT) {
                result.add(new TurnInstruction(
                        "🅿️",
                        "Arrive at spot " + current.getLabel() + " on Floor " + current.getFloor(),
                        type.name(),
                        current.getFloor()));
                continue;
            }

            // ── Floor change (ramp transition — cell on next floor) ──────────
            if (current.getFloor() != prev.getFloor()) {
                // Already handled above via RAMP_UP/DOWN/BOTH cell type,
                // but catch any missed transitions here
                String dir = current.getFloor() > prev.getFloor() ? "up" : "down";
                result.add(new TurnInstruction(
                        current.getFloor() > prev.getFloor() ? "↑" : "↓",
                        "Continue " + dir + " to Floor " + current.getFloor(),
                        type.name(),
                        current.getFloor()));
                continue;
            }

            // ── Turn detection (same floor) ──────────────────────────────────
            if (i >= 2) {
                ParkingCell before = parkingService.getGraph().getCell(path.get(i - 2));

                int prevDRow = prev.getRow()    - before.getRow();
                int prevDCol = prev.getCol()    - before.getCol();
                int currDRow = current.getRow() - prev.getRow();
                int currDCol = current.getCol() - prev.getCol();

                // Direction changed → emit a turn instruction
                if (prevDRow != currDRow || prevDCol != currDCol) {
                    String icon = turnIcon(prevDRow, prevDCol, currDRow, currDCol);
                    String desc = turnDescription(prevDRow, prevDCol, currDRow, currDCol);
                    if (icon != null) {
                        result.add(new TurnInstruction(
                                icon,
                                desc + " near " + friendlyName(current),
                                type.name(),
                                current.getFloor()));
                    }
                }
                // If direction unchanged, we're just going straight — no instruction needed
            } else {
                // Second cell — emit initial direction
                int dRow = current.getRow() - prev.getRow();
                int dCol = current.getCol() - prev.getCol();
                result.add(new TurnInstruction(
                        straightIcon(dRow, dCol),
                        "Go " + straightDescription(dRow, dCol),
                        type.name(),
                        current.getFloor()));
            }
        }

        return result;
    }

    /** Returns turn icon based on incoming and outgoing direction vectors. */
    private String turnIcon(int prevDRow, int prevDCol, int currDRow, int currDCol) {
        // Cross product sign: positive = left turn, negative = right turn (in row/col space)
        int cross = prevDRow * currDCol - prevDCol * currDRow;
        if (cross > 0) return "↰";   // turn left
        if (cross < 0) return "↱";   // turn right
        if (prevDRow == -currDRow && prevDCol == -currDCol) return "↩"; // U-turn
        return null; // same direction
    }

    private String turnDescription(int prevDRow, int prevDCol, int currDRow, int currDCol) {
        int cross = prevDRow * currDCol - prevDCol * currDRow;
        if (cross > 0) return "Turn left";
        if (cross < 0) return "Turn right";
        return "Continue";
    }

    private String straightIcon(int dRow, int dCol) {
        if (dRow == -1) return "↑";
        if (dRow ==  1) return "↓";
        if (dCol == -1) return "←";
        if (dCol ==  1) return "→";
        return "↑";
    }

    private String straightDescription(int dRow, int dCol) {
        if (dRow == -1) return "straight (north)";
        if (dRow ==  1) return "straight (south)";
        if (dCol == -1) return "straight (west)";
        if (dCol ==  1) return "straight (east)";
        return "straight";
    }

    /** Human-readable cell name for instructions. */
    private String friendlyName(ParkingCell cell) {
        return switch (cell.getType()) {
            case PARKING_SPOT -> "spot " + cell.getLabel();
            case ELEVATOR     -> "elevator (Floor " + cell.getFloor() + ")";
            case RAMP_UP      -> "ramp up (Floor " + cell.getFloor() + ")";
            case RAMP_DOWN    -> "ramp down (Floor " + cell.getFloor() + ")";
            case RAMP_BOTH    -> "ramp (Floor " + cell.getFloor() + ")";
            case INTERSECTION -> "intersection (Floor " + cell.getFloor() + ")";
            default           -> "road cell (Floor " + cell.getFloor() + ")";
        };
    }
}