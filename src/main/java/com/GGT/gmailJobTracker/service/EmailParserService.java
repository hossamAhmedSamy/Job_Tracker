package com.GGT.gmailJobTracker.service;

import com.GGT.gmailJobTracker.entities.JobApplication;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EmailParserService {

    private static final Gson gson = new Gson();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static JobApplication parseJsonToEntity(String jsonResponse) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Skip if not a job application
        if (!json.get("isJobApplication").getAsBoolean()) {
            return null;
        }

        JobApplication jobApp = new JobApplication();

        // Extract and validate required fields
        jobApp.setCompanyName(json.get("companyName").getAsString());
        jobApp.setJobTitle(json.get("jobTitle").getAsString());
        jobApp.setStatus(json.get("status").getAsString());

        // Handle optional fields
        if (json.has("applicationDate") && !json.get("applicationDate").isJsonNull()) {
            jobApp.setApplicationDate(LocalDate.parse(json.get("applicationDate").getAsString(), DATE_FORMATTER));
        }

        // Parse importantDates (if any)
        if (json.has("importantDates")) {
            JsonObject datesJson = json.getAsJsonObject("importantDates");
            Map<String, LocalDate> importantDates = new HashMap<>();
            datesJson.entrySet().forEach(entry ->
                    importantDates.put(entry.getKey(), LocalDate.parse(entry.getValue().getAsString(), DATE_FORMATTER))
            );
            jobApp.setImportantDates(importantDates);
        }

        // Parse keyDetails (if any)
        if (json.has("keyDetails")) {
            JsonObject detailsJson = json.getAsJsonObject("keyDetails");
            Map<String, String> keyDetails = new HashMap<>();
            detailsJson.entrySet().forEach(entry ->
                    keyDetails.put(entry.getKey(), entry.getValue().getAsString())
            );
            jobApp.setKeyDetails(keyDetails);
        }

        return jobApp;
    }
}