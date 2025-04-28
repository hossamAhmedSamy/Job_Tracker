package com.GGT.gmailJobTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service  // Make it a Spring-managed bean
public class EmailProcessor {

    @Autowired
    private EmailFilter emailFilter; // Assuming you already have the filter component

    @Autowired
    private GmailServiceBuilder gmailServiceBuilder; // Assuming this is your Gmail service

    @Autowired
    private RestTemplate restTemplate; // To make HTTP requests

    @Value("${deepseek.api.url}") // Store in application.properties
    private String deepSeekApiUrl;

    @Value("${deepseek.api.key}") // Store in application.properties
    private String deepSeekApiKey;


    public void processFilteredEmails() {
        try {
            System.out.println("process filter started");
            Gmail service = gmailServiceBuilder.getGmailService();

            // Fetch the latest emails from Gmail
            ListMessagesResponse messagesResponse = service.users().messages().list("me")
                    .setMaxResults(10L)
                    .execute();

            for (Message m : messagesResponse.getMessages()) {
                Message fullMessage = service.users().messages().get("me", m.getId()).execute();

                // Extract subject and body of the email
                String subject = emailFilter.getSubject(fullMessage);
                String body = emailFilter.getEmailBody(fullMessage);

                // Log to check values
                System.out.println("Subject: " + subject);
                System.out.println("Body: " + body);

                // Filter emails based on keywords
                if (emailFilter.isJobApplicationEmail(fullMessage)) {
                    System.out.println("Job application email detected.");

                    // Construct the DeepSeek prompt
                    String prompt = constructPrompt(subject, body);

                    // Log the constructed prompt
                    System.out.println("Prompt for DeepSeek: " + prompt);

                    // Call DeepSeek with the prompt
                    String deepSeekResponse = callDeepSeek(prompt);

                    // Log the response from DeepSeek
                    System.out.println("DeepSeek Response: " + deepSeekResponse);

                    // Process the response from DeepSeek
                    processDeepSeekResponse(deepSeekResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String callDeepSeek(String prompt) {
        try {
            System.out.println("Calling DeepSeek API...");  // Log API call initiation

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + deepSeekApiKey);
            headers.set("Content-Type", "application/json");

            // Construct the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = Collections.singletonList(
                    Map.of("role", "user", "content", prompt)
            );
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(deepSeekApiUrl, HttpMethod.POST, request, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // Extract AI response from JSON
            String aiResponse = rootNode.path("choices").get(0).path("message").path("content").asText();

            // Log the response content
            System.out.println("DeepSeek API Response: " + aiResponse);

            return aiResponse;
        } catch (Exception e) {
            e.printStackTrace();  // Log error
            return "Error: Unable to process request.";
        }
    }

    private String constructPrompt(String subject, String body) {
        return "Analyze this email and perform the following steps:\n\n" +
                "1. FIRST, determine if this is a job application email by checking for keywords like:\n" +
                "   - 'application', 'interview', 'offer', 'rejection', 'hiring', 'position', 'role', 'resume', 'apply'\n" +
                "   - If NO keywords are found, return: {\"isJobApplication\": false}\n\n" +
                "2. IF it is a job application email, extract the following details as JSON:\n" +
                "   - Company name\n" +
                "   - Job title/position\n" +
                "   - Application status (one of: APPLIED, INTERVIEW_SCHEDULED, TECHNICAL_ASSESSMENT, REJECTED, OFFER_RECEIVED, NO_RESPONSE)\n" +
                "   - Application date (if mentioned, format: YYYY-MM-DD)\n" +
                "   - Important dates (e.g., interview date, deadline)\n" +
                "   - Key details (salary, location, etc.)\n\n" +
                "FORMAT (if job-related):\n" +
                "{\n" +
                "  \"isJobApplication\": true,\n" +
                "  \"companyName\": \"string\",\n" +
                "  \"jobTitle\": \"string\",\n" +
                "  \"status\": \"string\",\n" +
                "  \"applicationDate\": \"YYYY-MM-DD\",\n" +
                "  \"importantDates\": {\"event\": \"YYYY-MM-DD\"},\n" +
                "  \"keyDetails\": {\"key\": \"value\"}\n" +
                "}\n\n" +
                "EXAMPLE REJECTION (if not job-related):\n" +
                "{\"isJobApplication\": false}\n\n" +
                "Email Subject: " + subject + "\n\n" +
                "Email Body:\n" + body;
    }

    private void processDeepSeekResponse(String deepSeekResponse) {
        // Here we would parse the DeepSeek response and decide if we should save it to the database
        // Use your existing logic to map it into a JobApplication entity and save it
        System.out.println("DeepSeek Response: " + deepSeekResponse);
    }
}
