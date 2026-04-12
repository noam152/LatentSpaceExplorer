package math;

public class EuclideanDistance implements IDistanceStrategy {

    /**
     * Calculates the straight-line distance between two N-dimensional vectors.
     */
    @Override
    public double calculateDistance(double[] vector1, double[] vector2) {

        if (vector1.length != vector2.length) { // Failsafe
            throw new IllegalArgumentException("Vectors must be of the same length to be compared!");
        }

        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) { // Walk through each dimension
            double diff = vector1[i] - vector2[i];
            sum += diff * diff; // Square the difference
        }
        return Math.sqrt(sum); // Square root of the total sum
    }
}