package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeService {

    public String getCurrentTime() {

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("hh:mm:ss a");

        return now.format(formatter);
    }

    public String getCurrentDate() {

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd MMMM yyyy");

        return now.format(formatter);
    }
}