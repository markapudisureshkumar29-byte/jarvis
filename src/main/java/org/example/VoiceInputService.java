package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class VoiceInputService {

    private final WhisperService whisperService;

    private volatile boolean running = false;

    private TargetDataLine microphone;

    private Thread listeningThread;

    /*
     * Minimum RMS amplitude (0-32767 range for 16-bit audio)
     * that counts as "someone is actually speaking" rather
     * than silence or background noise.
     */
    private static final double SPEECH_ENERGY_THRESHOLD = 1500.0;

    /*
     * How many consecutive silent chunks (after speech was
     * detected) before we consider the utterance finished
     * and send it to Whisper. Each chunk is ~256ms with an
     * 8192-byte buffer at 16kHz/16-bit/mono, so 14 chunks is
     * roughly 3.5 seconds of trailing silence — generous
     * enough to allow natural mid-sentence pauses without
     * cutting the speaker off.
     */
    private static final int SILENCE_CHUNKS_TO_END_UTTERANCE = 14;

    public VoiceInputService(
            String whisperExePath,
            String whisperModelPath) {

        this.whisperService =
                new WhisperService(
                        whisperExePath,
                        whisperModelPath
                );
    }

    public interface Listener {
        void onText(String text);
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start() {

        if (running) {
            return;
        }

        running = true;

        listeningThread = new Thread(
                this::listenLoop,
                "Jarvis-Whisper-Listener"
        );

        // Daemon thread: if the microphone read() ever hangs
        // (a known javax.sound.sampled quirk on Windows where
        // a blocked TargetDataLine.read() doesn't always
        // unblock after close()), this thread must not be
        // able to block JVM shutdown. A non-daemon thread here
        // caused JARVIS to require a manual Ctrl+C (exit code
        // 130) instead of exiting cleanly after "stop" was said.
        listeningThread.setDaemon(true);

        listeningThread.start();
    }

    private void listenLoop() {

        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000.0f,
                16,
                1,
                2,
                16000.0f,
                false
        );

        DataLine.Info info =
                new DataLine.Info(
                        TargetDataLine.class,
                        format
                );

        try {

            /*
             * Find microphone.
             */
            System.out.println(
                    "Jarvis: Opening microphone..."
            );

            microphone =
                    (TargetDataLine)
                            AudioSystem.getLine(info);

            System.out.println(
                    "Jarvis: Microphone found."
            );

            /*
             * Open microphone.
             */
            microphone.open(format);

            System.out.println(
                    "Jarvis: Microphone opened."
            );

            /*
             * Start microphone.
             */
            microphone.start();

            System.out.println();
            System.out.println(
                    "Jarvis: Listening..."
            );
            System.out.println(
                    "Speak your command."
            );
            System.out.println();

            /*
             * Audio buffer for reading from the mic in chunks.
             */
            byte[] buffer =
                    new byte[8192];

            /*
             * Accumulates raw audio bytes for the current
             * utterance while the person is speaking.
             */
            ByteArrayOutputStream utteranceBuffer =
                    new ByteArrayOutputStream();

            boolean speechStarted = false;

            int consecutiveSilentChunks = 0;

            while (running) {

                int bytesRead =
                        microphone.read(
                                buffer,
                                0,
                                buffer.length
                        );

                if (bytesRead <= 0) {
                    continue;
                }

                double rms =
                        calculateRms(
                                buffer,
                                bytesRead
                        );

                boolean loudEnough =
                        rms >= SPEECH_ENERGY_THRESHOLD;

                if (loudEnough) {

                    if (!speechStarted) {

                        speechStarted = true;

                        System.out.print(
                                "\rHearing you...        "
                        );
                    }

                    consecutiveSilentChunks = 0;

                    utteranceBuffer.write(
                            buffer,
                            0,
                            bytesRead
                    );

                } else if (speechStarted) {

                    utteranceBuffer.write(
                            buffer,
                            0,
                            bytesRead
                    );

                    consecutiveSilentChunks++;

                    if (
                            consecutiveSilentChunks
                                    >= SILENCE_CHUNKS_TO_END_UTTERANCE
                    ) {

                        System.out.print(
                                "\r" + " ".repeat(40) + "\r"
                        );

                        processUtterance(
                                utteranceBuffer.toByteArray(),
                                format
                        );

                        utteranceBuffer.reset();

                        speechStarted = false;

                        consecutiveSilentChunks = 0;

                        if (running) {

                            System.out.println(
                                    "Jarvis: Listening..."
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Jarvis: Voice input failed."
            );

            System.out.println(
                    "Reason: "
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            closeMicrophone();

            running = false;
        }
    }

    /**
     * Writes the recorded utterance to a temporary .wav file
     * and sends it to WhisperService for transcription.
     */
    private void processUtterance(
            byte[] audioBytes,
            AudioFormat format) {

        if (audioBytes.length == 0) {
            return;
        }

        System.out.println(
                "Jarvis: Transcribing..."
        );

        try {

            File tempWav =
                    File.createTempFile(
                            "jarvis_utterance_",
                            ".wav"
                    );

            tempWav.deleteOnExit();

            AudioInputStream audioInputStream =
                    new AudioInputStream(
                            new ByteArrayInputStream(audioBytes),
                            format,
                            audioBytes.length / format.getFrameSize()
                    );

            AudioSystem.write(
                    audioInputStream,
                    AudioFileFormat.Type.WAVE,
                    tempWav
            );

            String text =
                    whisperService.transcribe(tempWav);

            tempWav.delete();

            if (!isRealSpeech(text)) {

                System.out.println(
                        "Jarvis: Didn't catch that."
                );

                return;
            }

            text = text.trim();

            System.out.println(
                    "You: " + text
            );

            if (listener != null) {

                listener.onText(text);
            }

        } catch (IOException e) {

            System.out.println(
                    "Jarvis: Failed to process utterance: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Whisper prints special bracketed tags such as
     * [BLANK_AUDIO], [SILENCE], [NO_SPEECH], or [MUSIC] when
     * it detects no actual speech in the audio, instead of
     * returning an empty string. These must NOT be treated as
     * real transcribed text, or JARVIS will "respond" to
     * silence/noise as if the user said something.
     */
    private boolean isRealSpeech(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized =
                text.trim().toLowerCase(Locale.ROOT);

        if (normalized.startsWith("[")
                && normalized.endsWith("]")) {

            return false;
        }

        /*
         * Guard against very short accidental noises that
         * still slipped past the energy threshold (e.g. a
         * cough or a tap), which Whisper sometimes turns into
         * a single stray word.
         */
        String lettersOnly =
                normalized.replaceAll(
                        "[^a-z]",
                        ""
                );

        if (lettersOnly.length() < 2) {
            return false;
        }

        return true;
    }

    public void stop() {

        if (!running) {
            return;
        }

        running = false;

        closeMicrophone();

        if (listeningThread != null) {

            listeningThread.interrupt();
        }

        System.out.println(
                "Jarvis: Voice mode stopped."
        );
    }

    private void closeMicrophone() {

        if (microphone != null) {

            try {

                microphone.stop();

            } catch (Exception ignored) {
            }

            try {

                microphone.close();

            } catch (Exception ignored) {
            }

            microphone = null;
        }
    }

    /*
     * Calculates the root-mean-square amplitude of a chunk
     * of 16-bit signed little-endian PCM audio.
     */
    private double calculateRms(
            byte[] buffer,
            int bytesRead) {

        long sumOfSquares = 0;

        int sampleCount = bytesRead / 2;

        if (sampleCount == 0) {
            return 0.0;
        }

        for (int i = 0; i < bytesRead - 1; i += 2) {

            int low = buffer[i] & 0xFF;
            int high = buffer[i + 1];

            short sample =
                    (short) ((high << 8) | low);

            sumOfSquares +=
                    (long) sample * (long) sample;
        }

        double meanSquare =
                (double) sumOfSquares / sampleCount;

        return Math.sqrt(meanSquare);
    }
}