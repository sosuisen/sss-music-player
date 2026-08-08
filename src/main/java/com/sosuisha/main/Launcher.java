package com.sosuisha.main;

import javafx.application.Application;

/**
 * Entry point of the application.
 * This launcher class starts the JavaFX runtime from the classpath.
 */
public class Launcher {
    /**
     * Starts the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
