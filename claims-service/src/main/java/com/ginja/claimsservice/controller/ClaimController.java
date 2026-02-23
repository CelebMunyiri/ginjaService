package com.ginja.claimsservice.controller;

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

    private final ClaimService service;

    @PostMapping
    public ClaimResponse createClaim(@Valid @RequestBody ClaimRequest request) {
        return service.submitClaim(request);
    }

    @GetMapping("/{id}")
    public Claim getClaim(@PathVariable String id) {
        return service.getClaim(id);
    }
}
