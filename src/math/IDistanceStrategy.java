package math;

/**
 * IDistanceStrategy: The "Contract" for all measurement tools.
 * Part of the Strategy Design Pattern.
 */
public interface IDistanceStrategy {

    /**
     * Calculates the distance between two N-dimensional vectors.
     */
    double calculateDistance(double[] vector1, double[] vector2);
}