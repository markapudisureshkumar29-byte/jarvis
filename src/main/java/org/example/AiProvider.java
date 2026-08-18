package org.example;

/**
 * Abstraction over any AI backend JARVIS can use for free-form
 * conversation. Lets JARVIS switch between offline (Ollama) and
 * online (Groq, or others later) without ConversationService
 * needing to know which one it's talking to.
 */
public interface AiProvider {

    /**
     * Generates a natural-language reply given a system
     * prompt (personality/instructions) and a user message
     * (which may include recent conversation history baked in).
     *
     * Implementations should never throw for network/API
     * failures — they should catch internally and return a
     * short, honest fallback message instead, so JARVIS always
     * says something rather than crashing.
     */
    String generateReply(
            String systemPrompt,
            String userMessage
    );
}