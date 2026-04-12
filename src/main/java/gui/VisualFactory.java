package gui;

import gui.rendering.BaseVisualEntity;
import gui.rendering.HighlightDecorator;
import gui.rendering.IVisualEntity;
import java.awt.Color;
import java.awt.Font;

public class VisualFactory {

    // UI Constants
    public static final Color DEFAULT_POINT_COLOR = Color.BLUE;
    public static final Color HIGHLIGHT_COLOR = Color.RED;
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);

    // Only ONE instance of each brush exists in memory.
    private static final IVisualEntity SHARED_BASE_ENTITY = new BaseVisualEntity();
    private static final IVisualEntity SHARED_HIGHLIGHTED_ENTITY = new HighlightDecorator(SHARED_BASE_ENTITY);

    // Use this for normal words
    public static IVisualEntity getBaseEntity() {
        return SHARED_BASE_ENTITY;
    }

    // Use this when a word is hovered
    public static IVisualEntity getHighlightedEntity() {
        return SHARED_HIGHLIGHTED_ENTITY;
    }
}