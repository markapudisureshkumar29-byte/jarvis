package org.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Handles free, natural conversation with the user, using
 * whichever AiProvider it's given (offline Ollama or online
 * Groq), as opposed to NaturalLanguageService's job of
 * converting speech into canonical commands.
 *
 * Keeps a short rolling history of recent exchanges, and
 * includes real stored facts from MemoryService so JARVIS
 * answers questions like "what's my name?" using actual data
 * instead of guessing or hallucinating.
 */
public class ConversationService {

    private static final int MAX_HISTORY_TURNS = 6;

    private final AiProvider aiProvider;

    private final MemoryService memoryService;

    private final Deque<String> history;

    public ConversationService(
            AiProvider aiProvider,
            MemoryService memoryService) {

        this.aiProvider = aiProvider;

        this.memoryService = memoryService;

        history = new ArrayDeque<>();
    }

    /**
     * Sends the user's message (with context and known facts)
     * to the configured AiProvider and returns JARVIS's reply.
     */
    public String chat(String userInput) {

        String systemPrompt =
                buildSystemPrompt();

        String userMessage =
                buildUserMessage(userInput);

        String reply =
                aiProvider.generateReply(
                        systemPrompt,
                        userMessage
                );

        if (reply != null && !reply.isBlank()) {

            addToHistory(userInput, reply);
        }

        return reply;
    }

    private String buildSystemPrompt() {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are JARVIS, a personal AI desktop assistant
                running on a real person's Windows computer.

                Your TONE is calm, concise, confident, polite,
                and occasionally a little witty — similar in
                spirit to well-known fictional AI assistants.

                However, you are NOT a fictional character and
                the person you are talking to is a real college
                student, not a superhero, billionaire, or any
                fictional character. Do not roleplay as if they
                were someone else. Do not invent a backstory,
                colleagues, or events that were never mentioned
                to you.

                Never invent facts about the user. If you do not
                know something about them, say so honestly
                instead of guessing or making something up.

                CRITICAL SAFETY RULE: You are only having a
                text/voice CONVERSATION right now. You have NO
                ability to actually open apps, open websites,
                search YouTube, control windows, take
                screenshots, change volume, or do anything else
                on the user's real computer in this mode — that
                only happens through a separate command system
                that did not trigger for this message. NEVER
                claim or imply that you performed, started, or
                completed a real action on their computer (e.g.
                "I've opened YouTube", "I've maximized the
                window", "I've searched for X"). If the user's
                message sounds like they want something actually
                done rather than just talked about, say plainly
                that it may not have registered as a command and
                suggest they rephrase it more directly (e.g.
                "open youtube" or "search youtube for X").

                Keep replies brief and natural — usually one to
                three sentences, unless the user clearly wants
                more detail. Respond like a genuinely helpful,
                grounded personal assistant having a real
                conversation, not a performance.

                Do not mention that you are a language model.
                Do not add disclaimers.
                """);

        Map<String, String> facts =
                memoryService.getAllMemories();

        if (!facts.isEmpty()) {

            prompt.append(
                    "\nKnown facts about the user (from JARVIS's "
                            + "memory — use these if relevant to "
                            + "answering, do not invent additional "
                            + "facts beyond what's listed here):\n"
            );

            for (Map.Entry<String, String> entry :
                    facts.entrySet()) {

                prompt.append("- ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        return prompt.toString();
    }

    private String buildUserMessage(String userInput) {

        StringBuilder message = new StringBuilder();

        if (!history.isEmpty()) {

            message.append("Recent conversation:\n");

            for (String line : history) {
                message.append(line).append("\n");
            }

            message.append("\n");
        }

        message.append("User: ")
                .append(userInput)
                .append("\nJARVIS:");

        return message.toString();
    }

    private void addToHistory(String userInput, String reply) {

        history.add(
                "User: " + userInput
                        + "\nJARVIS: " + reply
        );

        while (history.size() > MAX_HISTORY_TURNS) {
            history.poll();
        }
    }
}