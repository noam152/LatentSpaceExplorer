package model;

/**
 * WordEntity: The concrete implementation of a latent word.
 * This is the actual "container" that travels through the system.
 */
public class WordEntity implements ILatentEntity {

    // Internal Data Storage
    private String word; // The name
    private double[] fullVector;  // The 300-D math brain
    private double[] pcaVector;   // The 2D/3D visual coordinates

    /**
     * Constructor: Creates a permanent record for a word.
     */
    public WordEntity(String word, double[] fullVector, double[] pcaVector) {
        this.word = word;
        this.fullVector = fullVector;
        this.pcaVector = pcaVector;
    }

    // --- Implementation of the Interface (The Contract) ---

    @Override
    public String getIdentifier() {
        return this.word; // Returns the word name
    }

    @Override
    public double[] getFullVector() {
        // Used for distance calculations (Cosine/Euclidean)
        return this.fullVector;
    }

    @Override
    public double[] getPCAVector() {
        // Used by the GUI to know where to draw the dot
        return this.pcaVector;
    }
}