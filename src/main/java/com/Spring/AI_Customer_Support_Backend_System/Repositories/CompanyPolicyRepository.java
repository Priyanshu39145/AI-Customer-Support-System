package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.CompanyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyPolicyRepository extends JpaRepository<CompanyPolicy, String> {

    boolean existsByFileHash(String fileHash);

    Optional<CompanyPolicy> findTopByFileNameOrderByVersionDesc(String fileName);
}
//Done
