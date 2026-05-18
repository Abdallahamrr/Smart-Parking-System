package com.smartparking.service;

import com.smartparking.dto.UtilizationDTO;
import com.smartparking.dto.UtilizationDTO.TimeSeriesPoint;
import com.smartparking.models.CellType;
import com.smartparking.models.ParkingCell;
import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UtilizationService {

    private final ParkingService parkingService;

    public UtilizationService(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    public UtilizationDTO getUtilization() {
        List<ParkingCell> allCells = parkingService.getGraph().getAllCells().values().stream()
                .filter(c -> c.getType() == CellType.PARKING_SPOT)
                .collect(Collectors.toList());

        int totalSpots = allCells.size();
        int occupiedSpots = (int) allCells.stream().filter(ParkingCell::isOccupied).count();
        double overallFillPercent = totalSpots == 0 ? 0 : Math.round(((double) occupiedSpots / totalSpots) * 1000) / 10.0;

        // Floor fill %
        Map<Integer, List<ParkingCell>> byFloor = allCells.stream().collect(Collectors.groupingBy(ParkingCell::getFloor));
        Map<Integer, Double> floorFill = new HashMap<>();
        byFloor.forEach((floor, cells) -> {
            long occ = cells.stream().filter(ParkingCell::isOccupied).count();
            floorFill.put(floor, cells.isEmpty() ? 0 : Math.round(((double) occ / cells.size()) * 1000) / 10.0);
        });

        // Zone fill %
        Map<String, List<ParkingCell>> byZone = allCells.stream().collect(Collectors.groupingBy(ParkingCell::getZone));
        Map<String, Double> zoneFill = new HashMap<>();
        byZone.forEach((zone, cells) -> {
            long occ = cells.stream().filter(ParkingCell::isOccupied).count();
            zoneFill.put(zone, cells.isEmpty() ? 0 : Math.round(((double) occ / cells.size()) * 1000) / 10.0);
        });

        // Vehicle type breakdown
        Map<String, Integer> vehicleTypeBreakdown = new HashMap<>();
        allCells.stream().filter(ParkingCell::isOccupied).forEach(cell -> {
            String type = cell.getMaxSize() != null ? cell.getMaxSize().name() : "UNKNOWN";
            vehicleTypeBreakdown.put(type, vehicleTypeBreakdown.getOrDefault(type, 0) + 1);
        });

        // Available designated spots by exact size
        Map<String, Integer> availableSpotsByDesignatedType = new HashMap<>();
        for (VehicleType vt : VehicleType.values()) {
            availableSpotsByDesignatedType.put(vt.name(), 0);
        }
        allCells.stream()
                .filter(cell -> !cell.isOccupied() && !cell.isReserved())
                .forEach(cell -> {
                    String type = cell.getMaxSize() != null ? cell.getMaxSize().name() : "UNKNOWN";
                    availableSpotsByDesignatedType.put(type, availableSpotsByDesignatedType.getOrDefault(type, 0) + 1);
                });

        // Available fitting spots per vehicle type (based on size values)
        Map<String, Integer> availableSpotsByFittingVehicleType = new HashMap<>();
        for (VehicleType vt : VehicleType.values()) {
            int vtValue = sizeValue(vt);
            long count = allCells.stream()
                    .filter(cell -> !cell.isOccupied() && !cell.isReserved())
                    .filter(cell -> cell.getMaxSize() != null && sizeValue(cell.getMaxSize()) >= vtValue)
                    .count();
            availableSpotsByFittingVehicleType.put(vt.name(), (int) count);
        }

        // Generate dummy time-series data ending at the current occupancy
        List<TimeSeriesPoint> timeSeries = new ArrayList<>();
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        int baseOccupancy = occupiedSpots;
        
        // Let's create 12 points, 1 hour apart
        for (int i = 11; i >= 0; i--) {
            LocalTime t = now.minusHours(i);
            int pastOcc = Math.max(0, Math.min(totalSpots, baseOccupancy + (int)(Math.random() * 20 - 10)));
            if (i == 0) pastOcc = baseOccupancy; // current
            timeSeries.add(new TimeSeriesPoint(t.format(formatter), pastOcc));
        }

        UtilizationDTO dto = new UtilizationDTO();
        dto.setTotalSpots(totalSpots);
        dto.setOccupiedSpots(occupiedSpots);
        dto.setOverallFillPercent(overallFillPercent);
        dto.setFloorFill(floorFill);
        dto.setZoneFill(zoneFill);
        dto.setVehicleTypeBreakdown(vehicleTypeBreakdown);
        dto.setAvailableSpotsByDesignatedType(availableSpotsByDesignatedType);
        dto.setAvailableSpotsByFittingVehicleType(availableSpotsByFittingVehicleType);
        dto.setTimeSeries(timeSeries);

        return dto;
    }

    private int sizeValue(VehicleType type) {
        return switch (type) {
            case COMPACT -> 1;
            case STANDARD -> 2;
            case SUV -> 3;
            case TRUCK -> 4;
        };
    }
}
