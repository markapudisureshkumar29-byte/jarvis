
        package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KokoroTTSClient {

    private static final String KOKORO_URL =
            "http://localhost:8880/v1/audio/speech";

    private static final String MODEL =
            "kokoro";

    private static final String VOICE =
            "am_onyx";

    private final HttpClient httpClient;

    public KokoroTTSClient() {

        this.httpClient =
                HttpClient.newHttpClient();
    }

    /**
     * Sends text to Kokoro and returns WAV audio.
     *
     * @param text text that JARVIS should speak
     * @return WAV audio as byte array
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public byte[] synthesize(String text)
            throws IOException, InterruptedException {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text cannot be null or empty."
            );
        }

        String json =
                "{"
                        + "\"model\":\"" + MODEL + "\","
                        + "\"voice\":\"" + VOICE + "\","
                        + "\"input\":\"" + escapeJson(text) + "\","
                        + "\"response_format\":\"wav\""
                        + "}";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(KOKORO_URL))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json,
                                        StandardCharsets.UTF_8
                                )
                        )
                        .build();

        HttpResponse<byte[]> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            String error =
                    new String(
                            response.body(),
                            StandardCharsets.UTF_8
                    );

            throw new IOException(
                    "Kokoro returned HTTP "
                            + response.statusCode()
                            + ": "
                            + error
            );
        }

        return response.body();
    }

    /**
     * Escapes characters that have special meaning in JSON strings.
     */
    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

