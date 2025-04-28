package com.GGT.gmailJobTracker.service;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class EmailFilter {

    private static final List<String> JOB_KEYWORDS = Arrays.asList(
            "application received", "interview scheduled", "offer extended", "rejection",
            "applied", "application", "submitted", "software engineer", "intern",
            "job", "position", "hiring", "opportunity", "thank you for your application"
    );

    public boolean isJobApplicationEmail(Message message) {
        String subject = getSubject(message);
        String body = getEmailBody(message);

        if (subject != null && subject.toLowerCase().contains("job alert")) {
            return false;
        }
        if (body != null && body.toLowerCase().contains("job alert")) {
            return false;
        }
        String subjectLower = subject != null ? subject.toLowerCase() : "";
        String bodyLower = body != null ? body.toLowerCase() : "";

        int keywordMatches = 0;
        for (String keyword : JOB_KEYWORDS) {
            if (subjectLower.contains(keyword) || bodyLower.contains(keyword)) {
                keywordMatches++;
                if (keywordMatches >= 2) { // Require at least 2 keywords
                    return true;
                }
            }
        }
        return false;
    }

    String getSubject(Message message) {
        MessagePart payload = message.getPayload();
        if (payload != null && payload.getHeaders() != null) {
            for (MessagePartHeader header : payload.getHeaders()) {
                if ("Subject".equalsIgnoreCase(header.getName())) {
                    return header.getValue();
                }
            }
        }
        return "";
    }

    String getEmailBody(Message message) {
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
}