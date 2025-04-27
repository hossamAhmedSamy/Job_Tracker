package com.GGT.gmailJobTracker.service;


import com.google.api.services.gmail.model.Message;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import com.google.api.services.gmail.model.*;

public class EmailFilter {

    private static final List<String> JOB_KEYWORDS = Arrays.asList("application received", "interview scheduled", "offer extended", "rejection");

    public boolean isJobApplicationEmail(Message message)  {
        String subject = getSubject(message);
        String body = getEmailBody(message);

        // Filter based on keywords in subject and body
        for (String keyword : JOB_KEYWORDS) {
            if (subject.toLowerCase().contains(keyword) || body.toLowerCase().contains(keyword)) {
                return true;  // It's a job application email
            }
        }
        return false;  // Not a job application
    }

    private String getSubject(Message message) {
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

    private String getEmailBody(Message message) {
        String body = "";
        try {
            MessagePart payload = message.getPayload();
            if (payload != null && payload.getBody() != null) {
                body = new String(Base64.getDecoder().decode(payload.getBody().getData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }
}
