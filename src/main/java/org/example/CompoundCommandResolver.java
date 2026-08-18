package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CompoundCommandResolver {

    private static final Pattern SPLIT_PATTERN = Pattern.compile(
            "\\s+(?:and|then|after that|followed by|also|next)\\s+|\\s*,\\s*",
            Pattern.CASE_INSENSITIVE
    );

    private final NaturalLanguageService nlpService;

    public CompoundCommandResolver(NaturalLanguageService nlpService) {
        this.nlpService = nlpService;
    }

    public List<String> resolve(String utterance) {
        if (utterance == null || utterance.isBlank()) {
            return List.of();
        }

        String trimmed = utterance.trim();

        if (!SPLIT_PATTERN.matcher(trimmed).find()) {
            String canonical = nlpService.toCanonicalCommand(trimmed);
            if (isConversation(canonical)) {
                return List.of("conversation:" + trimmed);
            }
            return List.of(canonical);
        }

        String[] segments = SPLIT_PATTERN.split(trimmed);
        List<String> commands = new ArrayList<>();

        for (String segment : segments) {
            String seg = segment.trim();
            if (seg.isEmpty()) continue;

            String canonical = nlpService.toCanonicalCommand(seg);

            if (isConversation(canonical)) {
                return List.of("conversation:" + trimmed);
            }

            commands.add(canonical);
        }

        return commands;
    }

    private boolean isConversation(String canonical) {
        return canonical == null
                || canonical.isBlank()
                || canonical.equalsIgnoreCase("none")
                || canonical.equalsIgnoreCase("conversation")
                || canonical.startsWith("conversation:");
    }
}