package org.example;

/**
 * Represents the result of interpreting a user's speech.
 *
 * It is either:
 * - a COMMAND: a canonical command string ready for
 *   CommandProcessor to execute, or
 * - CONVERSATION: a natural-language reply from JARVIS
 *   (already generated) that should simply be spoken back
 *   to the user, with no command execution involved.
 */
public class NluResult {

    public enum Type {
        COMMAND,
        CONVERSATION
    }

    private final Type type;

    private final String text;

    public NluResult(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}