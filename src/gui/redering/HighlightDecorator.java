package gui.redering;

import java.awt.*;

/**
 * HighlightDecorator: Adding "Special Effects" to points
 */
public class HighlightDecorator implements IVisualEntity {
    private IVisualEntity decoratedEntity; // The inner "doll"

    public HighlightDecorator(IVisualEntity decoratedEntity) {
        this.decoratedEntity = decoratedEntity;
    }

    @Override
    public void draw(Graphics g, int x, int y, String identifier) {
        // First, let the original point draw itself
        decoratedEntity.draw(g, x, y, identifier);

        // Draw red target rings around the point
        g.setColor(Color.RED);
        g.drawOval(x - 6, y - 6, 12, 12); // Inner ring
        g.drawOval(x - 7, y - 7, 14, 14); // Outer ring

        // CRITICAL: Save original font to restore it later
        Font originalFont = g.getFont();

        // Draw the Callout Line (Laser line from point to corner)
        int cornerX = 30;
        int cornerY = 40;
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(255, 0, 0, 150)); // Red with 150 transparency
        g2d.setStroke(new BasicStroke(2)); // Thicker line
        g2d.drawLine(x, y, cornerX, cornerY + 15);
        g2d.setStroke(new BasicStroke(1)); // Reset stroke

        // Calculate dynamic label size based on the word length
        String tagText = "Selected: " + identifier;
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(tagText);
        int textHeight = fm.getHeight();

        // Draw the Label Background (Yellow bubble)
        g.setColor(new Color(255, 255, 220, 245));
        g.fillRoundRect(cornerX, cornerY, textWidth + 20, textHeight + 10, 10, 10);

        g.setColor(Color.RED); // Label Border
        g.drawRoundRect(cornerX, cornerY, textWidth + 20, textHeight + 10, 10, 10);

        // Final text inside the bubble
        g.setColor(Color.BLACK);
        g.drawString(tagText, cornerX + 10, cornerY + textHeight + 2);

        // Restore original font for other entities
        g.setFont(originalFont);
    }
}