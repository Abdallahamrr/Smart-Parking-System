package com.smartparking.service;

import com.smartparking.models.VehicleType;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import com.smartparking.models.ParkingCell;
import com.smartparking.models.CellType;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ReservationService {

    private final ParkingService parkingService;

    @Autowired
    public ReservationService(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    public static class Reservation {
        public String vehicleId;
        public LocalTime arrival;
        public LocalTime departure;
        public VehicleType type;
        public double revenue;
        public String spotId;

        public Reservation(String vehicleId, LocalTime arrival, LocalTime departure, VehicleType type) {
            this.vehicleId = vehicleId;
            this.arrival = arrival;
            this.departure = departure;
            this.type = type;
            this.revenue = calculateRevenue();
        }

        private double calculateRevenue() {
            double durationHours = (departureMinutes() - arrivalMinutes()) / 60.0;
            double rate = switch (type) {
                case COMPACT -> 10.0;
                case STANDARD -> 15.0;
                case SUV -> 20.0;
                case TRUCK -> 25.0;
                default -> 10.0;
            };
            return durationHours * rate;
        }

        public int arrivalMinutes() {
            return arrival.getHour() * 60 + arrival.getMinute();
        }

        public int departureMinutes() {
            return departure.getHour() * 60 + departure.getMinute();
        }
    }

    private int sizeValue(VehicleType type) {
        return switch (type) {
            case COMPACT -> 1;
            case STANDARD -> 2;
            case SUV -> 3;
            case TRUCK -> 4;
        };
    }

    public List<Reservation> schedule(List<Reservation> reservations) {
        if (parkingService != null && parkingService.getGraph() != null) {
            for (ParkingCell cell : parkingService.getGraph().getAllCells().values()) {
                if (cell.getType() == CellType.PARKING_SPOT) {
                    cell.setReserved(false);
                }
            }
        }

        if (reservations.isEmpty())
            return new ArrayList<>();

        Map<VehicleType, List<Reservation>> grouped = new HashMap<>();
        for (Reservation r : reservations) {
            grouped.computeIfAbsent(r.type, k -> new ArrayList<>()).add(r);
        }

        List<Reservation> finalSelected = new ArrayList<>();
        
        List<ParkingCell> allSpots = new ArrayList<>();
        if (parkingService != null && parkingService.getGraph() != null) {
            allSpots = parkingService.getGraph().getAllCells().values().stream()
                .filter(c -> c.getType() == CellType.PARKING_SPOT)
                .toList();
        }

        for (Map.Entry<VehicleType, List<Reservation>> entry : grouped.entrySet()) {
            VehicleType type = entry.getKey();
            List<Reservation> remaining = new ArrayList<>(entry.getValue());

            // Find all spots that can fit this vehicle type AND are not yet reserved
            List<ParkingCell> suitableSpots = allSpots.stream()
                .filter(spot -> !spot.isReserved() && sizeValue(spot.getMaxSize()) >= sizeValue(type))
                .toList();

            for (ParkingCell spot : suitableSpots) {
                if (remaining.isEmpty()) {
                    break;
                }
                
                // Run DP on remaining reservations of this type
                List<Reservation> selectedForSpot = scheduleSingleSpot(remaining);
                if (selectedForSpot.isEmpty()) {
                    break;
                }

                // Assign this spot to the selected reservations
                for (Reservation r : selectedForSpot) {
                    r.spotId = spot.getId();
                    remaining.remove(r);
                }
                spot.setReserved(true);
                finalSelected.addAll(selectedForSpot);
            }
        }

        return finalSelected;
    }

    private List<Reservation> scheduleSingleSpot(List<Reservation> reservations) {
        if (reservations.isEmpty())
            return new ArrayList<>();

        List<Reservation> sorted = reservations.stream()
                .sorted(Comparator.comparingInt(Reservation::departureMinutes))
                .toList();

        int n = sorted.size();
        double[] dp = new double[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        for (int i = 0; i < n; i++) {
            dp[i] = sorted.get(i).revenue;
        }

        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (sorted.get(j).departureMinutes() <= sorted.get(i).arrivalMinutes()) {
                    if (dp[j] + sorted.get(i).revenue > dp[i]) {
                        dp[i] = dp[j] + sorted.get(i).revenue;
                        parent[i] = j;
                    }
                }
            }
        }

        int maxIdx = 0;
        for (int i = 1; i < n; i++) {
            if (dp[i] > dp[maxIdx])
                maxIdx = i;
        }

        List<Reservation> selected = new ArrayList<>();
        for (int i = maxIdx; i != -1; i = parent[i]) {
            selected.add(0, sorted.get(i));
        }

        return selected;
    }

    private final List<Reservation> allReservations = new ArrayList<>();

    public void addReservation(Reservation reservation) {
        allReservations.add(reservation);
    }

    public List<Reservation> getAllReservations() {
        return allReservations;
    }

    public void clearReservations() {
        allReservations.clear();
    }
}
