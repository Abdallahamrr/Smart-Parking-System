package com.smartparking.algorithms;

import com.smartparking.models.Vehicle;
import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;

@Component
public class DPScheduler {

    public static class Reservation {
        public String vehicleId;
        public LocalTime arrival;
        public LocalTime departure;
        public VehicleType type;

        public Reservation(String vehicleId, LocalTime arrival, LocalTime departure, VehicleType type) {
            this.vehicleId = vehicleId;
            this.arrival = arrival;
            this.departure = departure;
            this.type = type;
        }

        public int arrivalMinutes() {
            return arrival.getHour() * 60 + arrival.getMinute();
        }

        public int departureMinutes() {
            return departure.getHour() * 60 + departure.getMinute();
        }
    }

    // DP Interval Scheduling - maximize number of reservations served
    public List<Reservation> schedule(List<Reservation> reservations) {
        if (reservations.isEmpty()) return new ArrayList<>();

        // Sort by departure time
        List<Reservation> sorted = reservations.stream()
                .sorted(Comparator.comparingInt(Reservation::departureMinutes))
                .toList();

        int n = sorted.size();
        int[] dp = new int[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        Arrays.fill(dp, 1);

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                // No overlap - departure of j before arrival of i
                if (sorted.get(j).departureMinutes() <= sorted.get(i).arrivalMinutes()) {
                    if (dp[j] + 1 > dp[i]) {
                        dp[i] = dp[j] + 1;
                        parent[i] = j;
                    }
                }
            }
        }

        // Find best ending point
        int maxIdx = 0;
        for (int i = 1; i < n; i++) {
            if (dp[i] > dp[maxIdx]) maxIdx = i;
        }

        // Reconstruct selected reservations
        List<Reservation> selected = new ArrayList<>();
        for (int i = maxIdx; i != -1; i = parent[i]) {
            selected.add(0, sorted.get(i));
        }

        return selected;
    }

    // Generate sample reservations for demo
    public List<Reservation> getSampleReservations() {
        List<Reservation> list = new ArrayList<>();
        list.add(new Reservation("R1", LocalTime.of(8, 0), LocalTime.of(10, 0), VehicleType.COMPACT));
        list.add(new Reservation("R2", LocalTime.of(9, 0), LocalTime.of(11, 0), VehicleType.STANDARD));
        list.add(new Reservation("R3", LocalTime.of(10, 0), LocalTime.of(12, 0), VehicleType.SUV));
        list.add(new Reservation("R4", LocalTime.of(11, 0), LocalTime.of(13, 0), VehicleType.COMPACT));
        list.add(new Reservation("R5", LocalTime.of(8, 0), LocalTime.of(9, 0), VehicleType.TRUCK));
        list.add(new Reservation("R6", LocalTime.of(13, 0), LocalTime.of(15, 0), VehicleType.STANDARD));
        list.add(new Reservation("R7", LocalTime.of(9, 30), LocalTime.of(11, 30), VehicleType.COMPACT));
        return list;
    }
}
