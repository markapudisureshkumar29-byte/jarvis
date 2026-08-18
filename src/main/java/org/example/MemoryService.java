package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MemoryService {

    private static final String MEMORY_FILE = "jarvis_memory.dat";

    private final Map<String, String> memories = new HashMap<>();

    public MemoryService() {
        loadMemories();
    }

    public void remember(String key, String value) {

        key = cleanKey(key);
        value = value.trim();

        memories.put(key, value);
        saveMemories();

        System.out.println(
                "Jarvis: I will remember your " + key + " is " + value + "."
        );
    }

    public String recall(String key) {

        key = cleanKey(key);

        return memories.get(key);
    }

    public void forget(String key) {

        key = cleanKey(key);

        if (memories.remove(key) != null) {

            saveMemories();

            System.out.println(
                    "Jarvis: I have forgotten your " + key + "."
            );

        } else {

            System.out.println(
                    "Jarvis: I don't remember your " + key + "."
            );
        }
    }

    /**
     * Returns a copy of all currently stored memory facts,
     * for use by the conversational AI layer so it can answer
     * questions using real stored data instead of guessing.
     *
     * Returns a copy (not the live map) so callers can't
     * accidentally mutate MemoryService's internal state.
     */
    public Map<String, String> getAllMemories() {

        return new HashMap<>(memories);
    }

    private String cleanKey(String key) {

        key = key.trim().toLowerCase();

        if (key.startsWith("my ")) {
            key = key.substring(3).trim();
        }

        return key;
    }

    private void saveMemories() {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(MEMORY_FILE))) {

            for (Map.Entry<String, String> entry :
                    memories.entrySet()) {

                writer.write(entry.getKey());
                writer.write("=");
                writer.write(entry.getValue());
                writer.newLine();
            }

        } catch (IOException e) {

            System.out.println(
                    "Jarvis: Could not save memory."
            );
        }
    }

    private void loadMemories() {

        File file = new File(MEMORY_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                int separator = line.indexOf("=");

                if (separator == -1) {
                    continue;
                }

                String key =
                        line.substring(0, separator);

                String value =
                        line.substring(separator + 1);

                memories.put(key, value);
            }

        } catch (IOException e) {

            System.out.println(
                    "Jarvis: Could not load memory."
            );
        }
    }
}