package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wraps the whisper.cpp command-line executable to transcribe
 * a .wav audio file into text.
 *
 * This runs whisper-cli.exe as a subprocess (similar to how
 * VoiceService runs PowerShell for text-to-speech) and reads
 * back its printed transcription.
 *
 * IMPORTANT:
 * whisper-cli.exe prints diagnostic/loading information (model
 * loading, backend selection, audio decoding status) separately
 * from the actual transcribed text. We must NOT merge those
 * streams together, or the diagnostic noise gets mixed into
 * the text we hand off to NaturalLanguageService.
 */
public class WhisperService {

    private final String whisperExePath;

    private final String modelPath;

    public WhisperService(
            String whisperExePath,
            String modelPath) {

        this.whisperExePath = whisperExePath;

        this.modelPath = modelPath;
    }

    /**
     * Runs whisper-cli.exe against the given .wav file and
     * returns the transcribed text, or an empty string if
     * nothing could be transcribed.
     */
    public String transcribe(File wavFile) {

        try {

            ProcessBuilder builder =
                    new ProcessBuilder(
                            whisperExePath,
                            "-m", modelPath,
                            "-f", wavFile.getAbsolutePath(),
                            "-nt",
                            "-np"
                    );

            /*
             * Do NOT merge diagnostic/log output into the
             * result stream. Discard it instead, so only
             * the actual transcribed text is captured below.
             */
            builder.redirectErrorStream(false);

            builder.redirectError(
                    ProcessBuilder.Redirect.DISCARD
            );

            Process process =
                    builder.start();

            StringBuilder output =
                    new StringBuilder();

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream()
                                 )
                         )) {

                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    String trimmedLine =
                            line.trim();

                    if (!trimmedLine.isEmpty()) {

                        output.append(trimmedLine)
                                .append(" ");
                    }
                }
            }

            process.waitFor();

            return output.toString().trim();

        } catch (IOException e) {

            System.out.println(
                    "Jarvis: Whisper transcription failed (IO): "
                            + e.getMessage()
            );

            return "";

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "Jarvis: Whisper transcription was interrupted."
            );

            return "";
        }
    }
}