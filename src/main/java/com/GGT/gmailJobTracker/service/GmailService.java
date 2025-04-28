package com.GGT.gmailJobTracker.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GmailService {


    @Value("${deepseek.api.url}") // Store in application.properties
    private String deepSeekApiUrl;

    @Value("${deepseek.api.key}") // Store in application.properties
    private String deepSeekApiKey;

    @Autowired
    private EmailFilter emailFilter;

    private final RestTemplate restTemplate = new RestTemplate();



    private static final List<String> JOB_KEYWORDS = Arrays.asList(
            "application received", "interview scheduled", "offer extended", "rejection"
    );
    public boolean isJobApplicationEmail(String subject) {
        if (subject == null) return false;

        subject = subject.toLowerCase();

        return subject.contains("applied") ||
                subject.contains("application") ||
                subject.contains("submitted") ||
                subject.contains("software engineer") ||
                subject.contains("intern") ||
                subject.contains("job") ||
                subject.contains("position") ||
                subject.contains("hiring");
    }

    private String getEmailBody(Message message) {
        StringBuilder bodyBuilder = new StringBuilder();
        try {
            MessagePart payload = message.getPayload();
            if (payload != null) {
                extractBodyParts(payload, bodyBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyBuilder.toString();
    }

    private void extractBodyParts(MessagePart part, StringBuilder bodyBuilder) {
        if (part.getParts() != null) {
            // Multipart message - recursively process each part
            for (MessagePart subPart : part.getParts()) {
                extractBodyParts(subPart, bodyBuilder);
            }
        } else {
            // Check if this part has content and is text
            String mimeType = part.getMimeType();
            if (part.getBody() != null && part.getBody().getData() != null &&
                    (mimeType.equals("text/plain") || mimeType.equals("text/html"))) {
                String data = part.getBody().getData();
                try {
                    // Replace URL-safe characters and decode
                    String decodedData = new String(Base64.getUrlDecoder().decode(data));
                    bodyBuilder.append(decodedData).append("\n");
                } catch (IllegalArgumentException e) {
                    System.err.println("Failed to decode email part: " + e.getMessage());
                }
            }
        }
    }

    // Update fetchAndFilterEmails to use EmailFilter
    // Update fetchAndFilterEmails to use EmailFilter
    public void fetchAndFilterEmails(List<Message> messages) {
        List<Message> filteredEmails = new ArrayList<>();

        for (Message message : messages) {
            try {
                if (emailFilter.isJobApplicationEmail(message)) {
                    filteredEmails.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Test: Print filtered emails to verify
        System.out.println("Filtered Emails:");
        for (Message email : filteredEmails) {
            System.out.println("Subject: " + getSubject(email));
        }

        // Proceed with sending to DeepSeek if filtering is successful
        if (!filteredEmails.isEmpty()) {
            sendToDeepSeek(filteredEmails);
        }
    }

    // Keep this method to extract subject for logging purposes
    private String getSubject(Message message) {
        String subject = "";
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for (MessagePartHeader header : headers) {
            if ("Subject".equalsIgnoreCase(header.getName())) {
                subject = header.getValue();
                break;
            }
        }
        return subject;
    }

    private void sendToDeepSeek(List<Message> filteredEmails) {
        // Implement DeepSeek API call here (mocked for now)
        for (Message email : filteredEmails) {
            // Mock sending the email content to DeepSeek API
            System.out.println("Sending email to DeepSeek for processing...");
            String subject = getSubject(email);
            String body = getEmailBody(email);

            // Simulate DeepSeek processing and response storage (mock response)
            String deepSeekResponse = processDeepSeek(subject, body);

            // Now store the DeepSeek response (not the email) in the database
            storeDeepSeekResponse(deepSeekResponse);
        }
    }

    private String processDeepSeek(String subject, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + deepSeekApiKey);
            headers.set("Content-Type", "application/json");

            // Build a structured prompt that asks for a structured JSON response
            String prompt = "Analyze this email and perform the following steps:\n\n" +
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
            // Build DeepSeek API request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = Collections.singletonList(
                    Map.of("role", "user", "content", prompt)
            );
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(deepSeekApiUrl, HttpMethod.POST, request, String.class);

            // Parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to process request.";
        }
    }

    private void storeDeepSeekResponse(String response) {


        System.out.println("Storing response in the database: " + response);
    }
}
