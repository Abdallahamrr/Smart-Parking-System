package com.smartparking.algorithms;

import com.smartparking.models.ParkingSpot;
import com.smartparking.models.Vehicle;
import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinPacking {

    // First Fit — assign to first available spot that fits
    public ParkingSpot firstFit(List<ParkingSpot> spots, Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && !spot.isReserved() && fits(spot, vehicle)) {
                return spot;
            }
        }
        return null;
    }

    // Best Fit — assign to the fullest zone that still fits
    public ParkingSpot bestFit(List<ParkingSpot> spots, Vehicle vehicle) {
        ParkingSpot best = null;
        long bestOccupied = -1;

        for (ParkingSpot spot : spots) {
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

    // First Fit Decreasing — sort by size descending then apply first fit
    public ParkingSpot firstFitDecreasing(List<ParkingSpot> spots, Vehicle vehicle) {
        List<ParkingSpot> sorted = spots.stream()
                .filter(s -> !s.isOccupied() && !s.isReserved())
                .sorted((a, b) -> sizeValue(b.getMaxSize()) - sizeValue(a.getMaxSize()))
                .toList();
        return firstFit(sorted, vehicle);
    }

    // Brute Force — try all spots and return the one with least wasted space
    public ParkingSpot bruteForce(List<ParkingSpot> spots, Vehicle vehicle) {
        ParkingSpot best = null;
        int minWaste = Integer.MAX_VALUE;

        for (ParkingSpot spot : spots) {
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

    private boolean fits(ParkingSpot spot, Vehicle vehicle) {
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