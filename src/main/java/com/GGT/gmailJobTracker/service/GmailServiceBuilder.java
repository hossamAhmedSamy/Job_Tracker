package com.GGT.gmailJobTracker.service;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;

@Component
public class GmailServiceBuilder {

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Path TOKENS_DIR = Path.of("tokens");

    public static Gmail getGmailService() throws Exception {
        InputStream in = GmailServiceBuilder.class.getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: credentials.json");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(GmailScopes.GMAIL_READONLY)
        )
                .setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIR.toFile()))
                .setAccessType("offline")
                .build();

        var credential = new AuthorizationCodeInstalledApp(
                flow,
                new LocalServerReceiver.Builder().setPort(8888).build()
        ).authorize("user");

        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
