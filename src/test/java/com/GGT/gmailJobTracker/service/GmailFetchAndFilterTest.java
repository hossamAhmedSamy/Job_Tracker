package com.GGT.gmailJobTracker.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class GmailFetchAndFilterTest {

    @Autowired
    private GmailService gmailService;

    @Autowired
    private EmailFilter emailFilter;

    @Test
    public void testEmailFiltering() throws Exception {
        Gmail service = GmailServiceBuilder.getGmailService();

        // Fetch 10 latest messages
        ListMessagesResponse messagesResponse = service.users().messages().list("me")
                .setMaxResults(10L)
                .execute();
        List<Message> messages = new ArrayList<>();

        System.out.println("Fetched " + messagesResponse.getMessages().size() + " messages");

        for (Message m : messagesResponse.getMessages()) {
            Message fullMessage = service.users().messages().get("me", m.getId()).execute();
            messages.add(fullMessage);

            // Print subject for debugging
            String subject = "";
            if (fullMessage.getPayload() != null && fullMessage.getPayload().getHeaders() != null) {
                for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(header.getName())) {
                        subject = header.getValue();
                        break;
                    }
                }
            }
            System.out.println("Message: " + subject);

            // Test filter individually
            boolean isJobEmail = emailFilter.isJobApplicationEmail(fullMessage);
            System.out.println("Is job email: " + isJobEmail);
        }

        // Test full filtering process
        gmailService.fetchAndFilterEmails(messages);
    }
}