package com.GGT.gmailJobTracker.service;

import com.GGT.gmailJobTracker.Repositories.JobApplicationRepository;
import com.GGT.gmailJobTracker.entities.JobApplication;
import com.google.api.services.gmail.model.Message;
import com.google.common.hash.Hashing;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final EmailParserService emailParser;

    public JobApplicationService(JobApplicationRepository repository, EmailParserService emailParser) {
        this.repository = repository;
        this.emailParser = emailParser;
    }

    @Transactional
    public JobApplication processAndSaveEmail(Message email) {
        // Step 1: Parse email
        String aiResponse = emailParser.parseEmailToJson(email);

        // Step 2: Convert JSON to Entity
        JobApplication jobApp = EmailParserService.parseJsonToEntity(aiResponse);
        if (jobApp == null) return null;

        // Step 3: Check for duplicates
        String emailHash = Hashing.sha256()
                .hashString(email.getSubject() + email.getBody(), StandardCharsets.UTF_8)
                .toString();

        if (repository.existsByEmailHash(emailHash)) {
            throw new DuplicateJobApplicationException("Email already processed");
        }
        jobApp.setEmailHash(emailHash);

        // Step 4: Save to DB
        return repository.save(jobApp);
    }
}