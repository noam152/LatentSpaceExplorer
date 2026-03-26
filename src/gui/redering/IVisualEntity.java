package gui.redering;

import java.awt.*;

/**
 * IVisualEntity: The Drawing Contract
 */
public interface IVisualEntity {

    /**
     * The core drawing command.
     * @param g The brush (Graphics object)
     * @param screenX The horizontal pixel position
     * @param screenY The vertical pixel position
     * @param word The text to display
     */
    void draw(Graphics g, int screenX, int screenY, String word);
}