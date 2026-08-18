package org.example;

import java.awt.Desktop;
import java.io.File;

public class FileService {

    public void openFile(String path) {

        try {

            File file = new File(path);

            if (!file.exists()) {
                System.out.println("Jarvis: File not found.");
                return;
            }

            Desktop.getDesktop().open(file);

            System.out.println("Jarvis: Opening file...");

        } catch (Exception e) {

            System.out.println("Jarvis: Failed to open file.");

        }
    }

    public void openFolder(String path) {

        try {

            File folder = new File(path);

            if (!folder.exists()) {
                System.out.println("Jarvis: Folder not found.");
                return;
            }

            Desktop.getDesktop().open(folder);

            System.out.println("Jarvis: Opening folder...");

        } catch (Exception e) {

            System.out.println("Jarvis: Failed to open folder.");

        }
    }
}