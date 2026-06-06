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

        // 1. Diagnostic: List available models for this specific key
        listAvailableModels(apiKey);

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .logRequestsAndResponses(true) // Enabled to debug the 404 response details
                .build();

        // 2. Create the AI Service
        EmailDigestService digestService = AiServices.create(EmailDigestService.class, model);

        try {
            // 3. Fetch Emails
            // Using the refactored instance-based GmailProvider
            System.out.println("Initializing Gmail connection...");
            GmailProvider gmailProvider = new GmailProvider();

            System.out.println("Retrieving messages from the last 24 hours...");
            List<Message> messages = gmailProvider.getEmailsFromLastDay();
            
            if (messages.isEmpty()) {
                System.out.println("No new emails found.");
                return;
            }

            System.out.println("Processing " + messages.size() + " emails...");
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
            } else if (msg.contains("404")) {
                System.err.println("HINT: Model not found. Check if 'gemini-1.5-flash' is available in your region or if the model name is correct.");
            }
        }
    }

    private static void listAvailableModels(String apiKey) {
        System.out.println(">>> Checking available models for your API key...");
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(">>> Available Models Response:");
            System.out.println(response.body());
            System.out.println("--------------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Failed to list models: " + e.getMessage());
        }
    }
}
