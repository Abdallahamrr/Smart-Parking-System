package com.smartparking.controllers;

import com.smartparking.algorithms.Dijkstra;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/navigate")
@CrossOrigin(origins = "*")
public class NavigationController {

    private final Dijkstra dijkstra;

    public NavigationController(Dijkstra dijkstra) {
        this.dijkstra = dijkstra;
    }

    @GetMapping("/{zone}")
    public Map<String, Object> navigate(@PathVariable String zone) {
        return dijkstra.findPath(zone);
    }
}