package com.smartparking.controllers;

import com.smartparking.service.ReservationService;
import com.smartparking.service.ReservationService.Reservation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public Map<String, Object> getOptimalSchedule() {
        List<Reservation> all = reservationService.getAllReservations();
        List<Reservation> optimal = reservationService.schedule(all);

        List<Reservation> rejected = all.stream()
                .filter(r -> !optimal.contains(r))
                .toList();

        double totalRevenue = optimal.stream().mapToDouble(r -> r.revenue).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("totalReservations", all.size());
        result.put("optimalCount", optimal.size());
        result.put("totalRevenue", totalRevenue);
        result.put("selected", optimal.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("vehicleId", r.vehicleId);
            map.put("arrival", r.arrival.toString());
            map.put("departure", r.departure.toString());
            map.put("type", r.type.toString());
            map.put("revenue", r.revenue);
            map.put("spotId", r.spotId);
            return map;
        }).toList());

        result.put("rejected", rejected.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("vehicleId", r.vehicleId);
            map.put("arrival", r.arrival.toString());
            map.put("departure", r.departure.toString());
            map.put("type", r.type.toString());
            map.put("revenue", r.revenue);
            return map;
        }).toList());

        return result;
    }

    @PostMapping
    public void addReservation(@RequestBody Map<String, String> payload) {
        String vehicleId = payload.get("vehicleId");
        java.time.LocalTime arrival = java.time.LocalTime.parse(payload.get("arrival"));
        java.time.LocalTime departure = java.time.LocalTime.parse(payload.get("departure"));
        com.smartparking.models.VehicleType type = com.smartparking.models.VehicleType.valueOf(payload.get("type").toUpperCase());
        reservationService.addReservation(new Reservation(vehicleId, arrival, departure, type));
    }

    @DeleteMapping
    public void clearReservations() {
        reservationService.clearReservations();
    }
}