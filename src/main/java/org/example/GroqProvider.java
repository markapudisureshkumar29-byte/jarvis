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
 * Online AiProvider backed by Groq's free API, running
 * Llama 3.3 70B for high-quality, low-latency responses.
 *
 * The API key is read from the GROQ_API_KEY environment
 * variable — NEVER hardcode it here, since this project is
 * published on GitHub.
 */
public class GroqProvider implements AiProvider {

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String MODEL =
            "openai/gpt-oss-120b";

    private final HttpClient httpClient;

    private final String apiKey;

    public GroqProvider() {

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        apiKey =
                System.getenv("GROQ_API_KEY");
    }

    @Override
    public String generateReply(
            String systemPrompt,
            String userMessage) {

        if (apiKey == null || apiKey.isBlank()) {

            return "My online reasoning isn't configured yet — "
                    + "the GROQ_API_KEY environment variable is missing.";
        }

        try {

            String json = """
                    {
                      "model": "%s",
                      "messages": [
                        {"role": "system", "content": "%s"},
                        {"role": "user", "content": "%s"}
                      ],
                      "temperature": 0.6
                    }
                    """.formatted(
                    MODEL,
                    escapeJson(systemPrompt),
                    escapeJson(userMessage)
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(GROQ_URL))
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
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

                System.out.println(
                        "[GroqProvider] HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );

                return "I'm having trouble reaching my online reasoning right now.";
            }

            String reply =
                    extractGroqResponse(response.body());

            if (reply == null || reply.isBlank()) {

                return "I'm not sure how to respond to that.";
            }

            return reply.trim();

        } catch (IOException e) {

            return "I can't reach the internet for online reasoning right now.";

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return "I was interrupted while thinking.";
        }
    }

    private String extractGroqResponse(String json) {

        Pattern pattern =
                Pattern.compile(
                        "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
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