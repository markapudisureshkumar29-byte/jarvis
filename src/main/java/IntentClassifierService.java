package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses an AiProvider to understand what the user actually
 * means, instead of relying on rigid startsWith()/contains()
 * string matching. Replaces the "guessing game" of natural
 * phrasing with real language understanding, while still only
 * being allowed to pick from a fixed, safe set of real JARVIS
 * capabilities.
 *
 * Returns a canonical command string in EXACTLY the raw-phrase
 * format CommandProcessor.processCommand() already parses
 * (e.g. "search youtube mahesh babu", "open website youtube"),
 * or "conversation" if nothing matched — so nothing downstream
 * of this class needs to change at all.
 */
public class IntentClassifierService {

    private final AiProvider aiProvider;

    public IntentClassifierService(AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    /**
     * Classifies a user's sentence into a canonical
     * CommandProcessor-compatible command string, or
     * "conversation" if no real intent was recognized.
     */
    public String classify(String userInput) {

        if (userInput == null || userInput.isBlank()) {
            return "conversation";
        }

        String systemPrompt =
                buildSystemPrompt();

        String rawReply =
                aiProvider.generateReply(
                        systemPrompt,
                        userInput
                );

        ParsedIntent parsed =
                parseIntentJson(rawReply);

        if (parsed == null) {
            /*
             * AI call failed, or returned something that
             * wasn't valid JSON (network error fallback text,
             * malformed output, etc). Safe default: treat as
             * conversation rather than guessing at a command.
             */
            return "conversation";
        }

        return toCanonicalCommand(parsed);
    }

    private String buildSystemPrompt() {

        return """
                You are an intent classifier for JARVIS, a
                personal desktop assistant. Your ONLY job is to
                read the user's sentence and decide which single
                real JARVIS capability (if any) they want, then
                respond with ONE line of raw JSON and nothing
                else — no markdown, no code fences, no
                explanation, no extra text before or after it.

                Respond in EXACTLY this JSON shape:
                {"intent":"<intent_name>","target":"<value or empty string>","key":"<value or empty string>","value":"<value or empty string>"}

                Never omit a field. Use empty strings "" for any
                field that does not apply, never null.

                Use "target" for these single-argument intents:
                open_website, open_application, search_google,
                search_youtube, open_file, open_folder,
                calculate, weather.

                Use "key" and "value" together ONLY for the
                "remember" intent (key = the fact's name, value
                = the fact itself). Use only "key" for "recall"
                and "forget".

                Valid intent names (use EXACTLY one of these):
                open_website, open_application, search_google,
                search_youtube, open_file, open_folder,
                calculate, weather, time, date, screenshot,
                system_info, volume_up, volume_down, mute,
                unmute, shutdown, restart, cancel_shutdown,
                voice_on, voice_off, help, remember, recall,
                forget, unknown.

                If the sentence does not clearly and
                specifically ask for one of these real
                capabilities, respond with intent "unknown" —
                do NOT guess or force a match. Casual
                conversation, jokes, questions about the world,
                greetings, and anything ambiguous should be
                "unknown".

                Known website names (use the short name as
                target, not a URL): google, youtube, github,
                gmail, chatgpt, reddit, twitter, linkedin,
                stackoverflow, netflix, amazon, wikipedia. Other
                site names are allowed too if clearly named.

                Known application names (use the short name as
                target): chrome, notepad, calculator, vscode,
                terminal, explorer, settings, task manager,
                paint, word, excel, powerpoint, spotify,
                discord, gmail, chatgpt, github.

                Examples:
                "open youtube" -> {"intent":"open_website","target":"youtube","key":"","value":""}
                "open me youtube please" -> {"intent":"open_website","target":"youtube","key":"","value":""}
                "can you pull up google for me" -> {"intent":"open_website","target":"google","key":"","value":""}
                "search youtube for mahesh babu" -> {"intent":"search_youtube","target":"mahesh babu","key":"","value":""}
                "play mahesh babu on youtube" -> {"intent":"search_youtube","target":"mahesh babu","key":"","value":""}
                "what is time" -> {"intent":"time","target":"","key":"","value":""}
                "what is the actual time right now" -> {"intent":"time","target":"","key":"","value":""}
                "what is 25 plus 17" -> {"intent":"calculate","target":"25 + 17","key":"","value":""}
                "remember my name is suresh" -> {"intent":"remember","target":"","key":"name","value":"suresh"}
                "what is my name" -> {"intent":"recall","target":"","key":"name","value":""}
                "take a screenshot" -> {"intent":"screenshot","target":"","key":"","value":""}
                "tell me a joke" -> {"intent":"unknown","target":"","key":"","value":""}
                "what is pain right now" -> {"intent":"unknown","target":"","key":"","value":""}
                """;
    }

    /**
     * Small internal holder for the parsed JSON fields.
     */
    private record ParsedIntent(
            String intent,
            String target,
            String key,
            String value) {
    }

    /**
     * Defensively extracts the four expected fields from the
     * AI's JSON reply using simple regex matching — matching
     * the same lightweight parsing style already used in
     * GroqProvider, rather than pulling in a JSON library.
     * Returns null if the reply isn't usable JSON at all.
     */
    private ParsedIntent parseIntentJson(String reply) {

        if (reply == null || reply.isBlank()) {
            return null;
        }

        String intent = extractField(reply, "intent");

        if (intent == null || intent.isBlank()) {
            return null;
        }

        String target = extractField(reply, "target");
        String key = extractField(reply, "key");
        String value = extractField(reply, "value");

        return new ParsedIntent(
                intent.trim().toLowerCase(),
                target == null ? "" : target.trim(),
                key == null ? "" : key.trim(),
                value == null ? "" : value.trim()
        );
    }

    private String extractField(String json, String fieldName) {

        Pattern pattern =
                Pattern.compile(
                        "\"" + fieldName + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                );

        Matcher matcher =
                pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Converts a parsed intent into EXACTLY the raw-phrase
     * command string CommandProcessor.processCommand() expects
     * — same contract toCanonicalCommand() used to produce.
     */
    private String toCanonicalCommand(ParsedIntent parsed) {

        switch (parsed.intent()) {

            case "open_website":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "open website " + parsed.target();

            case "open_application":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "open " + parsed.target();

            case "search_google":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "search google " + parsed.target();

            case "search_youtube":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "search youtube " + parsed.target();

            case "open_file":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "open file " + parsed.target();

            case "open_folder":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "open folder " + parsed.target();

            case "calculate":
                return parsed.target().isBlank()
                        ? "conversation"
                        : "calculate " + parsed.target();

            case "weather":
                return parsed.target().isBlank()
                        ? "weather"
                        : "weather " + parsed.target();

            case "time":
                return "time";

            case "date":
                return "date";

            case "screenshot":
                return "screenshot";

            case "system_info":
                return "system info";

            case "volume_up":
                return "volume up";

            case "volume_down":
                return "volume down";

            case "mute":
                return "mute";

            case "unmute":
                return "unmute";

            case "shutdown":
                return "shutdown";

            case "restart":
                return "restart";

            case "cancel_shutdown":
                return "cancel shutdown";

            case "voice_on":
                return "voice on";

            case "voice_off":
                return "voice off";

            case "help":
                return "help";

            case "remember":
                return (parsed.key().isBlank() || parsed.value().isBlank())
                        ? "conversation"
                        : "remember " + parsed.key() + " is " + parsed.value();

            case "recall":
                return parsed.key().isBlank()
                        ? "conversation"
                        : "what is my " + parsed.key();

            case "forget":
                return parsed.key().isBlank()
                        ? "conversation"
                        : "forget my " + parsed.key();

            default:
                return "conversation";
        }
    }
}