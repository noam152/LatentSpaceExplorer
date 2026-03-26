package gui;

import model.ILatentEntity;
import engine.SpaceManager;

import javax.swing.*; // Window and UI components (JFrame, JPanel)
import java.awt.*;    // Graphics, Colors, Fonts, and Drawing math
import java.util.ArrayList;
import java.util.List;

/**
 * SemanticScaleWindow: A detached window that projects the 3D universe onto a 1D line.
 * Extends JFrame (The actual physical window frame in the OS).
 */
public class SemanticScaleWindow extends JFrame {

    private SpaceManager spaceManager; // The Brain
    private ILatentEntity word1; // Left anchor word
    private ILatentEntity word2; // Right anchor word

    public SemanticScaleWindow(SpaceManager spaceManager, String w1, String w2) {
        this.spaceManager = spaceManager;

        // 1. Fetch the full mathematical objects from the dictionary
        this.word1 = spaceManager.getEntity(w1);
        this.word2 = spaceManager.getEntity(w2);

        // 2. Window Setup: Title, Size, Centering, and Close Operation
        setTitle("Semantic Scale: " + w1 + " <---> " + w2); // Window top bar
        setSize(1000, 400); // 1000px wide for a long scale
        setLocationRelativeTo(null); // Passing 'null' centers the window perfectly
        // CRITICAL: DISPOSE_ON_CLOSE means "Close ONLY this window, don't kill the whole app!"
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 3. Add our custom drawing canvas to the window frame
        add(new ScalePanel());
    }

    /**
     * Inner Class: ScalePanel (The actual drawing board / Canvas)
     */
    private class ScalePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clear the screen

            // Upgrade the basic brush (Graphics) to the advanced brush (Graphics2D)
            Graphics2D g2d = (Graphics2D) g;

            // Turn on Anti-Aliasing (Smooths out jagged edges on circles and text)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Get canvas dimensions and set safe padding
            int width = getWidth();
            int height = getHeight();
            int padding = 100; // 100px margins | שוליים של 100 פיקסלים

            /**
             * Step A: Math - Create the Directional Axis (Vector Subtraction)
             */

            // Extract the full 300-dimension vectors of the two anchor words
            double[] v1 = word1.getFullVector();
            double[] v2 = word2.getFullVector();

            // Create an empty vector to represent the line connecting them (The Axis)
            double[] axis = new double[v1.length];

            // Calculate direction: End Point minus Start Point (v2 - v1)
            for (int i = 0; i < v1.length; i++) {
                axis[i] = v2[i] - v1[i];
            }

            /**
             * Math - Project all words onto the Axis (Dot Product)
             */
            // A box to hold a word and its calculated score on our scale
            class ProjectedWord {
                String word;
                double score;
                public ProjectedWord(String w, double s) { this.word = w; this.score = s; }
            }

            List<ProjectedWord> projectedWords = new ArrayList<>();

            // Track min and max scores for normalization later (starting at Infinity)
            double minScore = Double.MAX_VALUE;
            double maxScore = -Double.MAX_VALUE;

            // Loop through EVERY word in the universe
            for (ILatentEntity entity : spaceManager.getAllEntities()) {
                double[] u = entity.getFullVector();
                double dotProduct = 0;

                // Dot Product: Multiply matching dimensions and sum them up.
                // This asks: "How much does this word align with our chosen axis?"
                for (int i = 0; i < u.length; i++) {
                    dotProduct += u[i] * axis[i];
                }

                // Pack it and save it
                projectedWords.add(new ProjectedWord(entity.getIdentifier(), dotProduct));

                // Update Min/Max records
                if (dotProduct < minScore) minScore = dotProduct;
                if (dotProduct > maxScore) maxScore = dotProduct;
            }

            /**
             * Drawing - The Main Horizontal Line
             */

            int lineY = height / 2; // Center of screen vertically
            g2d.setColor(Color.LIGHT_GRAY); // Light gray brush
            g2d.setStroke(new BasicStroke(3)); // Thick 3px brush tip
            g2d.drawLine(padding, lineY, width - padding, lineY); // Draw from left margin to right margin

            /**
             * Drawing - The Dots and Text
             */

            g2d.setFont(new Font("Arial", Font.PLAIN, 12)); // Small standard font

            for (ProjectedWord pw : projectedWords) {

                //Min-Max Normalization: Convert the score to a percentage (0.0 to 1.0)
                double normalizedScore = (pw.score - minScore) / (maxScore - minScore);

                // Map percentage to X screen pixels
                int xPos = padding + (int) (normalizedScore * (width - 2 * padding));

                // Jitter Trick (Y Offset): Use the word's unique HashCode to scatter it randomly up or down (Max 50px).
                // This prevents thousands of words from drawing exactly on top of each other!
                int yOffset = (Math.abs(pw.word.hashCode()) % 100) - 50;

                // 4. Draw the actual dot
                g2d.setColor(new Color(0, 0, 255, 100));
                g2d.fillOval(xPos - 3, lineY + yOffset - 3, 6, 6);

                // Clutter Filter Trick: Only draw text for 1 in every 15 words to prevent unreadable text overlap.
                // Also, ensure we don't accidentally draw our Anchor Words here.
                if (Math.abs(pw.word.hashCode()) % 15 == 0 && !pw.word.equals(word1.getIdentifier()) && !pw.word.equals(word2.getIdentifier())) {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawString(pw.word, xPos + 5, lineY + yOffset + 5);
                }
            }

            /**
             * Drawing - The Giant Anchor Words
             */

            g2d.setFont(new Font("Arial", Font.BOLD, 20)); // Big bold font

            // Left Anchor Word
            g2d.setColor(Color.RED);
            g2d.drawString(word1.getIdentifier(), 20, lineY - 20); // Print near left edge

            // Right Anchor Word
            g2d.setColor(new Color(0, 153, 0)); // Dark Green | ירוק כהה

            // Ask the font measuring tape: "How many pixels wide is this word?"
            int word2Width = g2d.getFontMetrics().stringWidth(word2.getIdentifier());

            // Subtract its width from the right edge so it aligns perfectly to the right side!
            g2d.drawString(word2.getIdentifier(), width - word2Width - 20, lineY - 20);
        }
    }
}