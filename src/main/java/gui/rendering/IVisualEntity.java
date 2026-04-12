package gui.rendering;

import java.awt.*;

/**
 * The Drawing Contract
 */
public interface IVisualEntity {
    void draw(Graphics g, int screenX, int screenY, String word); // The core drawing command.
}