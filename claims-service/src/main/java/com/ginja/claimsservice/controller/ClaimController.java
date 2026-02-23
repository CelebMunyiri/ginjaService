package com.ginja.claimsservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ginja.claimsservice.dto.ClaimRequest;
import com.ginja.claimsservice.dto.ClaimResponse;
import com.ginja.claimsservice.model.Claim;
import com.ginja.claimsservice.service.ClaimService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private static final Logger logger = LoggerFactory.getLogger(ClaimController.class);

    private final ClaimService service;

    @PostMapping
    public ClaimResponse createClaim(@Valid @RequestBody ClaimRequest request) {
        logger.info("Received claim submission request for member: {}", request.getMemberId());
        ClaimResponse response = service.submitClaim(request);
        logger.info("Claim submission completed with status: {}", response.getStatus());
        return response;
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        logger.info("Received request to get all claims");
        return service.getAllClaims();
    }

    @GetMapping("/{id}")
    public Claim getClaim(@PathVariable String id) {
        logger.info("Received request to get claim with ID: {}", id);
        return service.getClaim(id);
    }
}
