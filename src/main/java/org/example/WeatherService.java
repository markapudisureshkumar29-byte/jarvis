package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void getWeather(String city) {

        try {

            String encodedCity = city.trim().replace(" ", "%20");

            String url = "https://wttr.in/" + encodedCity + "?format=3";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 200) {

                System.out.println("Jarvis: " + response.body());

            } else {

                System.out.println(
                        "Jarvis: Unable to get weather for " + city + "."
                );
            }

        } catch (IOException | InterruptedException e) {

            System.out.println("Jarvis: Weather service is currently unavailable.");

        }
    }
}