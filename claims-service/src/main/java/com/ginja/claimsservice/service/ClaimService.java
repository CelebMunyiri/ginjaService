package com.ginja.claimsservice.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository repository;

    public ClaimResponse submitClaim(ClaimRequest request) {
        logger.info("Processing claim submission for member: {}, amount: {}", 
                request.getMemberId(), request.getClaimAmount());

        boolean active = MockData.MEMBERS
                .getOrDefault(request.getMemberId(), false);

        double procedureCost = MockData.PROCEDURE_COSTS
                .getOrDefault(request.getProcedureCode(), 20000.0);

        boolean fraud = request.getClaimAmount() > 2 * procedureCost;
        
        if (fraud) {
            logger.warn("Potential fraud detected for member: {}, claim amount: {} exceeds 2x procedure cost: {}",
                    request.getMemberId(), request.getClaimAmount(), procedureCost);
        }

        double approvedAmount = 0;
        String status;

        if (!active) {
            status = "REJECTED";
            logger.info("Claim rejected - member {} is not active", request.getMemberId());
        } else if (request.getClaimAmount() > MockData.MAX_BENEFIT) {
            status = "PARTIAL";
            approvedAmount = MockData.MAX_BENEFIT;
            logger.info("Claim partially approved for member {} - amount capped at max benefit: {}",
                    request.getMemberId(), MockData.MAX_BENEFIT);
        } else {
            status = "APPROVED";
            approvedAmount = request.getClaimAmount();
            logger.info("Claim approved for member {} - full amount: {}",
                    request.getMemberId(), request.getClaimAmount());
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
        logger.info("Claim saved successfully with ID: {}", saved.getId());

        return ClaimResponse.builder()
                .claimId(saved.getId())
                .status(status)
                .fraudFlag(fraud)
                .approvedAmount(approvedAmount)
                .build();
    }

    public Claim getClaim(String id) {
        logger.info("Retrieving claim with ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Claim not found with ID: {}", id);
                    return new RuntimeException("Claim not found");
                });
    }

    public java.util.List<Claim> getAllClaims() {
        logger.info("Retrieving all claims");
        java.util.List<Claim> claims = repository.findAll();
        logger.info("Retrieved {} claims", claims.size());
        return claims;
    }
}
