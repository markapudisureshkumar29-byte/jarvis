package org.example;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        JarvisEngine engine = new JarvisEngine();

        System.out.println("================================");
        System.out.println("       JARVIS AI ASSISTANT");
        System.out.println("================================");
        System.out.println("1. Text Mode");
        System.out.println("2. Voice Mode");
        System.out.print("Select mode (1/2): ");

        String choice = scanner.nextLine().trim();

        switch (choice) {

            case "1" -> engine.startTextMode();

            case "2" -> engine.startVoiceMode();

            default -> {
                System.out.println(
                        "Invalid choice. Starting text mode."
                );
                engine.startTextMode();
            }
        }

        scanner.close();
    }
}