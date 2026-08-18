package org.example;

import java.io.IOException;

public class SystemControlService {

    public void shutdown() {
        executeCommand("shutdown", "/s", "/t", "10");
        System.out.println("Jarvis: System will shut down in 10 seconds.");
    }

    public void restart() {
        executeCommand("shutdown", "/r", "/t", "10");
        System.out.println("Jarvis: System will restart in 10 seconds.");
    }

    public void cancelShutdown() {
        executeCommand("shutdown", "/a");
        System.out.println("Jarvis: Shutdown or restart cancelled.");
    }

    private void executeCommand(String... command) {
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            System.out.println("Jarvis: Unable to execute system command.");
        }
    }
}