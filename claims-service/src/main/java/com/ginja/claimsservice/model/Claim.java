package com.ginja.claimsservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String memberId;
    private String providerId;
    private String diagnosisCode;
    private String procedureCode;

    private double claimAmount;
    private double approvedAmount;

    private String status; // APPROVED | PARTIAL | REJECTED
    private boolean fraudFlag;

    private LocalDateTime createdAt;
}