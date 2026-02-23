package com.ginja.claimsservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Claims Service API");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("status", "running");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /claims", "Create a new claim");
        endpoints.put("GET /claims", "Get all claims");
        endpoints.put("GET /claims/{id}", "Get a claim by ID");
        response.put("endpoints", endpoints);
        
        return response;
    }
}
