package com.smartparking.service;

import com.smartparking.graph.GraphBuilder;
import com.smartparking.graph.ParkingGraph;
import com.smartparking.models.CellType;
import com.smartparking.models.ParkingCell;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParkingService {

    private final GraphBuilder graphBuilder;
    private ParkingGraph graph;

    public ParkingService(GraphBuilder graphBuilder) {
        this.graphBuilder = graphBuilder;
    }

    @PostConstruct
    public void init() {
        graph = graphBuilder.build();
    }

    public ParkingGraph getGraph() { return graph; }

    public List<ParkingCell> getCellsByFloor(int floor) {
        return graph.getAllCells().values().stream()
            .filter(c -> c.getFloor() == floor)
            .collect(Collectors.toList());
    }

    public boolean occupySpot(String spotId, String vehicleId) {
        ParkingCell cell = graph.getCell(spotId);
        if (cell == null || cell.getType() != CellType.PARKING_SPOT || cell.isOccupied())
            return false;
        cell.setOccupied(true);
        cell.setVehicleId(vehicleId);
        return true;
    }

    public boolean releaseSpot(String spotId) {
        ParkingCell cell = graph.getCell(spotId);
        if (cell == null || !cell.isOccupied()) return false;
        cell.setOccupied(false);
        cell.setVehicleId(null);
        return true;
    }

    public Optional<ParkingCell> findVehicle(String vehicleId) {
        return graph.getAllCells().values().stream()
            .filter(c -> vehicleId.equals(c.getVehicleId()))
            .findFirst();
    }

    public List<ParkingCell> getAvailableSpots() {
        return graph.getAllCells().values().stream()
            .filter(c -> c.getType() == CellType.PARKING_SPOT && !c.isOccupied())
            .collect(Collectors.toList());
    }
}