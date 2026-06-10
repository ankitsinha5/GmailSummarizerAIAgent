package org.example.backend;

import com.google.api.services.gmail.model.Message;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

public class EmailDigestProcessor {

    private final EmailDigestService digestService;
    private final GmailProvider gmailProvider;

    public EmailDigestProcessor() throws IOException, GeneralSecurityException {
        // 0. Pre-flight check: Ensure Gmail OAuth credentials exist
        if (EmailDigestProcessor.class.getResource("/credentials.json") == null) {
            throw new RuntimeException("FATAL: /src/main/resources/credentials.json not found. Please download your OAuth 2.0 Client ID from Google Cloud Console.");
        }

        // 1. Setup Gemini Model
        String apiKey = Optional.ofNullable(System.getenv("GEMINI_API_KEY"))
                .orElseThrow(() -> new RuntimeException("Missing environment variable: GEMINI_API_KEY"));

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-flash-latest")
                .logRequestsAndResponses(false)
                .build();

        // 2. Create the AI Service
        this.digestService = AiServices.create(EmailDigestService.class, model);
        this.gmailProvider = new GmailProvider();
    }

    public String processEmailsAndGenerateDigest() throws IOException, GeneralSecurityException {
        System.out.println("Initializing Gmail connection...");
        System.out.println("Retrieving latest messages...");
        List<Message> allMessages = gmailProvider.getEmailsFromLastDay();
        
        if (allMessages.isEmpty()) {
            return "No new emails found from the last 24 hours.";
        }

        // Limit to the latest 5 emails for testing to save tokens
        int limit = Math.min(allMessages.size(), 5);
        List<Message> messages = allMessages.subList(0, limit);

        System.out.println("Processing the latest " + messages.size() + " emails...");
        StringBuilder allEmailContent = new StringBuilder();
        for (Message m : messages) {
            String snippet = gmailProvider.getMessageSnippet(m.getId());
            allEmailContent.append(snippet).append("\n---\n");
        }

        System.out.println("Sending content to Gemini for summarization...");
        return digestService.summarize(allEmailContent.toString());
    }
}
