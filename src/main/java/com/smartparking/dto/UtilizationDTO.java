package com.smartparking.dto;

import java.util.List;
import java.util.Map;

public class UtilizationDTO {
    private int totalSpots;
    private int occupiedSpots;
    private double overallFillPercent;
    private Map<Integer, Double> floorFill;
    private Map<String, Double> zoneFill;
    private Map<String, Integer> vehicleTypeBreakdown;
    private List<TimeSeriesPoint> timeSeries;

    public UtilizationDTO() {}

    public int getTotalSpots() { return totalSpots; }
    public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }

    public int getOccupiedSpots() { return occupiedSpots; }
    public void setOccupiedSpots(int occupiedSpots) { this.occupiedSpots = occupiedSpots; }

    public double getOverallFillPercent() { return overallFillPercent; }
    public void setOverallFillPercent(double overallFillPercent) { this.overallFillPercent = overallFillPercent; }

    public Map<Integer, Double> getFloorFill() { return floorFill; }
    public void setFloorFill(Map<Integer, Double> floorFill) { this.floorFill = floorFill; }

    public Map<String, Double> getZoneFill() { return zoneFill; }
    public void setZoneFill(Map<String, Double> zoneFill) { this.zoneFill = zoneFill; }

    public Map<String, Integer> getVehicleTypeBreakdown() { return vehicleTypeBreakdown; }
    public void setVehicleTypeBreakdown(Map<String, Integer> vehicleTypeBreakdown) { this.vehicleTypeBreakdown = vehicleTypeBreakdown; }

    public List<TimeSeriesPoint> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<TimeSeriesPoint> timeSeries) { this.timeSeries = timeSeries; }

    public static class TimeSeriesPoint {
        public String time;
        public int occupancy;

        public TimeSeriesPoint(String time, int occupancy) {
            this.time = time;
            this.occupancy = occupancy;
        }
    }
}
