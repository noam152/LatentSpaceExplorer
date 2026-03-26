package math;

// =================
// EuclideanDistance
// =================
public class EuclideanDistance implements IDistanceStrategy {

    /**
     * Calculates the straight-line distance between two N-dimensional vectors.
     */
    @Override
    public double calculateDistance(double[] vector1, double[] vector2) {

        // Failsafe: Ensure both words are from the same "universe" (dimension count)
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must be of the same length to be compared!");
        }

        double sum = 0.0; // The accumulator for our squared differences

        // The Loop: Walk through each dimension one by one
        for (int i = 0; i < vector1.length; i++) {

            // Calculate the gap between the two numbers in this specific dimension
            double diff = vector1[i] - vector2[i];

            // Square the difference: This removes negative signs and emphasizes larger gaps
            sum += diff * diff;
        }

        // The Final Touch: Square root of the total sum
        return Math.sqrt(sum);
    }
}