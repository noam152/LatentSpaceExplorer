package gui;

import java.awt.*;

/**
 * VisualFactory: The Central Resource Manager
 */
public class VisualFactory {

    // Shared UI Constants (Flyweight Pattern)

    // Default blue for normal dots
    public static final Color DEFAULT_POINT_COLOR = Color.BLUE;

    // Highlight red for selected dots
    public static final Color HIGHLIGHT_COLOR = Color.RED;

    // 2. Shared Typography
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
}