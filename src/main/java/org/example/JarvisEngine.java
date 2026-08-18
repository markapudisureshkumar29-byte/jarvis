package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarvisEngine {

    private final NaturalLanguageService naturalLanguageService;
    private final CommandProcessor commandProcessor;
    private final VoiceService voiceService;
    private final VoiceInputService voiceInputService;
    private final ConversationService conversationService;
    private final CompoundCommandResolver compoundResolver;
    private final Scanner scanner;

    private volatile boolean voiceModeActive = false;
    private volatile boolean awake = false;

    private volatile boolean awaitingConfirmation = false;
    private volatile String pendingCommand = null;
    private volatile List<String> pendingCompoundTail = new ArrayList<>();

    private static final Pattern YES_PATTERN = Pattern.compile(
            "^(yes|yeah|sure|confirm|go ahead|do it|ok|okay|yep)$"
    );
    private static final Pattern NO_PATTERN = Pattern.compile(
            "^(no|nope|cancel|stop|don't|do not|abort|nah)$"
    );

    // Whisper (whisper-base.en) consistently mishears "jarvis" as
    // near-sounding words, even when spoken clearly/loudly — this is
    // a real, observed STT accuracy limitation, not a mic/volume
    // issue. Matching only the literal word "jarvis" left the wake
    // word almost never detected in real use. This pattern instead
    // matches "jarvis" plus its common real-world mishearings.
    private static final Pattern WAKE_WORD_PATTERN = Pattern.compile(
            "\\b(jarvis|jardries|jardis|jarvus|jarvas|darvis|jadvis|jadvies|jarvies|jarvi)\\b"
    );

    // Once awake, JARVIS stays awake and keeps conversing naturally
    // until the user explicitly asks it to sleep/stop listening.
    // Separate from isExitCommand(), which ends the whole voice
    // mode session entirely rather than just going quiet.
    private static final Pattern SLEEP_PATTERN = Pattern.compile(
            "\\b(go to sleep|sleep now|stop listening|go back to sleep)\\b"
    );

    public JarvisEngine() {
        MemoryService memoryService = new MemoryService();
        AiProvider aiProvider = new GroqProvider();

        naturalLanguageService = new NaturalLanguageService(memoryService);
        compoundResolver = new CompoundCommandResolver(naturalLanguageService);
        commandProcessor = new CommandProcessor(memoryService);
        voiceService = new VoiceService();
        voiceInputService = new VoiceInputService(
                "C:\\Users\\marka\\whisper-cpp\\whisper-bin-x64\\Release\\whisper-cli.exe",
                "C:\\Users\\marka\\whisper-cpp\\models\\ggml-base.en.bin"
        );
        conversationService = new ConversationService(aiProvider, memoryService);
        scanner = new Scanner(System.in);
    }

    public void startTextMode() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          JARVIS TEXT MODE");
        System.out.println("  Type 'exit' or 'quit' to stop.");
        System.out.println("========================================");
        System.out.println();

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.isBlank()) {
                continue;
            }

            if (isExitCommand(input)) {
                speak("Goodbye.");
                break;
            }

            processInput(input);
            System.out.println();
        }
    }

    public void startVoiceMode() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("          JARVIS VOICE MODE");
        System.out.println("  Say 'Jarvis' (any phrasing) to wake me.");
        System.out.println("  Say 'go to sleep' to stop listening actively.");
        System.out.println("  Say 'exit' or 'quit' to end voice mode.");
        System.out.println("========================================");
        System.out.println();

        voiceModeActive = true;
        awake = false;

        voiceInputService.setListener(new VoiceInputService.Listener() {
            @Override
            public void onText(String text) {
                if (text == null || text.isBlank()) {
                    return;
                }

                if (isExitCommand(text)) {
                    speak("Shutting down voice mode.");
                    voiceModeActive = false;
                    voiceInputService.stop();
                    return;
                }

                if (awaitingConfirmation) {
                    handleConfirmation(text);
                    return;
                }

                if (!awake) {
                    String afterWake = extractAfterWakeWord(text);
                    if (afterWake == null) {
                        return;
                    }
                    awake = true;
                    if (afterWake.isBlank()) {
                        speak("Yes sir?");
                    } else {
                        processInput(afterWake);
                    }
                    return;
                }

                // Already awake: check for sleep command before
                // treating this as a normal command/conversation input.
                String normalized = normalize(text);
                if (SLEEP_PATTERN.matcher(normalized).find()) {
                    speak("Going to sleep. Say my name when you need me.");
                    awake = false;
                    return;
                }

                processInput(text);
                // Note: awake stays true here — JARVIS keeps listening
                // and conversing naturally without needing the wake
                // word again, until sleep is explicitly requested.
            }
        });

        voiceInputService.start();

        while (voiceModeActive) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                voiceModeActive = false;
                voiceInputService.stop();
                break;
            }
        }

        System.out.println();
        System.out.println("Voice mode ended.");
    }

    private void processInput(String input) {
        try {
            if (input == null || input.isBlank()) {
                return;
            }

            input = input.trim().replaceAll("\\s+", " ");
            System.out.println("Processing: " + input);

            if (awaitingConfirmation) {
                handleConfirmation(input);
                return;
            }

            List<String> resolved = compoundResolver.resolve(input);

            if (resolved.isEmpty()) {
                speak("I didn't catch that.");
                return;
            }

            // Conversation branch
            if (resolved.size() == 1 && resolved.get(0).startsWith("conversation:")) {
                String reply = conversationService.chat(input);
                speak(reply);
                return;
            }

            // Command branch
            for (int i = 0; i < resolved.size(); i++) {
                String command = resolved.get(i);

                if (command == null || command.isBlank()) {
                    continue;
                }

                System.out.println("Canonical: " + command);

                if (isDangerous(command)) {
                    pendingCompoundTail = new ArrayList<>();
                    for (int j = i + 1; j < resolved.size(); j++) {
                        pendingCompoundTail.add(resolved.get(j));
                    }

                    pendingCommand = command;
                    awaitingConfirmation = true;
                    speak("You asked me to " + command + ". Say 'yes' to confirm, or 'no' to cancel.");
                    return;
                }

                commandProcessor.processCommand(command);
            }

        } catch (Exception e) {
            System.err.println();
            System.err.println("JARVIS ERROR: " + e.getMessage());
            e.printStackTrace();
            speak("I encountered an error processing that request.");
        }
    }

    private void handleConfirmation(String input) {
        String lower = input.trim().toLowerCase(Locale.ROOT);

        if (YES_PATTERN.matcher(lower).matches()) {
            commandProcessor.processCommand(pendingCommand);

            for (String command : pendingCompoundTail) {
                if (command != null && !command.isBlank()) {
                    commandProcessor.processCommand(command);
                }
            }

            pendingCompoundTail.clear();
            pendingCommand = null;
            awaitingConfirmation = false;

        } else if (NO_PATTERN.matcher(lower).matches()) {
            speak("Action cancelled.");
            pendingCompoundTail.clear();
            pendingCommand = null;
            awaitingConfirmation = false;

        } else {
            speak("Please say yes to confirm, or no to cancel.");
        }
    }

    private boolean isDangerous(String command) {
        if (command == null) return false;
        String lower = command.trim().toLowerCase(Locale.ROOT);
        return lower.equals("shutdown") || lower.equals("restart");
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractAfterWakeWord(String text) {
        if (text == null) {
            return null;
        }

        String normalized = normalize(text);
        Matcher matcher = WAKE_WORD_PATTERN.matcher(normalized);

        if (!matcher.find()) {
            return null;
        }

        return normalized.substring(matcher.end()).trim();
    }

    private void speak(String text) {
        System.out.println("JARVIS: " + text);
        voiceService.speak(text);
    }

    private boolean isExitCommand(String input) {
        if (input == null) return false;
        String command = input.trim().toLowerCase(Locale.ROOT);
        return command.equals("exit")
                || command.equals("quit")
                || command.contains("bye");
    }
}