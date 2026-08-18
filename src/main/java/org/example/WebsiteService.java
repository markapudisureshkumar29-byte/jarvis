package org.example;

import java.io.IOException;
import java.util.Map;

public class WebsiteService {

    private static final Map<String, String> SITES = Map.ofEntries(
            Map.entry("google", "https://google.com"),
            Map.entry("youtube", "https://youtube.com"),
            Map.entry("github", "https://github.com"),
            Map.entry("gmail", "https://mail.google.com"),
            Map.entry("chatgpt", "https://chat.openai.com"),
            Map.entry("reddit", "https://reddit.com"),
            Map.entry("twitter", "https://twitter.com"),
            Map.entry("linkedin", "https://linkedin.com"),
            Map.entry("stackoverflow", "https://stackoverflow.com"),
            Map.entry("netflix", "https://netflix.com"),
            Map.entry("amazon", "https://amazon.com"),
            Map.entry("wikipedia", "https://wikipedia.org")
    );

    public void openWebsite(String name) {
        String url = SITES.getOrDefault(name.toLowerCase().trim(), "https://" + name);
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
        } catch (IOException e) {
            System.err.println("Failed to open website: " + url);
            e.printStackTrace();
        }
    }
}