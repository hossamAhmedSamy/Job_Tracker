package com.GGT.gmailJobTracker.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
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

    private String getEmailBody(Message message) {
        String body = "";
        try {
            MessagePart payload = message.getPayload();
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                body = new String(Base64.getDecoder().decode(payload.getBody().getData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    // Fetch, filter, and send to DeepSeek
    public void fetchAndFilterEmails(List<Message> messages) {
        List<Message> filteredEmails = new ArrayList<>();

        for (Message message : messages) {
            try {
                if (isJobApplicationEmail(getEmailBody(message)) || isJobApplicationEmail(getSubject(message))) {
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
        sendToDeepSeek(filteredEmails);
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

            // Build the DeepSeek prompt
            String prompt = "Summarize this email and tell if it is a job application update:\n\nSubject: "
                    + subject + "\n\nBody:\n" + body;

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
