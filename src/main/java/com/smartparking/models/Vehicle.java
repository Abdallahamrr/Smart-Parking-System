package com.smartparking.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class Vehicle {
    private String id;
    private VehicleType type;
    private boolean reserved;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
}
