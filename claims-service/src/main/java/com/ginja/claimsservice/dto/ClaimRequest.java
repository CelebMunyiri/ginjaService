package com.ginja.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {

    @NotNull
    private String memberId;

    @NotNull
    private String providerId;

    @NotNull
    private String diagnosisCode;

    @NotNull
    private String procedureCode;

    @NotNull
    private Double claimAmount;
}