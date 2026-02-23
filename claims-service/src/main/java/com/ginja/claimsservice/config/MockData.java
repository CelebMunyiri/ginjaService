package com.ginja.claimsservice.config;

import java.util.Map;

public class MockData {

    public static final Map<String, Boolean> MEMBERS = Map.of(
        "M123", true,
        "M999", false
    );

    public static final Map<String, Double> PROCEDURE_COSTS = Map.of(
        "P001", 20000.0,
        "P002", 15000.0
    );

    public static final double MAX_BENEFIT = 40000.0;
}