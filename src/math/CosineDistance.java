package math;

// ==============
// CosineDistance
// ==============
public class CosineDistance implements IDistanceStrategy {

    @Override
    public double calculateDistance(double[] vector1, double[] vector2) {

        // Failsafe: Vectors must live in the same dimension
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must be of the same length to calculate angle!");
        }

        // Accumulators for the Formula components
        double dotProduct = 0.0; // The top part (A * B)
        double normA = 0.0;      // Length of Vector A squared
        double normB = 0.0;      // Length of Vector B squared

        // 3. The Big Loop: Calculate everything in one pass for performance
        for (int i = 0; i < vector1.length; i++) {
            // Multiply matching dimensions and add to sum
            dotProduct += vector1[i] * vector2[i];

            // Sum of squares for each vector (used to calculate magnitude later)
            normA += vector1[i] * vector1[i];
            normB += vector2[i] * vector2[i];
        }

        // Mathematical Safety: Avoid "Division by Zero"
        // If a vector has 0 length, we can't calculate an angle.
        if (normA == 0 || normB == 0) return 1.0;

        // Apply the Cosine Formula: DotProduct / (Magnitude A * Magnitude B)
        // Result is between 1.0 (Identical direction) and -1.0 (Opposite)
        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

        // Convert Similarity to Distance
        // Most algorithms expect 0 to be "closest".
        // If similarity is 1.0 (identical), distance becomes 0.0.
        return 1.0 - similarity;
    }
}