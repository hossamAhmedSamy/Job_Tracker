package com.GGT.gmailJobTracker.Repositories;

import com.GGT.gmailJobTracker.entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Custom query example: Find by company name
    List<JobApplication> findByCompanyName(String companyName);

    // Check if email already exists
    boolean existsByEmailHash(String emailHash);
}