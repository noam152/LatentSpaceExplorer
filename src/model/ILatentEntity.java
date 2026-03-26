package model;

/**
 * ILatentEntity: The Identity Card of a word in space.
 * This interface ensures every word has a name and mathematical coordinates.
 */
public interface ILatentEntity {

    /**
     * Returns the human-readable name of the word.
     */
    String getIdentifier();

    /**
     * Returns the raw, high-dimensional data.
     * Used for precise math and distance calculations.
     */
    double[] getFullVector();

    /**
     * Returns the reduced 3D/2D coordinates after PCA transformation.
     * Used specifically for drawing the word on the screen.
     */
    double[] getPCAVector();
}