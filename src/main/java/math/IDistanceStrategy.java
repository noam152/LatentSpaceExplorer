package math;

/**
 * The "Contract" for all measurement tools.
 */
public interface IDistanceStrategy {

    /**
     * Calculates the distance between two N-dimensional vectors.
     */
    double calculateDistance(double[] vector1, double[] vector2);
}