package com.GGT.gmailJobTracker.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.PostConstruct;
import java.util.*;

@SpringBootApplication
@SpringBootTest
public class GmailFetchAndFilterTest {

    @Autowired
    private GmailService gmailService;

    @PostConstruct
    public void runTest() throws Exception {
        Gmail service = GmailServiceBuilder.getGmailService();

        // Fetch 10 latest messages
        ListMessagesResponse messagesResponse = service.users().messages().list("me")
                .setMaxResults(10L)
                .execute();
        List<Message> messages = new ArrayList<>();

        for (Message m : messagesResponse.getMessages()) {
            Message fullMessage = service.users().messages().get("me", m.getId()).execute();
            messages.add(fullMessage);
        }

        // Now run the filtering + test printing
        gmailService.fetchAndFilterEmails(messages);
    }
}
