package com.smartparking.controllers;

import com.smartparking.algorithms.DPScheduler;
import com.smartparking.algorithms.DPScheduler.Reservation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final DPScheduler dpScheduler;

    public ReservationController(DPScheduler dpScheduler) {
        this.dpScheduler = dpScheduler;
    }

    @GetMapping
    public Map<String, Object> getOptimalSchedule() {
        List<Reservation> all = dpScheduler.getSampleReservations();
        List<Reservation> optimal = dpScheduler.schedule(all);

        Map<String, Object> result = new HashMap<>();
        result.put("totalReservations", all.size());
        result.put("optimalCount", optimal.size());
        result.put("selected", optimal.stream().map(r -> {
            Map<String, String> map = new HashMap<>();
            map.put("vehicleId", r.vehicleId);
            map.put("arrival", r.arrival.toString());
            map.put("departure", r.departure.toString());
            map.put("type", r.type.toString());
            return map;
        }).toList());

        return result;
    }
}