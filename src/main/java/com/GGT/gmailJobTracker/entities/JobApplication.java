package com.GGT.gmailJobTracker.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String status; // (APPLIED, INTERVIEW, REJECTED, etc.)

    private LocalDate applicationDate;

    @ElementCollection
    @CollectionTable(name = "important_dates", joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "event_name")
    @Column(name = "event_date")
    private Map<String, LocalDate> importantDates;

    @ElementCollection
    @CollectionTable(name = "key_details", joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value")
    private Map<String, String> keyDetails;

    public JobApplication() {

    }

    // Getters & Setters

    public Map<String, String> getKeyDetails() {
        return keyDetails;
    }

    public void setKeyDetails(Map<String, String> keyDetails) {
        this.keyDetails = keyDetails;
    }

    public Map<String, LocalDate> getImportantDates() {
        return importantDates;
    }

    public void setImportantDates(Map<String, LocalDate> importantDates) {
        this.importantDates = importantDates;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public JobApplication(Long id, String companyName, String jobTitle, String status, LocalDate applicationDate, Map<String, LocalDate> importantDates, Map<String, String> keyDetails) {
        this.id = id;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.status = status;
        this.applicationDate = applicationDate;
        this.importantDates = importantDates;
        this.keyDetails = keyDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    // Constructors
}

