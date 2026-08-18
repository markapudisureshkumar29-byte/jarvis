
package org.example;

public class CommandProcessor {

    private final TimeService timeService = new TimeService();
    private final ApplicationService applicationService =
            new ApplicationService();
    private final SearchService searchService =
            new SearchService();
    private final FileService fileService =
            new FileService();
    private final WebsiteService websiteService =
            new WebsiteService();
    private final ScreenshotService screenshotService =
            new ScreenshotService();
    private final SystemInfoService systemInfoService =
            new SystemInfoService();
    private final CalculatorService calculatorService =
            new CalculatorService();
    private final WeatherService weatherService =
            new WeatherService();
    private final VoiceService voiceService =
            new VoiceService();
    private final MemoryService memoryService;
    private final SystemControlService systemControlService =
            new SystemControlService();
    private final SystemVolumeService systemVolumeService =
            new SystemVolumeService();

    public CommandProcessor(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void processCommand(String command) {

        command = command.trim().toLowerCase();

        // MEMORY

        if (command.startsWith("remember ")) {

            String memory = command.substring(9).trim();

            int separator = memory.indexOf(" is ");

            if (separator == -1) {

                String message =
                        "Use this format: remember my name is Boss";

                System.out.println("Jarvis: " + message);
                voiceService.speak(message);
                return;
            }

            String key =
                    memory.substring(0, separator).trim();

            String value =
                    memory.substring(separator + 4).trim();

            memoryService.remember(key, value);

            voiceService.speak(
                    "I'll remember that."
            );

            return;
        }

        if (command.startsWith("what is my ")) {

            String key =
                    command.substring(11).trim();

            String value =
                    memoryService.recall(key);

            if (value == null) {

                String message =
                        "I don't remember your " + key + ".";

                System.out.println(
                        "Jarvis: " + message
                );

                voiceService.speak(message);

            } else {

                String message =
                        "Your " + key + " is " + value + ".";

                System.out.println(
                        "Jarvis: " + message
                );

                voiceService.speak(message);
            }

            return;
        }

        if (command.startsWith("forget my ")) {

            String key =
                    command.substring(10).trim();

            memoryService.forget(key);

            return;
        }

        // VOICE

        if (command.equals("voice on")) {

            voiceService.enable();
            voiceService.speak("Voice enabled.");
            return;
        }

        if (command.equals("voice off")) {

            voiceService.disable();

            System.out.println(
                    "Jarvis: Voice disabled."
            );

            return;
        }

        // VOLUME

        if (command.equals("volume up")) {

            systemVolumeService.volumeUp();

            voiceService.speak(
                    "Volume increased."
            );

            return;
        }

        if (command.equals("volume down")) {

            systemVolumeService.volumeDown();

            voiceService.speak(
                    "Volume decreased."
            );

            return;
        }

        if (command.equals("mute")) {

            systemVolumeService.mute();

            voiceService.speak(
                    "Volume muted."
            );

            return;
        }

        if (command.equals("unmute")) {

            systemVolumeService.unmute();

            voiceService.speak(
                    "Volume unmuted."
            );

            return;
        }

        // SYSTEM CONTROL

        if (command.equals("shutdown")) {

            systemControlService.shutdown();

            voiceService.speak(
                    "The computer will shut down in ten seconds."
            );

            return;
        }

        if (command.equals("restart")) {

            systemControlService.restart();

            voiceService.speak(
                    "The computer will restart in ten seconds."
            );

            return;
        }

        if (command.equals("cancel shutdown")) {

            systemControlService.cancelShutdown();

            voiceService.speak(
                    "Shutdown cancelled."
            );

            return;
        }

        // HELP

        if (command.equals("help")) {

            showHelp();

            voiceService.speak(
                    "Here are my available commands."
            );

            return;
        }

        // SCREENSHOT

        if (command.equals("screenshot")) {

            screenshotService.takeScreenshot();

            voiceService.speak(
                    "Screenshot saved successfully."
            );

            return;
        }

        // SYSTEM INFO

        if (command.equals("system info")) {

            systemInfoService.showSystemInfo();

            voiceService.speak(
                    "System information displayed."
            );

            return;
        }

        // DATE

        if (command.equals("date")
                || command.equals("today")) {

            String currentDate =
                    timeService.getCurrentDate();

            String message =
                    "Today is " + currentDate + ".";

            System.out.println(
                    "Jarvis: " + message
            );

            voiceService.speak(message);

            return;
        }

        // CALCULATOR

        if (command.startsWith("calculate ")) {

            String expression =
                    command.substring(10).trim();

            calculatorService.calculate(
                    expression
            );

            voiceService.speak(
                    "Calculation completed."
            );

            return;
        }

        // WEATHER

        if (command.startsWith("weather ")) {

            String city =
                    command.substring(8).trim();

            weatherService.getWeather(city);

            voiceService.speak(
                    "Weather information retrieved for "
                            + city
            );

            return;
        }

        if (command.equals("weather")) {

            String message =
                    "Please specify a city. For example, weather Delhi.";

            System.out.println(
                    "Jarvis: " + message
            );

            voiceService.speak(message);

            return;
        }

        // WEBSITE

        if (command.startsWith("open website ")) {

            String website =
                    command.substring(13).trim();

            websiteService.openWebsite(
                    website
            );

            voiceService.speak(
                    "Opening " + website
            );

            return;
        }

        // FILE

        if (command.startsWith("open file ")) {

            String path =
                    command.substring(10).trim();

            fileService.openFile(path);

            voiceService.speak(
                    "Opening file."
            );

            return;
        }

        // FOLDER

        if (command.startsWith("open folder ")) {

            String path =
                    command.substring(12).trim();

            fileService.openFolder(path);

            voiceService.speak(
                    "Opening folder."
            );

            return;
        }

        // APPLICATION

        if (command.startsWith("open ")) {

            String application =
                    command.substring(5).trim();

            applicationService.openApplication(
                    application
            );

            voiceService.speak(
                    "Opening " + application
            );

            return;
        }

        // GOOGLE

        if (command.startsWith("search google ")) {

            String query =
                    command.substring(14).trim();

            searchService.searchGoogle(
                    query
            );

            voiceService.speak(
                    "Searching Google for " + query
            );

            return;
        }

        // YOUTUBE

        if (command.startsWith("search youtube ")) {

            String query =
                    command.substring(15).trim();

            searchService.searchYouTube(
                    query
            );

            voiceService.speak(
                    "Searching YouTube for " + query
            );

            return;
        }

        // BASIC COMMANDS

        switch (command) {

            case "hello":

                System.out.println(
                        "Jarvis: Hello Boss!"
                );

                voiceService.speak(
                        "Hello Boss!"
                );

                break;

            case "how are you":

                System.out.println(
                        "Jarvis: I am functioning perfectly."
                );

                voiceService.speak(
                        "I am functioning perfectly."
                );

                break;

            case "who are you":

                System.out.println(
                        "Jarvis: I am your personal AI assistant."
                );

                voiceService.speak(
                        "I am your personal AI assistant."
                );

                break;

            case "time":

                String currentTime =
                        timeService.getCurrentTime();

                System.out.println(
                        "Jarvis: The current time is "
                                + currentTime
                );

                voiceService.speak(
                        "The current time is "
                                + currentTime
                );

                break;

            case "bye":

                System.out.println(
                        "Jarvis: Goodbye Boss!"
                );

                voiceService.speak(
                        "Goodbye Boss!"
                );

                break;

            default:

                System.out.println(
                        "Jarvis: Sorry, I don't understand that command."
                );

                voiceService.speak(
                        "Sorry, I don't understand that command."
                );
        }
    }

    private void showHelp() {

        System.out.println();
        System.out.println(
                "================================"
        );
        System.out.println(
                "        JARVIS COMMANDS"
        );
        System.out.println(
                "================================"
        );

        System.out.println("hello");
        System.out.println("how are you");
        System.out.println("who are you");

        System.out.println("time");
        System.out.println("date");
        System.out.println("today");

        System.out.println("system info");
        System.out.println("screenshot");

        System.out.println("calculate 25 + 17");
        System.out.println("weather Delhi");

        System.out.println("open chrome");
        System.out.println("open notepad");
        System.out.println("open calculator");

        System.out.println("open website google");
        System.out.println("open website youtube");
        System.out.println("open website github");

        System.out.println("open file <path>");
        System.out.println("open folder <path>");

        System.out.println(
                "search google <query>"
        );

        System.out.println(
                "search youtube <query>"
        );

        System.out.println(
                "remember my name is Boss"
        );

        System.out.println(
                "what is my name"
        );

        System.out.println(
                "forget my name"
        );

        System.out.println("volume up");
        System.out.println("volume down");
        System.out.println("mute");
        System.out.println("unmute");

        System.out.println("voice on");
        System.out.println("voice off");

        System.out.println("shutdown");
        System.out.println("restart");
        System.out.println("cancel shutdown");

        System.out.println("bye");

        System.out.println(
                "================================"
        );

        System.out.println();
    }
}