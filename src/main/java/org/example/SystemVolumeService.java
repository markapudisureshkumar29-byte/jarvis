package org.example;

import java.io.IOException;

public class SystemVolumeService {

    public void volumeUp() {

        sendMediaKey("0xAF");

        System.out.println(
                "Jarvis: Volume increased."
        );
    }

    public void volumeDown() {

        sendMediaKey("0xAE");

        System.out.println(
                "Jarvis: Volume decreased."
        );
    }

    public void mute() {

        sendMediaKey("0xAD");

        System.out.println(
                "Jarvis: Volume muted."
        );
    }

    public void unmute() {

        sendMediaKey("0xAD");

        System.out.println(
                "Jarvis: Volume unmuted."
        );
    }

    private void sendMediaKey(String keyCode) {

        String command =
                "$wsh = New-Object -ComObject WScript.Shell; " +
                        "$wsh.SendKeys([char]" + keyCode + ");";

        try {

            new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    command
            ).start();

        } catch (IOException e) {

            System.out.println(
                    "Jarvis: Could not control system volume."
            );
        }
    }
}