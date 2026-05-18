package com.smartparking.controllers;

import com.smartparking.models.ParkingCell;
import com.smartparking.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/floor/{floorNum}")
    public List<ParkingCell> getFloor(@PathVariable int floorNum) {
        return parkingService.getCellsByFloor(floorNum);
    }

    @PostMapping("/occupy")
    public ResponseEntity<?> occupy(@RequestBody Map<String, String> body) {
        boolean ok = parkingService.occupySpot(body.get("spotId"), body.get("vehicleId"));
        return ok ? ResponseEntity.ok(Map.of("success", true))
                  : ResponseEntity.badRequest().body(Map.of("success", false, "reason", "Spot unavailable"));
    }

    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody Map<String, String> body) {
        boolean ok = parkingService.releaseSpot(body.get("spotId"));
        return ok ? ResponseEntity.ok(Map.of("success", true))
                  : ResponseEntity.badRequest().body(Map.of("success", false));
    }

    @GetMapping("/find/{vehicleId}")
    public ResponseEntity<?> findVehicle(@PathVariable String vehicleId) {
        return parkingService.findVehicle(vehicleId)
            .map(cell -> ResponseEntity.ok((Object) cell))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available")
    public List<ParkingCell> getAvailable() {
        return parkingService.getAvailableSpots();
    }
}