package engine;

import gui.MainWindow;
// Imports Java's built-in GUI toolkit (Swing).
// Used for creating windows, buttons, and managing UI threads.
import javax.swing.*;

/**
 * The entry point of the LatentSpace application.
 * Responsible for bootstrapping the engine and launching the Graphical User Interface (GUI) safely.
 */
public class Main {

    // The main method: The exact signature required by Java to start the program
    public static void main(String[] args) {

        // Create the core application engine
        LatentSpaceEngine engine = new LatentSpaceEngine();

        try {
            // Step 1: Initialize the backend (data loading, Python execution, math setup)
            engine.initialize();

            // Step 2: Launch the GUI on the Event Dispatch Thread (EDT) for thread safety
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow(engine);
                window.setVisible(true); // Boom! Displays the window
            });

        } catch (Exception e) {
            // Error handling if the engine fails to start (e.g., Python crash, missing files)
            System.err.println("Failed to start system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}