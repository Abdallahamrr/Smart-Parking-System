package com.smartparking.controllers;

import com.smartparking.algorithms.Dijkstra.TravelerMode;
import com.smartparking.dto.PathResponseDTO;
import com.smartparking.service.NavigationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    /**
     * GET /api/navigation/path?from=F1:ROAD_3_3&to=F3:A1&mode=CAR
     *
     * mode = CAR  → uses ramps only
     * mode = FOOT → uses elevators only (default if omitted)
     */
    @GetMapping("/path")
    public PathResponseDTO getPath(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "FOOT") String mode
    ) {
        TravelerMode travelerMode;
        try {
            travelerMode = TravelerMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            travelerMode = TravelerMode.FOOT; // safe fallback
        }
        return navigationService.navigate(from, to, travelerMode);
    }
}