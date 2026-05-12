package com.smartparking.core;

import com.smartparking.models.ParkingSpot;
import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParkingLotState {
    

    private final List<ParkingSpot> spots = new ArrayList<>();
    private int totalVehicles = 0;
    private int totalRejections = 0;

    public ParkingLotState() {
        initializeLot();
    }

    private void initializeLot() {
        // Floor 1 - Zone A (Compact spots)
        for (int i = 1; i <= 5; i++) {
            spots.add(new ParkingSpot("F1-A" + i, 1, "A", VehicleType.COMPACT, false, false, false, null));
        }
        // Floor 1 - Zone B (Standard spots)
        for (int i = 1; i <= 5; i++) {
            spots.add(new ParkingSpot("F1-B" + i, 1, "B", VehicleType.STANDARD, false, false, false, null));
        }
        // Floor 2 - Zone C (SUV spots)
        for (int i = 1; i <= 5; i++) {
            spots.add(new ParkingSpot("F2-C" + i, 2, "C", VehicleType.SUV, false, false, false, null));
        }
        // Floor 2 - Zone D (Accessible spots)
        for (int i = 1; i <= 5; i++) {
            spots.add(new ParkingSpot("F2-D" + i, 2, "D", VehicleType.STANDARD, false, false, true, null));
        }
        // Floor 3 - Zone E (Truck spots)
        for (int i = 1; i <= 5; i++) {
            spots.add(new ParkingSpot("F3-E" + i, 3, "E", VehicleType.TRUCK, false, false, false, null));
        }
    }

    public List<ParkingSpot> getAllSpots() {
        return spots;
    }

    public List<ParkingSpot> getAvailableSpots() {
        return spots.stream()
                .filter(s -> !s.isOccupied() && !s.isReserved())
                .toList();
    }

    public ParkingSpot getSpotById(String id) {
        return spots.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public void incrementVehicles() { totalVehicles++; }
    public void incrementRejections() { totalRejections++; }
    public int getTotalVehicles() { return totalVehicles; }
    public int getTotalRejections() { return totalRejections; }

    public void resetStats() {
        totalVehicles = 0;
        totalRejections = 0;
    }
}