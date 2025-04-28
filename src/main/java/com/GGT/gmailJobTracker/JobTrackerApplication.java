package com.GGT.gmailJobTracker;

import com.GGT.gmailJobTracker.service.EmailProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobTrackerApplication.class, args);
	}

	@Bean
	CommandLineRunner init(EmailProcessor emailProcessor) {
		return args -> {
			System.out.println("Running initial email processing...");
			emailProcessor.processFilteredEmails();
		};
	}
}