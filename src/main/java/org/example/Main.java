package org.example;

import com.google.api.services.gmail.model.Message;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import java.util.List;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {

    public static void main(String[] args) {
        // 0. Pre-flight check: Ensure Gmail OAuth credentials exist
        if (Main.class.getResource("/credentials.json") == null) {
            System.err.println("FATAL: /src/main/resources/credentials.json not found.");
            System.err.println("Please download your OAuth 2.0 Client ID from Google Cloud Console.");
            return;
        }

        // 1. Setup Gemini Model
        String apiKey = Optional.ofNullable(System.getenv("GEMINI_API_KEY"))
                .orElseThrow(() -> new RuntimeException("Missing environment variable: GEMINI_API_KEY"));

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-flash-latest")
                .logRequestsAndResponses(false) // Reduced logging to keep console clean
                .build();

        // 2. Create the AI Service
        EmailDigestService digestService = AiServices.create(EmailDigestService.class, model);

        try {
            // 3. Fetch Emails
            // Using the refactored instance-based GmailProvider
            System.out.println("Initializing Gmail connection...");
            GmailProvider gmailProvider = new GmailProvider();

            System.out.println("Retrieving latest messages...");
            List<Message> allMessages = gmailProvider.getEmailsFromLastDay();
            
            if (allMessages.isEmpty()) {
                System.out.println("No new emails found.");
                return;
            }

            // Limit to the latest 2 emails for testing to save tokens
            int limit = Math.min(allMessages.size(), 2);
            List<Message> messages = allMessages.subList(0, limit);

            System.out.println("Processing the latest " + messages.size() + " emails...");
            StringBuilder allEmailContent = new StringBuilder();
            for (Message m : messages) {
                String snippet = gmailProvider.getMessageSnippet(m.getId());
                allEmailContent.append(snippet).append("\n---\n");
            }

            // 4. Summarize with Gemini
            System.out.println("Sending content to Gemini for summarization...");
            String summary = digestService.summarize(allEmailContent.toString());

            System.out.println("\n--- ACTIONABLE SUMMARY ---\n");
            System.out.println(summary);

        } catch (Exception e) {
            System.err.println("An error occurred during the email digest process:");
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println(msg);
            
            if (msg.contains("401") || msg.contains("403")) {
                System.err.println("HINT: Authentication failed. Please verify your API Key and ensure the 'Generative Language API' is enabled in your Google Cloud Console.");
            } else if (msg.contains("429")) {
                System.err.println("HINT: Rate limit reached (Quota Exhausted).");
                System.err.println("If using the Free Tier, try switching to 'gemini-1.5-flash' or wait a few minutes before retrying.");
            } else if (msg.contains("404")) {
                System.err.println("HINT: Model not found. Check if 'gemini-1.5-flash' is available in your region or if the model name is correct.");
            }
        }
    }
}
