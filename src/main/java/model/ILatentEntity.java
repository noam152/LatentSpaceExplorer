package model;

/**
 * This interface ensures every word has a name and mathematical coordinates.
 */
public interface ILatentEntity {

    /**
     * Returns the name of the word.
     */
    String getIdentifier();

    /**
     * Returns the raw, high-dimensional data.
     */
    double[] getFullVector();

    /**
     * Returns the reduced 3D/2D coordinates after PCA transformation.
     */
    double[] getPCAVector();
}