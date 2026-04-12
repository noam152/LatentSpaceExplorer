package engine;

import gui.MainWindow;

import javax.swing.*; // Java's built-in GUI toolkit

/**
 * The entry point of the LatentSpace application.
 * Responsible for wakes up the engine and launching the (GUI) safely.
 */
public class Main {

    public static void main(String[] args) {

        LatentSpaceEngine engine = new LatentSpaceEngine(); // Create the core application engine

        try {
            // Step 1: Initialize the core logic layer (data loading, Python execution, math setup)
            engine.initialize();

            // Step 2: Launch the GUI on the Event Dispatch Thread (EDT) for thread safety
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow(engine);
                window.setVisible(true); // Displays the window
            });

        } catch (Exception e) {
            // Error handling if the engine fails to start (Python crash, missing files)
            System.err.println("Failed to start system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}