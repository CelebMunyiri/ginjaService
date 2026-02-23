package com.ginja.claimsservice.repository;

import com.ginja.claimsservice.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, String> {
}
