package com.ginja.claimsservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ginja.claimsservice.dto.ClaimRequest;
import com.ginja.claimsservice.dto.ClaimResponse;
import com.ginja.claimsservice.model.Claim;
import com.ginja.claimsservice.repository.ClaimRepository;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository repository;

    @InjectMocks
    private ClaimService claimService;

    private ClaimRequest validRequest;
    private Claim savedClaim;

    @BeforeEach
    void setUp() {
        validRequest = new ClaimRequest();
        validRequest.setMemberId("M123");
        validRequest.setProviderId("H456");
        validRequest.setDiagnosisCode("D001");
        validRequest.setProcedureCode("P001");
        validRequest.setClaimAmount(30000.0);

        savedClaim = Claim.builder()
                .id("C789")
                .memberId("M123")
                .providerId("H456")
                .diagnosisCode("D001")
                .procedureCode("P001")
                .claimAmount(30000.0)
                .approvedAmount(30000.0)
                .status("APPROVED")
                .fraudFlag(false)
                .build();
    }

    @Test
    void testSubmitClaim_ActiveMember_WithinBenefitLimit_NoFraud() {
        // Arrange
        when(repository.save(any(Claim.class))).thenReturn(savedClaim);

        // Act
        ClaimResponse response = claimService.submitClaim(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("C789", response.getClaimId());
        assertEquals("APPROVED", response.getStatus());
        assertEquals(30000.0, response.getApprovedAmount());
        assertFalse(response.isFraudFlag());
        verify(repository, times(1)).save(any(Claim.class));
    }

    @Test
    void testSubmitClaim_InactiveMember_ShouldReject() {
        // Arrange
        validRequest.setMemberId("M999"); // Inactive member
        Claim rejectedClaim = savedClaim.toBuilder()
                .status("REJECTED")
                .approvedAmount(0.0)
                .build();
        when(repository.save(any(Claim.class))).thenReturn(rejectedClaim);

        // Act
        ClaimResponse response = claimService.submitClaim(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals(0.0, response.getApprovedAmount());
        verify(repository, times(1)).save(any(Claim.class));
    }

    @Test
    void testSubmitClaim_ExceedsBenefitLimit_PartialApproval() {
        // Arrange
        validRequest.setClaimAmount(50000.0); // Exceeds max benefit of 40000
        Claim partialClaim = savedClaim.toBuilder()
                .status("PARTIAL")
                .claimAmount(50000.0)
                .approvedAmount(40000.0)
                .build();
        when(repository.save(any(Claim.class))).thenReturn(partialClaim);

        // Act
        ClaimResponse response = claimService.submitClaim(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("PARTIAL", response.getStatus());
        assertEquals(40000.0, response.getApprovedAmount());
        verify(repository, times(1)).save(any(Claim.class));
    }

    @Test
    void testSubmitClaim_FraudDetection_ExceedsTwiceProcedureCost() {
        // Arrange
        validRequest.setClaimAmount(50000.0); // P001 costs 20000, 50000 > 2*20000
        Claim fraudClaim = savedClaim.toBuilder()
                .fraudFlag(true)
                .claimAmount(50000.0)
                .approvedAmount(40000.0)
                .status("PARTIAL")
                .build();
        when(repository.save(any(Claim.class))).thenReturn(fraudClaim);

        // Act
        ClaimResponse response = claimService.submitClaim(validRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isFraudFlag());
        verify(repository, times(1)).save(any(Claim.class));
    }

    @Test
    void testGetClaim_ValidId_ReturnsClaim() {
        // Arrange
        String claimId = "C789";
        when(repository.findById(claimId)).thenReturn(Optional.of(savedClaim));

        // Act
        Claim result = claimService.getClaim(claimId);

        // Assert
        assertNotNull(result);
        assertEquals(claimId, result.getId());
        assertEquals("M123", result.getMemberId());
        verify(repository, times(1)).findById(claimId);
    }

    @Test
    void testGetClaim_InvalidId_ThrowsException() {
        // Arrange
        String claimId = "INVALID";
        when(repository.findById(claimId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            claimService.getClaim(claimId);
        });
        assertEquals("Claim not found", exception.getMessage());
        verify(repository, times(1)).findById(claimId);
    }

    @Test
    void testGetAllClaims_ReturnsListOfClaims() {
        // Arrange
        Claim claim2 = savedClaim.toBuilder()
                .id("C790")
                .memberId("M456")
                .build();
        List<Claim> claims = Arrays.asList(savedClaim, claim2);
        when(repository.findAll()).thenReturn(claims);

        // Act
        List<Claim> result = claimService.getAllClaims();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetAllClaims_EmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Claim> result = claimService.getAllClaims();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }
}
