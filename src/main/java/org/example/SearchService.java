package org.example;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SearchService {

    public void searchGoogle(String query) {

        try {

            String url = "https://www.google.com/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Desktop.getDesktop().browse(new URI(url));

            System.out.println("Jarvis: Searching Google for \"" + query + "\"");

        } catch (Exception e) {

            System.out.println("Jarvis: Failed to search Google.");

        }

    }

    public void searchYouTube(String query) {

        try {

            String url = "https://www.youtube.com/results?search_query=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8);

            Desktop.getDesktop().browse(new URI(url));

            System.out.println("Jarvis: Searching YouTube for \"" + query + "\"");

        } catch (Exception e) {

            System.out.println("Jarvis: Failed to search YouTube.");

        }

    }
}