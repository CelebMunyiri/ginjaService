package com.ginja.claimsservice.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ginja.claimsservice.config.MockData;
import com.ginja.claimsservice.dto.ClaimRequest;
import com.ginja.claimsservice.dto.ClaimResponse;
import com.ginja.claimsservice.model.Claim;
import com.ginja.claimsservice.repository.ClaimRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository repository;

    public ClaimResponse submitClaim(ClaimRequest request) {

        boolean active = MockData.MEMBERS
                .getOrDefault(request.getMemberId(), false);

        double procedureCost = MockData.PROCEDURE_COSTS
                .getOrDefault(request.getProcedureCode(), 20000.0);

        boolean fraud = request.getClaimAmount() > 2 * procedureCost;

        double approvedAmount = 0;
        String status;

        if (!active) {
            status = "REJECTED";
        } else if (request.getClaimAmount() > MockData.MAX_BENEFIT) {
            status = "PARTIAL";
            approvedAmount = MockData.MAX_BENEFIT;
        } else {
            status = "APPROVED";
            approvedAmount = request.getClaimAmount();
        }

        Claim claim = Claim.builder()
                .memberId(request.getMemberId())
                .providerId(request.getProviderId())
                .diagnosisCode(request.getDiagnosisCode())
                .procedureCode(request.getProcedureCode())
                .claimAmount(request.getClaimAmount())
                .approvedAmount(approvedAmount)
                .status(status)
                .fraudFlag(fraud)
                .createdAt(LocalDateTime.now())
                .build();

        Claim saved = repository.save(claim);

        return ClaimResponse.builder()
                .claimId(saved.getId())
                .status(status)
                .fraudFlag(fraud)
                .approvedAmount(approvedAmount)
                .build();
    }

    public Claim getClaim(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
    }
}
