package com.smartparking.controllers;

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

    @GetMapping("/path")
    public PathResponseDTO getPath(
        @RequestParam String from,
        @RequestParam String to
    ) {
        return navigationService.navigate(from, to);
    }
}