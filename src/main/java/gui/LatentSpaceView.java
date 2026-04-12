package gui;

import model.ILatentEntity;
import engine.SpaceManager;
import gui.rendering.IVisualEntity;
import engine.IObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; // Helper for physical mouse clicks
import java.awt.event.MouseEvent; // Holds click data (X,Y pixels)
import java.util.function.Consumer; // Import Java's built-in callback interface
import java.util.ArrayList;
import java.util.List;

/**
 * The visual canvas that draws the universe.
 */
public class LatentSpaceView extends JPanel implements IObserver {

    private SpaceManager spaceManager;
    private String selectedWord = null;
    private double zoomLevel = 4.0; // Keeps track of how far the user has dragged the canvas.
    private int panOffsetX = 0;
    private int panOffsetY = 0;
    private Point lastMousePoint = null; // Remembers the exact pixel where the user started dragging.
    private Consumer<String> onWordSelected; // A "box" to hold the action we want to run when a word is clicked.
    private List<ProjectedEntity> cachedProjectedEntities = new ArrayList<>();
    private boolean needsRecalculation = true; // Flag to trigger update only when necessary

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

        // Unified Mouse Handler for Clicks & Drags
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Keep the exact same click function from before for word selection
                handleMouseClick(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // The moment the user clicks down, record the starting point for dragging
                lastMousePoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    // Calculate how many pixels the mouse moved since the last frame
                    int dx = e.getX() - lastMousePoint.x;
                    int dy = e.getY() - lastMousePoint.y;

                    // Update the global pan offsets
                    panOffsetX += dx;
                    panOffsetY += dy;
                    needsRecalculation = true; // Position changed

                    // Update the reference point for the next frame of dragging
                    lastMousePoint = e.getPoint();

                    repaint();
                }
            }
        };

        // Attach this smart handler to listen for both clicks and drags!
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);

        // Improved Mouse Wheel Zoom for Trackpads & Mice
        this.addMouseWheelListener(e -> {
            // getPreciseWheelRotation catches the tiny, smooth fractions of a laptop trackpad
            double rotation = e.getPreciseWheelRotation();

            // Calculate exact scale multiplier (very smooth math)
            double scaleFactor = Math.pow(1.1, rotation);
            zoomLevel *= scaleFactor;
            needsRecalculation = true; // Zoom changed

            //  Prevent the universe from collapsing into a dot or zooming out to infinity
            if (zoomLevel < 0.2) zoomLevel = 0.2;     // Max Zoom IN limit
            if (zoomLevel > 20.0) zoomLevel = 20.0;   // Max Zoom OUT limit

            repaint();
        });
    }

    /**
     * The Observer Update Method
     */
    @Override
    public void update() {
        needsRecalculation = true;
        repaint();
    }

    /**
     * The Projection Engine
     */

    private double[] transformVector(double[] pcaVec) {
        return spaceManager.getProjectionState().transform(pcaVec, spaceManager);
    }

    /**
     * Mouse Click Detection
     */
    // Finds which word was clicked
    private void handleMouseClick(int mouseX, int mouseY) {
        String closestWord = null;
        double minDist = Double.MAX_VALUE;


        for (ILatentEntity entity : spaceManager.getAllEntities()) {
            if (entity.getPCAVector() == null) continue;// Skip words that don't have a mathematical vector

            // Figure out exactly where this word is currently drawn on the screen
            double[] projected = transformVector(entity.getPCAVector());
            // Translate Math (X,Y) to physical Screen Pixels (X gets true, Y gets 'false')
            int screenX = getScreenCoordinate(projected[0], getWidth(), zoomLevel, true);
            int screenY = getScreenCoordinate(projected[1], getHeight(), zoomLevel, false);

            // Calculate exact pixel distance between click and the word (Pythagoras)
            double dist = Math.sqrt(Math.pow(mouseX - screenX, 2) + Math.pow(mouseY - screenY, 2));

            // If it's close enough (< 15px) and the closest found so far, save it
            if (dist < 15 && dist < minDist) {
                minDist = dist;
                closestWord = entity.getIdentifier();
            }
        }

        selectedWord = closestWord;

        // Check if the "box" is not empty.
        if (onWordSelected != null) {
            // Run the action inside the box, passing it the word we just clicked.
            onWordSelected.accept(selectedWord);
        }

        repaint();
    }

    /**
     * The Canvas
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clear the board
        g.setFont(VisualFactory.DEFAULT_FONT);

        // Only calculate math if something actually changed (Zoom, Pan, Axis)
        // If nothing changed, we skip the heavy loop and go straight to drawing.
        if (needsRecalculation || cachedProjectedEntities.isEmpty()) {
            updateProjectionCache();
        }

        // KNN Lines logic
        List<ILatentEntity> neighbors = null;
        if (selectedWord != null) {
            try {
                neighbors = spaceManager.getKNearestNeighbors(selectedWord, spaceManager.getKNeighbors());
                Graphics2D g2d = (Graphics2D) g;

                // Find the screen position of our selected word from the cache
                ProjectedEntity centerNode = null;
                for (ProjectedEntity pe : cachedProjectedEntities) {
                    if (pe.entity.getIdentifier().equals(selectedWord)) {
                        centerNode = pe;
                        break;
                    }
                }

                if (centerNode != null && neighbors != null) {
                    g2d.setColor(new Color(0, 200, 0, 100));
                    g2d.setStroke(new BasicStroke(1.5f));

                    for (ILatentEntity neighbor : neighbors) {
                        for (ProjectedEntity pe : cachedProjectedEntities) {
                            if (pe.entity.getIdentifier().equals(neighbor.getIdentifier())) {
                                g2d.drawLine(centerNode.screenX, centerNode.screenY, pe.screenX, pe.screenY);
                                break;
                            }
                        }
                    }
                    g2d.setStroke(new BasicStroke(1));
                }
            } catch (Exception e) {
                System.err.println("Critical Error in KNN visualization: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Flyweight Brushes
        IVisualEntity highlightedPoint = VisualFactory.getHighlightedEntity();

        // 3. Zoom/Label Logic
        boolean is3D = !spaceManager.getProjectionState().is2D();
        double showLabelsThreshold = 0.5;
        boolean shouldShowLabels = zoomLevel < showLabelsThreshold;

        // the main drawing loop
        for (ProjectedEntity p : cachedProjectedEntities) {
            int radius = 8;
            int alpha = 160;

            // Make it feel like real space: bigger and solid if close, smaller and faint if far away.
            if (is3D) {
                radius = (int) (8 + (p.depth * 3));
                radius = Math.max(2, Math.min(radius, 18));
                alpha = Math.max(30, Math.min(255, 40 + (radius * 12)));
            }

            if (selectedWord != null && p.entity.getIdentifier().equals(selectedWord)) {
                highlightedPoint.draw(g, p.screenX, p.screenY, p.entity.getIdentifier());

            } else if (neighbors != null && neighbors.contains(p.entity)) {
                g.setColor(new Color(128, 0, 128, alpha)); // Purple for neighbors
                g.fillOval(p.screenX - (radius / 2), p.screenY - (radius / 2), radius, radius);
                if (shouldShowLabels) {
                    g.drawString(p.entity.getIdentifier(), p.screenX + 5, p.screenY - 5);
                }
            } else {
                g.setColor(new Color(0, 0, 255, alpha)); // Blue for background
                g.fillOval(p.screenX - (radius / 2), p.screenY - (radius / 2), radius, radius);
                if (shouldShowLabels) {
                    g.setColor(new Color(0, 0, 0, alpha));
                    g.drawString(p.entity.getIdentifier(), p.screenX + 5, p.screenY - 5);
                }
            }
        }
    }

    /**
     * Math to Pixels Converter
     */
    // Converts the Engine's math coordinates to actual screen pixels.
    private int getScreenCoordinate(double pcaVal, int screenDimension, double zoomLevel, boolean isXAxis) {
        if (isXAxis) {
            // X-Axis: Start at screen center, add the zoomed math value, and finally add camera panning.
            return (int) ((screenDimension / 2) + (pcaVal / zoomLevel) * (screenDimension / 2))+panOffsetX;
        } else {
            // Y-Axis: reversed, math goes up, but screen pixels go DOWN. So we subtract the math value.
            return (int) ((screenDimension / 2) - (pcaVal / zoomLevel) * (screenDimension / 2))+panOffsetY;
        }
    }

    /**
     * Search and Select. Finds a word in the engine and highlights it,
     */
    public ILatentEntity searchAndSelectWord(String searchWord) {
        if (searchWord == null || searchWord.trim().isEmpty()) return null;

        ILatentEntity targetEntity = null;

        // Search through all entities
        for (ILatentEntity entity : spaceManager.getAllEntities()) {
            if (entity.getIdentifier().equalsIgnoreCase(searchWord.trim())) {
                targetEntity = entity;
                break;
            }
        }

        // Not found handling
        if (targetEntity == null) {
            JOptionPane.showMessageDialog(this,
                    "Word '" + searchWord + "' not found in the latent space!",
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
            return null; // Stop here and return nothing
        }

        // Selection: Tell the system this is now the "Clicked" word.
        selectedWord = targetEntity.getIdentifier();

        // The Callback Execution (Notify the main app to update KNN)
        if (onWordSelected != null) {
            onWordSelected.accept(selectedWord);
        }

        repaint();

        return targetEntity; // Return the math object we found
    }

    /**
     *  Camera Auto-Center & Zoom. Takes a specific word and teleports the camera to center on it.
     */
    public void zoomAndCenterOnEntity(ILatentEntity targetEntity) {
        if (targetEntity == null || targetEntity.getPCAVector() == null) return;

        // Apply Zoom first
        zoomLevel = 0.3;

        // Math to Pixels converter
        double[] projected = transformVector(targetEntity.getPCAVector());
        int screenDimX = getWidth();
        int screenDimY = getHeight();

        // Calculate how many pixels the word is away from the absolute center
        double distanceXFromCenter = (projected[0] / zoomLevel) * (screenDimX / 2);
        double distanceYFromCenter = (projected[1] / zoomLevel) * (screenDimY / 2);

        panOffsetX = (int) -distanceXFromCenter; // X-Axis: Gets a minus to push the map left if the word is on the right.
        panOffsetY = (int) distanceYFromCenter; // Y-Axis: No minus, Computer screens draw Y upside-down, so it cancels out naturally.
        needsRecalculation = true;

        repaint();
    }

    // method that allows the MainWindow to hand us the action to put in the "box".
    public void setOnWordSelected(Consumer<String> listener) {
        this.onWordSelected = listener;
    }

    public String getSelectedWord() {
        return selectedWord;
    }

    /**
     * Converts math vectors to screen pixels and sorts them by depth.
     */
    private void updateProjectionCache() {
        cachedProjectedEntities.clear();

        for (ILatentEntity entity : spaceManager.getAllEntities()) {
            if (entity.getPCAVector() == null) continue;

            // Transform the static PCA coordinates into dynamic view coordinates.
            double[] projected = transformVector(entity.getPCAVector());

            // Convert math coordinates to physical screen pixels
            int screenX = getScreenCoordinate(projected[0], getWidth(), zoomLevel, true);
            int screenY = getScreenCoordinate(projected[1], getHeight(), zoomLevel, false);

            // Store in cache. Depth (projected[2]) is used for 3D sorting later.
            cachedProjectedEntities.add(new ProjectedEntity(entity, screenX, screenY, projected[2]));
        }

        // Sort by depth so distant points are drawn first
        cachedProjectedEntities.sort((a, b) -> Double.compare(a.depth, b.depth));

        needsRecalculation = false;
    }
}