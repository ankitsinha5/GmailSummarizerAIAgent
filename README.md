# Gemini Gmail Digest Service

An intelligent Java-based utility that connects to your Gmail account, retrieves messages from the last 24 hours, and uses Google's Gemini AI to generate a concise, actionable summary of your day.

## 🚀 Features

*   **Secure Gmail Integration**: Uses OAuth 2.0 to securely access Gmail messages without storing passwords.
*   **AI-Powered Summarization**: Leverages the `gemini-1.5-flash` model via LangChain4j for high-speed, intelligent content analysis.
*   **Actionable Insights**: Transforms a cluttered inbox into a structured summary of tasks and important information.
*   **Diagnostic Tools**: Includes built-in model discovery to verify API key capabilities at runtime.

## 🛠️ Tech Stack

*   **Language**: Java 23
*   **Build Tool**: Maven
*   **AI Framework**: LangChain4j
*   **APIs**: Google Gmail API, Google Generative Language API (Gemini)

## 📋 Prerequisites

### 1. Google Cloud Configuration
You must have a Google Cloud Project with the following APIs enabled:
*   **Gmail API**
*   **Generative Language API** (required for Gemini)

### 2. Gmail OAuth Credentials
1.  Go to the Google Cloud Console.
2.  Navigate to **APIs & Services > Credentials**.
3.  Create an **OAuth 2.0 Client ID** of type **Desktop App**.
4.  Download the JSON file, rename it to `credentials.json`, and place it in:
    `src/main/resources/credentials.json`

### 3. Gemini API Key
1.  Visit Google AI Studio.
2.  Generate a new API Key.
3.  **Security Note**: Do not hardcode this key. Set it as an environment variable:
    ```bash
    export GEMINI_API_KEY='your_api_key_here'
    ```

## ⚙️ Setup & Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/FirstAIProject.git
    cd FirstAIProject
    ```

2.  **Install Dependencies**:
    ```bash
    mvn clean install
    ```

## 🏃 Running the Application

You can run the application directly from your IDE or via the terminal. On the first run, a browser window will open asking for permission to read your Gmail messages.

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## 🛡️ Security Best Practices

*   **Environment Variables**: This project uses `System.getenv("GEMINI_API_KEY")` to prevent accidental leakage of secrets.
*   **OAuth Scopes**: The project uses `GMAIL_READONLY` scope to adhere to the principle of least privilege.
*   **Token Storage**: Authentication tokens are stored locally in the `/tokens` directory. Delete this folder if you need to re-authenticate or clear credentials.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---
*Developed as part of an exploration into AI-native Java applications.*