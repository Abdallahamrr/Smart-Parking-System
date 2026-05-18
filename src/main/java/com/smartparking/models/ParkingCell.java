package com.smartparking.models;

import java.util.Objects;

public class ParkingCell {
    private String id; // e.g. "F1:A3"
    private int floor;
    private int row;
    private int col;
    private CellType type;
    private boolean occupied;
    private String vehicleId; // null if empty
    private String label; // display label e.g. "A3", "RAMP_UP", "LIFT"
    private boolean reserved;
    private VehicleType maxSize;
    private String zone;

    public ParkingCell(String id, int floor, int row, int col, CellType type) {
        this.id = id;
        this.floor = floor;
        this.row = row;
        this.col = col;
        this.type = type;
        this.occupied = false;
        this.reserved = false;
        this.maxSize = VehicleType.TRUCK;
        this.zone = "Floor " + floor;
        this.label = deriveLabel(type, id);
    }

    private String deriveLabel(CellType type, String id) {
        return switch (type) {
            case PARKING_SPOT -> id.split(":")[1];
            case RAMP_UP -> "R_UP";
            case RAMP_DOWN -> "R_DOWN";
            case RAMP_BOTH -> "R_BOTH";
            case ELEVATOR -> "ELV";
            case ROAD -> "";
            case INTERSECTION -> "+";
            default -> "";
        };
    }

    // Getters & setters
    public String getId() {
        return id;
    }

    public int getFloor() {
        return floor;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public CellType getType() {
        return type;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReserved() {
        return reserved;
    }

    public VehicleType getMaxSize() {
        return maxSize;
    }

    public String getZone() {
        return zone;
    }

    public void setOccupied(boolean o) {
        this.occupied = o;
    }

    public void setVehicleId(String v) {
        this.vehicleId = v;
    }

    public void setReserved(boolean r) {
        this.reserved = r;
    }

    public void setMaxSize(VehicleType s) {
        this.maxSize = s;
    }

    public void setZone(String z) {
        this.zone = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ParkingCell c))
            return false;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
