
        package org.example;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class VoiceService {

    private final KokoroTTSClient kokoro;

    private boolean enabled = true;

    public VoiceService() {
        this.kokoro = new KokoroTTSClient();
    }

    /**
     * Enables JARVIS voice output.
     */
    public void enable() {
        enabled = true;

        System.out.println(
                "Jarvis: Voice enabled."
        );
    }

    /**
     * Disables JARVIS voice output.
     */
    public void disable() {
        enabled = false;

        System.out.println(
                "Jarvis: Voice disabled."
        );
    }

    /**
     * Speaks the supplied text using Kokoro TTS.
     *
     * If Kokoro fails, Windows System.Speech
     * is used as a fallback.
     */
    public void speak(String text) {

        if (!enabled) {
            return;
        }

        if (text == null || text.isBlank()) {
            return;
        }

        // Try Kokoro first
        try {

            byte[] wav =
                    kokoro.synthesize(text);

            playWav(wav);

            return;

        } catch (Exception e) {

            System.err.println();
            System.err.println(
                    "========== KOKORO TTS ERROR =========="
            );

            System.err.println(
                    "Exception: "
                            + e.getClass().getName()
            );

            System.err.println(
                    "Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "======================================"
            );

            System.err.println(
                    "Jarvis: Kokoro unavailable."
            );

            System.err.println(
                    "Jarvis: Falling back to Windows Speech."
            );

            System.err.println();
        }

        // Windows fallback
        try {

            speakWindows(text);

        } catch (Exception e) {

            System.err.println(
                    "Windows System.Speech also failed."
            );

            e.printStackTrace();
        }
    }

    /**
     * Plays WAV audio returned by Kokoro.
     *
     * Uses SourceDataLine instead of Clip because
     * some Kokoro WAV streams are not handled reliably
     * by Java's Clip implementation.
     */
    private void playWav(byte[] wav)
            throws IOException,
            UnsupportedAudioFileException,
            LineUnavailableException {

        if (wav == null || wav.length == 0) {

            throw new IOException(
                    "Kokoro returned empty audio data."
            );
        }

        try (
                AudioInputStream audioStream =
                        AudioSystem.getAudioInputStream(
                                new ByteArrayInputStream(wav)
                        )
        ) {

            AudioFormat format =
                    audioStream.getFormat();

            System.out.println(
                    "Jarvis: Kokoro audio format: "
                            + format
            );

            DataLine.Info info =
                    new DataLine.Info(
                            SourceDataLine.class,
                            format
                    );

            if (!AudioSystem.isLineSupported(info)) {

                throw new LineUnavailableException(
                        "Audio format is not supported: "
                                + format
                );
            }

            try (
                    SourceDataLine line =
                            (SourceDataLine)
                                    AudioSystem.getLine(info)
            ) {

                line.open(format);
                line.start();

                byte[] buffer =
                        new byte[4096];

                int bytesRead;

                while (
                        (bytesRead =
                                audioStream.read(buffer))
                                != -1
                ) {

                    if (bytesRead > 0) {

                        line.write(
                                buffer,
                                0,
                                bytesRead
                        );
                    }
                }

                line.drain();
            }
        }
    }

    /**
     * Windows built-in speech fallback.
     */
    private void speakWindows(String text)
            throws IOException,
            InterruptedException {

        String escapedText =
                text.replace(
                        "'",
                        "''"
                );

        String powershellCommand =
                "Add-Type -AssemblyName System.Speech;"
                        + "$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer;"
                        + "$speaker.Speak('"
                        + escapedText
                        + "');";

        Process process =
                new ProcessBuilder(
                        "powershell.exe",
                        "-NoProfile",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-Command",
                        powershellCommand
                )
                        .redirectErrorStream(true)
                        .start();

        process.waitFor();
    }
}

