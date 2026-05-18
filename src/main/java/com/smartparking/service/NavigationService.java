package com.smartparking.service;

import com.smartparking.algorithms.Dijkstra;
import com.smartparking.dto.PathResponseDTO;
import com.smartparking.models.ParkingCell;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NavigationService {

    private final ParkingService parkingService;
    private final Dijkstra dijkstra;

    public NavigationService(ParkingService parkingService, Dijkstra dijkstra) {
        this.parkingService = parkingService;
        this.dijkstra = dijkstra;
    }

    public PathResponseDTO navigate(String fromId, String toId) {
        List<String> path = dijkstra.findShortestPath(
            parkingService.getGraph(), fromId, toId
        );

        if (path.isEmpty()) {
            return new PathResponseDTO(false, fromId, toId, List.of(), 0);
        }

        // Enrich path with cell metadata
        List<PathResponseDTO.PathStep> steps = path.stream().map(id -> {
            ParkingCell cell = parkingService.getGraph().getCell(id);
            return new PathResponseDTO.PathStep(
                id,
                cell.getFloor(),
                cell.getRow(),
                cell.getCol(),
                cell.getType().name(),
                cell.getLabel()
            );
        }).collect(Collectors.toList());

        int totalCost = path.size();  // simplified; can sum edge weights
        return new PathResponseDTO(true, fromId, toId, steps, totalCost);
    }
}