package com.smartparking.algorithms;

import com.smartparking.models.ParkingCell;
import com.smartparking.models.Vehicle;
import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinPacking {

    // First Fit - assign to first available spot that fits
    public ParkingCell firstFit(List<ParkingCell> spots, Vehicle vehicle) {
        for (ParkingCell spot : spots) {
            if (!spot.isOccupied() && !spot.isReserved() && fits(spot, vehicle)) {
                return spot;
            }
        }
        return null;
    }

    // Best Fit - assign to the fullest zone that still fits
    public ParkingCell bestFit(List<ParkingCell> spots, Vehicle vehicle) {
        ParkingCell best = null;
        long bestOccupied = -1;

        for (ParkingCell spot : spots) {
            if (!spot.isOccupied() && !spot.isReserved() && fits(spot, vehicle)) {
                long zoneOccupied = spots.stream()
                        .filter(s -> s.getZone().equals(spot.getZone()) && s.isOccupied())
                        .count();
                if (zoneOccupied > bestOccupied) {
                    bestOccupied = zoneOccupied;
                    best = spot;
                }
            }
        }
        return best;
    }

    // First Fit Decreasing - sort by size descending then apply first fit
    public ParkingCell firstFitDecreasing(List<ParkingCell> spots, Vehicle vehicle) {
        List<ParkingCell> sorted = spots.stream()
                .filter(s -> !s.isOccupied() && !s.isReserved())
                .sorted((a, b) -> sizeValue(b.getMaxSize()) - sizeValue(a.getMaxSize()))
                .toList();
        return firstFit(sorted, vehicle);
    }

    // Brute Force - try all spots and return the one with least wasted space
    public ParkingCell bruteForce(List<ParkingCell> spots, Vehicle vehicle) {
        ParkingCell best = null;
        int minWaste = Integer.MAX_VALUE;

        for (ParkingCell spot : spots) {
            if (!spot.isOccupied() && !spot.isReserved() && fits(spot, vehicle)) {
                int waste = sizeValue(spot.getMaxSize()) - sizeValue(vehicle.getType());
                if (waste < minWaste) {
                    minWaste = waste;
                    best = spot;
                }
            }
        }
        return best;
    }

    private boolean fits(ParkingCell spot, Vehicle vehicle) {
        return sizeValue(spot.getMaxSize()) >= sizeValue(vehicle.getType());
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
