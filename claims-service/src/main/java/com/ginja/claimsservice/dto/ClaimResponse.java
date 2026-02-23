package com.ginja.claimsservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimResponse {

    private String claimId;
    private String status;
    private boolean fraudFlag;
    private double approvedAmount;
}
