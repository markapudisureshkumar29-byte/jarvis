package org.example;

public class SystemInfoService {

    public void showSystemInfo() {

        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        System.out.println("================================");
        System.out.println("        JARVIS SYSTEM INFO");
        System.out.println("================================");

        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("OS Version: " + System.getProperty("os.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("CPU Cores: " + runtime.availableProcessors());
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Used Memory: " + usedMemory + " MB");
        System.out.println("Total Memory: " + totalMemory + " MB");

        System.out.println("================================");
    }
}