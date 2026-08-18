package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline AiProvider backed by a local Ollama instance.
 */
public class OllamaProvider implements AiProvider {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL =
            "llama3.2:3b";

    private final HttpClient httpClient;

    public OllamaProvider() {

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String generateReply(
            String systemPrompt,
            String userMessage) {

        /*
         * Ollama's /api/generate endpoint takes a single
         * prompt string, not separate system/user roles, so
         * we combine them.
         */
        String fullPrompt =
                systemPrompt
                        + "\n\n"
                        + userMessage;

        try {

            String json = """
                    {
                      "model": "%s",
                      "prompt": "%s",
                      "stream": false,
                      "options": {
                        "temperature": 0.6
                      }
                    }
                    """.formatted(
                    MODEL,
                    escapeJson(fullPrompt)
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OLLAMA_URL))
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                return "I'm having trouble thinking right now (offline engine).";
            }

            String reply =
                    extractOllamaResponse(response.body());

            if (reply == null || reply.isBlank()) {

                return "I'm not sure how to respond to that.";
            }

            return reply.trim();

        } catch (IOException e) {

            return "I can't reach my offline reasoning engine right now.";

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return "I was interrupted while thinking.";
        }
    }

    private String extractOllamaResponse(String json) {

        Pattern pattern =
                Pattern.compile(
                        "\"response\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                );

        Matcher matcher =
                pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", " ")
                .replace("\\r", " ")
                .replace("\\t", " ")
                .trim();
    }

    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}