package gui.redering;

import gui.VisualFactory; // The Design Center
import java.awt.*;        // Java Drawing Tools

/**
 * The "Inner Doll": The most basic way to draw a word point.
 * Part of the Decorator Pattern - this is the base entity being wrapped.
 */
public class BaseVisualEntity implements IVisualEntity {

    @Override
    public void draw(Graphics g, int screenX, int screenY, String word) {

        // Pick the brush color from our central Factory (Flyweight Pattern)
        g.setColor(VisualFactory.DEFAULT_POINT_COLOR); // Blue | כחול

        // 2. Draw the Dot (Centered)
        // We subtract half the size (3px) so the point's CENTER is exactly on our coordinates.
        g.fillOval(screenX - 3, screenY - 3, 6, 6);

        // 3. Draw the Text (With Offset)
        // We move the text slightly (5px right, 5px up) so it doesn't cover the point itself.
        g.drawString(word, screenX + 5, screenY - 5);
    }
}