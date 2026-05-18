package com.smartparking.controllers;

import com.smartparking.dto.UtilizationDTO;
import com.smartparking.service.UtilizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/utilization")
@CrossOrigin(origins = "*")
public class UtilizationController {

    private final UtilizationService utilizationService;

    public UtilizationController(UtilizationService utilizationService) {
        this.utilizationService = utilizationService;
    }

    @GetMapping
    public UtilizationDTO getUtilization() {
        return utilizationService.getUtilization();
    }
}
