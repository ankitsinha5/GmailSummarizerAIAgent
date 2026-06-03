package org.example;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. Setup Gemini Model
        // Recommended: Set this as an environment variable in your IDE run configuration
      //  String apiKey = System.getenv("GEMINI_API_KEY");
        String apiKey = "AQ.Ab8RN6JMaL691MJQLJYPGsVlBUTIPml-gRELaU0OyZjIX0xudA";

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: GEMINI_API_KEY environment variable is not set.");
            return;
        }

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash") // Free tier model
                .build();

        // 2. Create the AI Service
        EmailDigestService digestService = AiServices.create(EmailDigestService.class, model);

        try {
            // 3. Fetch Emails
            System.out.println("Connecting to Gmail...");
            Gmail service = GmailProvider.getService();
            
            System.out.println("Fetching emails from the last 24 hours...");
            List<Message> messages = GmailProvider.getEmailsFromLastDay(service);
            
            if (messages.isEmpty()) {
                System.out.println("No new emails found.");
                return;
            }

            StringBuilder allEmailContent = new StringBuilder();
            for (Message m : messages) {
                String snippet = GmailProvider.getMessageSnippet(service, m.getId());
                allEmailContent.append(snippet).append("\n---\n");
            }

            // 4. Summarize with Gemini
            System.out.println("Summarizing emails...");
            String summary = digestService.summarize(allEmailContent.toString());

            System.out.println("\n--- ACTIONABLE SUMMARY ---\n");
            System.out.println(summary);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
