package org.example;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Plays WAV audio data through the default speakers.
 */
public class AudioPlayer {

    public void play(byte[] wavData) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(wavData);
             AudioInputStream ais = AudioSystem.getAudioInputStream(bais)) {

            AudioFormat format = ais.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = ais.read(buffer)) != -1) {
                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
            }
        }
    }
}