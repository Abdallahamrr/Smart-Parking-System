package com.smartparking.controllers;

import com.smartparking.algorithms.BinPacking;
import com.smartparking.models.ParkingCell;
import com.smartparking.models.Vehicle;
import com.smartparking.models.VehicleType;
import com.smartparking.service.ParkingService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocation")
@CrossOrigin(origins = "*")
public class AllocationController {

    private final BinPacking binPacking;
    private final ParkingService parkingService;

    public AllocationController(BinPacking binPacking, ParkingService parkingService) {
        this.binPacking = binPacking;
        this.parkingService = parkingService;
    }

    @GetMapping("/compare")
    public Map<String, Object> compareAlgorithms(@RequestParam String vehicleType) {
        VehicleType type;
        try {
            type = VehicleType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = VehicleType.STANDARD; // Default fallback
        }
        
        Vehicle vehicle = new Vehicle("TEST-" + System.currentTimeMillis(), type, false, LocalTime.now(), LocalTime.now().plusHours(1));
        
        List<ParkingCell> spots = parkingService.getAvailableSpots();

        Map<String, Object> results = new HashMap<>();

        long start = System.nanoTime();
        ParkingCell ff = binPacking.firstFit(spots, vehicle);
        long ffTime = System.nanoTime() - start;

        start = System.nanoTime();
        ParkingCell bf = binPacking.bestFit(spots, vehicle);
        long bfTime = System.nanoTime() - start;

        start = System.nanoTime();
        ParkingCell ffd = binPacking.firstFitDecreasing(spots, vehicle);
        long ffdTime = System.nanoTime() - start;

        start = System.nanoTime();
        ParkingCell brute = binPacking.bruteForce(spots, vehicle);
        long bruteTime = System.nanoTime() - start;

        results.put("firstFit", Map.of("spot", ff != null ? ff.getId() : "None", "timeNs", ffTime));
        results.put("bestFit", Map.of("spot", bf != null ? bf.getId() : "None", "timeNs", bfTime));
        results.put("firstFitDecreasing", Map.of("spot", ffd != null ? ffd.getId() : "None", "timeNs", ffdTime));
        results.put("bruteForce", Map.of("spot", brute != null ? brute.getId() : "None", "timeNs", bruteTime));

        return results;
    }
}
