package com.smartparking.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParkingSpot {
    private String id;
    private int floor;
    private String zone;
    private VehicleType maxSize;
    private boolean occupied;
    private boolean reserved;
    private boolean accessible;
    private Vehicle currentVehicle;
}