package model;

/**
 * WordEntity: The concrete implementation of a latent word.
 * This is the actual "container" that travels through the system.
 */
public class WordEntity implements ILatentEntity {

    private String word; // The name
    private double[] fullVector;  // The 300-D math brain
    private double[] pcaVector;   // The 2D/3D visual coordinates

    public WordEntity(String word, double[] fullVector, double[] pcaVector) {
        this.word = word;
        this.fullVector = fullVector;
        this.pcaVector = pcaVector;
    }

    @Override
    public String getIdentifier() {
        return this.word; // Returns the word name
    }

    @Override
    public double[] getFullVector() {
        return this.fullVector; // Used for distance calculations (Cosine/Euclidean)
    }

    @Override
    public double[] getPCAVector() {
        return this.pcaVector; // Used by the GUI to know where to draw the dot
    }
}