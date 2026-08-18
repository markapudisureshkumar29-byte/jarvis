package org.example;

import java.util.List;
import java.util.Locale;

/**
 * Interprets user input and converts it into an NluResult.
 *
 * COMMAND      -> canonical command for CommandProcessor
 * CONVERSATION -> natural-language text for JARVIS to speak
 */
public class NaturalLanguageService {

    private final MemoryService memoryService;
    private final CompoundCommandResolver compoundCommandResolver;

    /**
     * Creates the NaturalLanguageService with shared MemoryService.
     */
    public NaturalLanguageService(MemoryService memoryService) {
        this.memoryService = memoryService;
        this.compoundCommandResolver =
                new CompoundCommandResolver(this);
    }

    /**
     * Main entry point used by JarvisEngine.
     *
     * @param text user's input
     * @return NluResult containing COMMAND or CONVERSATION
     */
    public NluResult process(String text) {

        if (text == null || text.isBlank()) {
            return new NluResult(
                    NluResult.Type.CONVERSATION,
                    ""
            );
        }

        String input = text.trim();

        /*
         * Resolve possible compound commands.
         */
        List<String> results =
                compoundCommandResolver.resolve(input);

        if (results.isEmpty()) {
            return new NluResult(
                    NluResult.Type.CONVERSATION,
                    input
            );
        }

        /*
         * If the resolver determines that the input
         * is conversation, pass it back to JarvisEngine.
         */
        if (results.size() == 1
                && results.get(0).startsWith("conversation:")) {

            String conversation =
                    results.get(0).substring("conversation:".length());

            return new NluResult(
                    NluResult.Type.CONVERSATION,
                    conversation
            );
        }

        /*
         * All segments were recognized as commands.
         */
        String canonicalCommand =
                String.join(" && ", results);

        return new NluResult(
                NluResult.Type.COMMAND,
                canonicalCommand
        );
    }

    /**
     * Converts a user's sentence into a canonical JARVIS command.
     *
     * This method is also used by CompoundCommandResolver.
     */
    public String toCanonicalCommand(String text) {

        if (text == null || text.isBlank()) {
            return "conversation";
        }

        String input =
                text.trim().toLowerCase(Locale.ROOT);

        // SYSTEM COMMANDS

        if (input.contains("shutdown")
                || input.contains("shut down")
                || input.contains("turn off the computer")) {

            return "shutdown";
        }

        if (input.contains("restart")
                || input.contains("reboot")) {

            return "restart";
        }

        // TIME

        if (input.equals("time")
                || input.contains("what time")
                || input.contains("current time")) {

            return "time";
        }

        // SYSTEM INFO

        if (input.contains("system information")
                || input.contains("system info")
                || input.contains("computer information")) {

            return "system_info";
        }

        // SCREENSHOT

        if (input.contains("take a screenshot")
                || input.contains("capture screenshot")
                || input.equals("screenshot")) {

            return "screenshot";
        }

        // VOLUME

        if (input.contains("increase volume")
                || input.contains("volume up")
                || input.contains("turn up volume")) {

            return "volume_up";
        }

        if (input.contains("decrease volume")
                || input.contains("volume down")
                || input.contains("turn down volume")) {

            return "volume_down";
        }

        if (input.contains("mute")
                || input.contains("mute volume")) {

            return "mute";
        }

        // CALCULATOR

        if (input.startsWith("calculate ")
                || input.startsWith("what is ")) {

            return "calculate:" + input;
        }

        // GOOGLE SEARCH

        if (input.startsWith("search google for ")
                || input.startsWith("search for ")) {

            return "search:" + input;
        }

        // YOUTUBE

        if (input.startsWith("search youtube for ")
                || input.startsWith("play on youtube ")) {

            return "youtube:" + input;
        }

        // WEBSITE

        if (input.startsWith("open website ")
                || input.startsWith("open https://")
                || input.startsWith("open http://")) {

            return "open_website:" + input;
        }

        // APPLICATION

        if (input.startsWith("open application ")
                || input.startsWith("open app ")) {

            return "open_application:" + input;
        }

        // FILE

        if (input.startsWith("open file ")) {
            return "open_file:" + input;
        }

        // FOLDER

        if (input.startsWith("open folder ")) {
            return "open_folder:" + input;
        }

        // WEATHER

        if (input.contains("weather")
                || input.contains("temperature outside")
                || input.contains("temperature today")) {

            return "weather";
        }

        // EVERYTHING ELSE

        return "conversation";
    }
}