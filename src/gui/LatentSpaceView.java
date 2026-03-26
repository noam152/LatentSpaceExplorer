package gui;

import model.ILatentEntity;
import engine.SpaceManager;
import gui.redering.BaseVisualEntity;
import gui.redering.HighlightDecorator;
import gui.redering.IVisualEntity;
import engine.IObserver;

import javax.swing.*; // The Swing library for UI components
import java.awt.*; // Abstract Window Toolkit (Drawing, Colors, Fonts)
import java.awt.event.MouseAdapter; // Helper for physical mouse clicks
import java.awt.event.MouseEvent; // Holds click data (X,Y pixels)
import java.util.ArrayList;
import java.util.List;

/**
 * LatentSpaceView: The visual canvas that draws the 3D word universe.
 */
public class LatentSpaceView extends JPanel implements IObserver {

    private SpaceManager spaceManager;
    private String selectedWord = null; // The currently clicked word

    /**
     * Helper Class: ProjectedEntity
     */
    // A data container for a word's physical 2D screen position and 3D depth.
    private class ProjectedEntity {
        ILatentEntity entity;
        int screenX, screenY; // Position in pixels
        double depth; // The Z-axis value (Depth)

        ProjectedEntity(ILatentEntity entity, int screenX, int screenY, double depth) {
            this.entity = entity;
            this.screenX = screenX;
            this.screenY = screenY;
            this.depth = depth;
        }
    }

    public LatentSpaceView(SpaceManager spaceManager) {
        this.spaceManager = spaceManager;
        this.spaceManager.addObserver(this); // Listen to Engine updates
        setBackground(new Color(240, 240, 240)); // Light gray background

        // Listen for clicks to detect word selection
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY()); // Send pixels to search function
            }
        });
    }

    /**
     * The Observer Update Method
     */
    @Override
    public void update() {
        // Redraw everything when the Engine data changes
        repaint();
    }

    /**
     * The Projection Engine (Math to Screen)
     * Handles both 2D flat projection and 3D rotation math.
     * HEB: מנוע התצוגה - מטפל במתמטיקה של 2D ו-3D, כולל סיבוב המצלמה אם צריך.
     */
    private double[] transformVector(double[] pcaVec) {

        // Get X and Y axes indices from the SpaceManager
        int xAxis = spaceManager.getXAxisIndex();
        int yAxis = spaceManager.getYAxisIndex();

        double x0 = 0.0, y0 = 0.0; // Prepare empty variables

        // Safely extract values from the PCA vector
        if (pcaVec.length > xAxis) { x0 = pcaVec[xAxis]; }
        if (pcaVec.length > yAxis) { y0 = pcaVec[yAxis]; }

        // THE 2D MODE LOGIC -  If we are in 2D mode, we don't care about Z and we don't rotate.
        if (spaceManager.is2DMode()) {
            return new double[]{x0, y0, 0.0}; // Return flat coordinates (Z is zero)
        }

        //  THE 3D MODE LOGIC - We are in 3D Mode! Get the Z axis index and value.
        int zAxis = spaceManager.getZAxisIndex();
        double z0 = 0.0;
        if (pcaVec.length > zAxis) { z0 = pcaVec[zAxis]; }

        // Get the current camera rotation angles (Pitch and Yaw)
        double rotX = spaceManager.getRotationX();
        double rotY = spaceManager.getRotationY();

        // Carousel Rotation (Yaw - around Y axis)
        double x1 = x0 * Math.cos(rotY) + z0 * Math.sin(rotY);
        double z1 = -x0 * Math.sin(rotY) + z0 * Math.cos(rotY);
        double y1 = y0;

        // Ferris Wheel Rotation (Pitch - around X axis)
        double y2 = y1 * Math.cos(rotX) - z1 * Math.sin(rotX);
        double z2 = y1 * Math.sin(rotX) + z1 * Math.cos(rotX);
        double x2 = x1;

        // Return the final 3D coordinates!
        return new double[]{x2, y2, z2};
    }

    /**
     * Mouse Click Detection
     */
    // Finds which word was clicked based on Pythagorean distance.
    private void handleMouseClick(int mouseX, int mouseY) {
        double maxAbsValue = 4.0; // Set the zoom level (universe boundaries from -4 to +4)
        String closestWord = null; // holds the name of the closest word found so far
        double minDist = Double.MAX_VALUE; // The "Record" - initialized to Infinity so the first word easily breaks

        // Iterate through all words to find the closest one to the mouse pointer
        for (ILatentEntity entity : spaceManager.getAllEntities()) {
            if (entity.getPCAVector() == null) continue;// Skip words that don't have a mathematical vector

            // Figure out exactly where this word is currently drawn on the screen
            double[] projected = transformVector(entity.getPCAVector());
            // Translate Math (X,Y) to physical Screen Pixels (X gets true, Y gets 'false')
            int screenX = getScreenCoordinate(projected[0], getWidth(), maxAbsValue, true);
            int screenY = getScreenCoordinate(projected[1], getHeight(), maxAbsValue, false);

            // Calculate exact pixel distance between click and the word (Pythagoras)
            double dist = Math.sqrt(Math.pow(mouseX - screenX, 2) + Math.pow(mouseY - screenY, 2));

            // If it's close enough (< 15px) and the closest found so far, save it
            if (dist < 15 && dist < minDist) {
                minDist = dist;
                closestWord = entity.getIdentifier();
            }
        }

        selectedWord = closestWord;
        repaint(); // Force redraw to show highlighted word
    }

    /**
     * Main Rendering Loop (The Canvas)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clear the board
        double maxAbsValue = 4.0;
        g.setFont(VisualFactory.DEFAULT_FONT); // Set global font

        // Calculate 3D position for all words and store them
        List<ProjectedEntity> allProjected = new ArrayList<>();
        for (ILatentEntity entity : spaceManager.getAllEntities()) {
            if (entity.getPCAVector() == null) continue;

            double[] projected = transformVector(entity.getPCAVector());
            int screenX = getScreenCoordinate(projected[0], getWidth(), maxAbsValue, true);
            int screenY = getScreenCoordinate(projected[1], getHeight(), maxAbsValue, false);
            // Pack the word, its 2D screen pixels (X,Y), and its 3D depth (Z) into a container and add to the list
            allProjected.add(new ProjectedEntity(entity, screenX, screenY, projected[2]));
        }

        // Painter's Algorithm: Sort by depth (Z) so close words cover far words
        allProjected.sort((a, b) -> Double.compare(a.depth, b.depth));

        // Draw lines connecting the selected word to its K-Nearest Neighbors
        List<ILatentEntity> neighbors = null;
        if (selectedWord != null) {
            try {
                neighbors = spaceManager.getKNearestNeighbors(selectedWord, spaceManager.getKNeighbors());// Ask the Engine for the closest words mathematically
                ProjectedEntity selProj = allProjected.stream()
                        .filter(p -> p.entity.getIdentifier().equals(selectedWord))
                        .findFirst().orElse(null);

                if (selProj != null) { // If we found it on the screen, prepare to draw lines
                    g.setColor(new Color(200, 200, 200, 150)); // Semi-transparent lines
                    for (ILatentEntity neighbor : neighbors) {// Find where this specific neighbor is drawn on the screen
                        ProjectedEntity nProj = allProjected.stream()
                                .filter(p -> p.entity.equals(neighbor))
                                .findFirst().orElse(null);
                        if (nProj != null) {// Draw a line connecting the selected word to the neighbor
                            g.drawLine(selProj.screenX, selProj.screenY, nProj.screenX, nProj.screenY); // Draw line
                        }
                    }
                }
            } catch (Exception e) {} // Failsafe if word not found
        }

        // 4. Draw the actual dots and text using the Decorator Pattern
        IVisualEntity basePoint = new BaseVisualEntity(); // Standard brush
        IVisualEntity highlightedPoint = new HighlightDecorator(basePoint); // Laser brush

        for (ProjectedEntity p : allProjected) {
            if (selectedWord != null && p.entity.getIdentifier().equals(selectedWord)) {
                // The clicked word gets the Red Laser highlight
                highlightedPoint.draw(g, p.screenX, p.screenY, p.entity.getIdentifier());
            } else if (neighbors != null && neighbors.contains(p.entity)) {
                // A neighbor word gets a special Purple color
                g.setColor(new Color(128, 0, 128));
                g.fillOval(p.screenX - 4, p.screenY - 4, 8, 8);
                g.drawString(p.entity.getIdentifier(), p.screenX + 5, p.screenY - 5);
            } else {
                // A normal word in the background (Blue point)
                basePoint.draw(g, p.screenX, p.screenY, p.entity.getIdentifier());
            }
        }
    }

    /**
     * Math to Pixels Converter
     */
    // Converts the Engine's math coordinates (e.g., 1.5) to actual screen pixels (e.g., 450px).
    private int getScreenCoordinate(double pcaVal, int screenDimension, double maxAbsValue, boolean isXAxis) {
        if (isXAxis) {
            // X-Axis: Center + (Percentage from center) * Half-screen size
            return (int) ((screenDimension / 2) + (pcaVal / maxAbsValue) * (screenDimension / 2));
        } else {
            // Y-Axis: Reversed! In computer graphics, pixel 0 is at the TOP, not bottom.
            return (int) ((screenDimension / 2) - (pcaVal / maxAbsValue) * (screenDimension / 2));
        }
    }
}