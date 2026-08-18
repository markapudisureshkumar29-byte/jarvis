package org.example;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

public class ScreenshotService {

    public void takeScreenshot() {

        try {

            Robot robot = new Robot();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            Rectangle rectangle = new Rectangle(screenSize);

            BufferedImage image = robot.createScreenCapture(rectangle);

            String timeStamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            File folder = new File("Screenshots");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File screenshot = new File(folder,
                    "Screenshot_" + timeStamp + ".png");

            ImageIO.write(image, "png", screenshot);

            System.out.println("Jarvis: Screenshot saved to:");
            System.out.println(screenshot.getAbsolutePath());

        } catch (AWTException | java.io.IOException e) {

            System.out.println("Jarvis: Failed to take screenshot.");

        }
    }
}