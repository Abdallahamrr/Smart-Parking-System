package com.smartparking.controllers;

import com.smartparking.algorithms.BinPacking;
import com.smartparking.core.ParkingLotState;
import com.smartparking.models.ParkingSpot;
import com.smartparking.models.Vehicle;
import com.smartparking.models.VehicleType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AllocationController {

    private final BinPacking binPacking;
    private final ParkingLotState lotState;

    public AllocationController(BinPacking binPacking, ParkingLotState lotState) {
        this.binPacking = binPacking;
        this.lotState = lotState;
    }

    // Get full lot status
    @GetMapping("/status")
    public List<ParkingSpot> getStatus() {
        return lotState.getAllSpots();
    }

    // Allocate a vehicle using all strategies and compare
    @PostMapping("/allocate")
    public Map<String, Object> allocate(@RequestParam String vehicleType,
                                        @RequestParam String strategy) {
        Vehicle vehicle = new Vehicle(
                UUID.randomUUID().toString(),
                VehicleType.valueOf(vehicleType.toUpperCase()),
                false,
                LocalTime.now(),
                LocalTime.now().plusHours(2)
        );

        List<ParkingSpot> spots = lotState.getAllSpots();
        ParkingSpot assigned = null;
        long startTime = System.nanoTime();

        assigned = switch (strategy.toLowerCase()) {
            case "bestfit" -> binPacking.bestFit(spots, vehicle);
            case "ffd" -> binPacking.firstFitDecreasing(spots, vehicle);
            case "bruteforce" -> binPacking.bruteForce(spots, vehicle);
            default -> binPacking.firstFit(spots, vehicle);
        };

        long duration = System.nanoTime() - startTime;

        Map<String, Object> result = new HashMap<>();
        lotState.incrementVehicles();
        if (assigned != null) {
        assigned.setOccupied(true);
        assigned.setCurrentVehicle(vehicle);
        result.put("success", true);
        result.put("spot", assigned);
        result.put("vehicle", vehicle);
        result.put("strategy", strategy);
        result.put("executionTimeNs", duration);
        } else {
        lotState.incrementRejections();
        result.put("success", false);
        result.put("message", "No available spot for vehicle type: " + vehicleType);
}

        return result;
    }

    // Compare all strategies at once
    @GetMapping("/compare")
    public Map<String, Object> compare(@RequestParam String vehicleType) {
        Vehicle vehicle = new Vehicle(
                UUID.randomUUID().toString(),
                VehicleType.valueOf(vehicleType.toUpperCase()),
                false,
                LocalTime.now(),
                LocalTime.now().plusHours(2)
        );

        List<ParkingSpot> spots = lotState.getAllSpots();
        Map<String, Object> result = new HashMap<>();

        String[] strategies = {"firstfit", "bestfit", "ffd", "bruteforce"};
        for (String strategy : strategies) {
            long start = System.nanoTime();
            ParkingSpot spot = switch (strategy) {
                case "bestfit" -> binPacking.bestFit(spots, vehicle);
                case "ffd" -> binPacking.firstFitDecreasing(spots, vehicle);
                case "bruteforce" -> binPacking.bruteForce(spots, vehicle);
                default -> binPacking.firstFit(spots, vehicle);
            };
            long duration = System.nanoTime() - start;

            Map<String, Object> strategyResult = new HashMap<>();
            strategyResult.put("spotFound", spot != null);
            strategyResult.put("spotId", spot != null ? spot.getId() : "none");
            strategyResult.put("executionTimeNs", duration);
            result.put(strategy, strategyResult);
        }

        return result;
    }

    // Release a spot
    @PostMapping("/release")
    public Map<String, Object> release(@RequestParam String spotId) {
        ParkingSpot spot = lotState.getSpotById(spotId);
        Map<String, Object> result = new HashMap<>();
        if (spot != null) {
            spot.setOccupied(false);
            spot.setCurrentVehicle(null);
            result.put("success", true);
            result.put("message", "Spot " + spotId + " released");
        } else {
            result.put("success", false);
            result.put("message", "Spot not found");
        }
        return result;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
    lotState.getAllSpots().forEach(spot -> {
        spot.setOccupied(false);
        spot.setReserved(false);
        spot.setCurrentVehicle(null);
        });
    lotState.resetStats();
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "Lot reset successfully");
    return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
    Map<String, Object> result = new HashMap<>();
    result.put("totalVehicles", lotState.getTotalVehicles());
    result.put("totalRejections", lotState.getTotalRejections());
    result.put("totalServed", lotState.getTotalVehicles() - lotState.getTotalRejections());
    result.put("rejectionRate", lotState.getTotalVehicles() == 0 ? 0 :
            (double) lotState.getTotalRejections() / lotState.getTotalVehicles() * 100);
    return result;
    }
}