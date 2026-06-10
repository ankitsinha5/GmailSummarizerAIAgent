package org.example.backend;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface EmailDigestService {

    @SystemMessage({
        "You are a helpful assistant that summarizes daily emails.",
        "Filter out spam, advertisements, and newsletters.",
        "Focus on personal messages, work-related emails, and important notifications.",
        "For the remaining emails, provide a concise and actionable summary.",
        "Format the output clearly with bullet points."
    })
    String summarize(@UserMessage String emailContent);
}
