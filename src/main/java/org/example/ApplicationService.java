package org.example;

import java.io.IOException;
import java.util.Map;

public class ApplicationService {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("chrome", "chrome"),
            Map.entry("notepad", "notepad"),
            Map.entry("calculator", "calc"),
            Map.entry("vscode", "code"),
            Map.entry("visual studio code", "code"),
            Map.entry("terminal", "cmd"),
            Map.entry("command prompt", "cmd"),
            Map.entry("explorer", "explorer"),
            Map.entry("settings", "ms-settings:"),
            Map.entry("task manager", "taskmgr"),
            Map.entry("paint", "mspaint"),
            Map.entry("word", "winword"),
            Map.entry("excel", "excel"),
            Map.entry("powerpoint", "powerpnt"),
            Map.entry("spotify", "spotify"),
            Map.entry("discord", "discord"),
            Map.entry("gmail", "chrome https://mail.google.com"),
            Map.entry("chatgpt", "chrome https://chat.openai.com"),
            Map.entry("github", "chrome https://github.com")
    );

    public void openApplication(String name) {
        String resolved = ALIASES.getOrDefault(name.toLowerCase().trim(), name);
        try {
            Runtime.getRuntime().exec(resolved);
        } catch (IOException e) {
            System.err.println("Failed to open application: " + resolved);
            e.printStackTrace();
        }
    }
}