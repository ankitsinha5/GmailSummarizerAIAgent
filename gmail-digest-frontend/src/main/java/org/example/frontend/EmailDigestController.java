package org.example.frontend;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class EmailDigestController {

    @FXML
    private Button generateDigestButton;

    @FXML
    private TextArea summaryTextArea;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    private static final String BACKEND_URL = "http://localhost:8080/api/digest/generate";
    private final HttpClient httpClient;

    public EmailDigestController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @FXML
    public void initialize() {
        summaryTextArea.setText("Click 'Generate Daily Digest' to fetch and summarize your emails using your Gemini-powered Agent.");
        statusLabel.setText("System ready");
    }

    @FXML
    protected void onGenerateDigestButtonClick() {
        generateDigestButton.setDisable(true);
        progressIndicator.setVisible(true);
        summaryTextArea.setText("Requesting email digest from backend...\n\nPlease ensure your Spring Boot backend is running on http://localhost:8080.");
        statusLabel.setText("Connecting to backend...");

        // Invoke HTTP call on a background thread to keep JavaFX application UI responsive
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BACKEND_URL))
                        .timeout(Duration.ofMinutes(2)) // AI operations might take a minute or two
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    generateDigestButton.setDisable(false);
                    progressIndicator.setVisible(false);
                    
                    if (response.statusCode() == 200) {
                        summaryTextArea.setText(response.body());
                        statusLabel.setText("Digest generated successfully!");
                    } else {
                        summaryTextArea.setText("Backend Error (HTTP " + response.statusCode() + "):\n" + response.body());
                        statusLabel.setText("Failed to generate digest");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    generateDigestButton.setDisable(false);
                    progressIndicator.setVisible(false);
                    summaryTextArea.setText("Failed to communicate with backend.\n\n" +
                            "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage() + "\n\n" +
                            "Checklist:\n" +
                            "1. Start your Spring Boot backend: mvn -pl gmail-digest-backend spring-boot:run\n" +
                            "2. Ensure GEMINI_API_KEY environment variable is set before starting the backend.\n" +
                            "3. Make sure credentials.json is present and tokens are generated.");
                    statusLabel.setText("Connection failed");
                    e.printStackTrace();
                });
            }
        }).start();
    }
}
