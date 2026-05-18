package com.smartparking.graph;

import com.smartparking.models.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraphBuilder {

    /*
      Layout definition per floor.
      'P' = parking spot
      'R' = road
      'U' = ramp up
      'D' = ramp down
      'B' = ramp both (up and down)
      'E' = elevator
      'W' = wall
      'X' = intersection
    */
    private static final String[][] FLOOR_1_LAYOUT = {
        { "W","W","W","W","W","W","W","W","W","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","R","R","X","R","R","X","R","R","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","W","E","W","W","W","U","W","W","W" },
    };

    private static final String[][] FLOOR_2_LAYOUT = {
        { "W","W","W","W","W","W","W","W","W","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","R","R","X","R","R","X","R","R","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","W","E","W","W","W","B","W","W","W" },
    };

    private static final String[][] FLOOR_3_LAYOUT = {
        { "W","W","W","W","W","W","W","W","W","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","R","R","X","R","R","X","R","R","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","P","P","R","P","P","R","P","P","W" },
        { "W","W","E","W","W","W","D","W","W","W" },
    };

    private static final String[][][] ALL_FLOORS = {
        FLOOR_1_LAYOUT, FLOOR_2_LAYOUT, FLOOR_3_LAYOUT
    };

    public ParkingGraph build() {
        Map<String, ParkingCell> cells = new HashMap<>();
        Map<String, GraphNode>   nodes = new HashMap<>();

        // Build cells and nodes
        for (int f = 0; f < ALL_FLOORS.length; f++) {
            int floorNum = f + 1;
            String[][] layout = ALL_FLOORS[f];
            int spotCounter = 0;

            for (int r = 0; r < layout.length; r++) {
                for (int c = 0; c < layout[r].length; c++) {
                    String sym = layout[r][c];
                    CellType type = symbolToType(sym);
                    String id = buildId(floorNum, r, c, type, spotCounter);
                    if (type == CellType.PARKING_SPOT) spotCounter++;

                    ParkingCell cell = new ParkingCell(id, floorNum, r, c, type);
                    if (type == CellType.PARKING_SPOT) {
                        // Specialize spot sizes based on spotCounter (which ranges from 1 to 24 per floor)
                        // Assigning:
                        // COMPACT (25%) -> 6 spots/floor (mod 1, 2)
                        // STANDARD (37.5%) -> 9 spots/floor (mod 3, 4, 5)
                        // SUV (25%) -> 6 spots/floor (mod 6, 7)
                        // TRUCK (12.5%) -> 3 spots/floor (mod 0)
                        VehicleType specializedType;
                        int mod = spotCounter % 8;
                        if (mod == 1 || mod == 2) {
                            specializedType = VehicleType.COMPACT;
                        } else if (mod == 3 || mod == 4 || mod == 5) {
                            specializedType = VehicleType.STANDARD;
                        } else if (mod == 6 || mod == 7) {
                            specializedType = VehicleType.SUV;
                        } else {
                            specializedType = VehicleType.TRUCK;
                        }
                        cell.setMaxSize(specializedType);
                    }
                    cells.put(id, cell);
                    nodes.put(id, new GraphNode(cell));
                }
            }
        }

        // Add same-floor edges
        addSameFloorEdges(nodes, cells, ALL_FLOORS);

        // Add inter-floor edges (ramps + elevators)
        addInterFloorEdges(nodes, cells, ALL_FLOORS);

        return new ParkingGraph(cells, nodes);
    }

    private void addSameFloorEdges(Map<String, GraphNode> nodes,
                                   Map<String, ParkingCell> cells,
                                   String[][][] allFloors) {
        int[][] dirs = { {0,1},{0,-1},{1,0},{-1,0} };

        for (ParkingCell cell : cells.values()) {
            if (cell.getType() == CellType.WALL) continue;
            int f = cell.getFloor() - 1;
            int r = cell.getRow(), c = cell.getCol();

            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nc < 0
                    || nr >= allFloors[f].length
                    || nc >= allFloors[f][nr].length) continue;

                String neighborId = findCellAt(cells, cell.getFloor(), nr, nc);
                if (neighborId == null) continue;

                ParkingCell neighbor = cells.get(neighborId);
                if (neighbor.getType() == CellType.WALL) continue;

                int weight = edgeWeight(cell.getType(), neighbor.getType());
                GraphNode from = nodes.get(cell.getId());
                from.addEdge(new GraphEdge(
                    cell.getId(), neighborId, weight, GraphEdge.EdgeType.WALK
                ));
            }
        }
    }

    private void addInterFloorEdges(Map<String, GraphNode> nodes,
                                    Map<String, ParkingCell> cells,
                                    String[][][] allFloors) {
        for (ParkingCell cell : cells.values()) {
            CellType type = cell.getType();
            int f = cell.getFloor();

            // Ramp up: connect F1 ramp-up to F2 ramp-down/both
            if (type == CellType.RAMP_UP || type == CellType.RAMP_BOTH) {
                String upNeighbor = findCellAt(cells, f + 1, cell.getRow(), cell.getCol());
                if (upNeighbor != null) {
                    nodes.get(cell.getId()).addEdge(new GraphEdge(
                        cell.getId(), upNeighbor, 10, GraphEdge.EdgeType.RAMP
                    ));
                }
            }

            // Ramp down: connect F2 ramp-down to F1 ramp-up/both
            if (type == CellType.RAMP_DOWN || type == CellType.RAMP_BOTH) {
                String downNeighbor = findCellAt(cells, f - 1, cell.getRow(), cell.getCol());
                if (downNeighbor != null) {
                    nodes.get(cell.getId()).addEdge(new GraphEdge(
                        cell.getId(), downNeighbor, 10, GraphEdge.EdgeType.RAMP
                    ));
                }
            }

            // Elevator: connect to same row/col on all adjacent floors
            if (type == CellType.ELEVATOR) {
                for (int targetFloor = 1; targetFloor <= allFloors.length; targetFloor++) {
                    if (targetFloor == f) continue;
                    String elevNeighbor = findCellAt(cells, targetFloor, cell.getRow(), cell.getCol());
                    if (elevNeighbor != null) {
                        nodes.get(cell.getId()).addEdge(new GraphEdge(
                            cell.getId(), elevNeighbor, 15, GraphEdge.EdgeType.ELEVATOR
                        ));
                    }
                }
            }
        }
    }

    private String findCellAt(Map<String, ParkingCell> cells, int floor, int row, int col) {
        for (ParkingCell c : cells.values()) {
            if (c.getFloor() == floor && c.getRow() == row && c.getCol() == col)
                return c.getId();
        }
        return null;
    }

    private int edgeWeight(CellType from, CellType to) {
        if (to == CellType.PARKING_SPOT)  return 2;
        if (to == CellType.ROAD)          return 1;
        if (to == CellType.INTERSECTION)  return 1;
        return 1;
    }

    private CellType symbolToType(String sym) {
        return switch (sym) {
            case "P" -> CellType.PARKING_SPOT;
            case "R" -> CellType.ROAD;
            case "U" -> CellType.RAMP_UP;
            case "D" -> CellType.RAMP_DOWN;
            case "B" -> CellType.RAMP_BOTH;
            case "E" -> CellType.ELEVATOR;
            case "X" -> CellType.INTERSECTION;
            default  -> CellType.WALL;
        };
    }

    private String buildId(int floor, int row, int col, CellType type, int spotCount) {
        return switch (type) {
            case PARKING_SPOT -> String.format("F%d:%s%d",
                floor,
                String.valueOf((char)('A' + (spotCount / 9))),
                (spotCount % 9) + 1);
            case RAMP_UP      -> String.format("F%d:RAMP_UP_%d_%d",   floor, row, col);
            case RAMP_DOWN    -> String.format("F%d:RAMP_DOWN_%d_%d", floor, row, col);
            case RAMP_BOTH    -> String.format("F%d:RAMP_%d_%d",      floor, row, col);
            case ELEVATOR     -> String.format("F%d:ELEV_%d_%d",      floor, row, col);
            case ROAD         -> String.format("F%d:ROAD_%d_%d",      floor, row, col);
            case INTERSECTION -> String.format("F%d:ISEC_%d_%d",      floor, row, col);
            default           -> String.format("F%d:WALL_%d_%d",      floor, row, col);
        };
    }
}
