package org.example.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@CrossOrigin(origins = "*")
public class EmailDigestRestController {

    @GetMapping("/api/digest/generate")
    public ResponseEntity<String> generateDigest() {
        try {
            EmailDigestProcessor processor = new EmailDigestProcessor();
            String summary = processor.processEmailsAndGenerateDigest();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "An error occurred during digest generation: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("GEMINI_API_KEY")) {
                errorMsg += "\nHint: The GEMINI_API_KEY environment variable might not be set.";
            } else if (e.getMessage() != null && e.getMessage().contains("credentials.json")) {
                errorMsg += "\nHint: The credentials.json file is missing or incorrectly placed.";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMsg);
        }
    }
}
