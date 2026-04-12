package math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CosineDistanceTest {

    @Test
    public void testIdenticalDirectionVectors() {
        CosineDistance calculator = new CosineDistance();
        // Vector B is just Vector A multiplied by 2. They point in the exact same direction.
        double[] vectorA = {2.0, 3.0};
        double[] vectorB = {4.0, 6.0};

        double result = calculator.calculateDistance(vectorA, vectorB);

        // Same direction means maximum similarity (1.0).
        // Distance is (1 - similarity), so it should be exactly 0.0.
        assertEquals(0.0, result, 0.0001, "Vectors pointing in the same direction should have 0 distance");
    }

    @Test
    public void testOrthogonalVectors() {
        CosineDistance calculator = new CosineDistance();
        // These vectors are at a perfect 90-degree angle to each other.
        double[] vectorA = {1.0, 0.0};
        double[] vectorB = {0.0, 1.0};

        double result = calculator.calculateDistance(vectorA, vectorB);

        // 90 degrees means 0 similarity. Distance should be 1.0.
        assertEquals(1.0, result, 0.0001, "Orthogonal vectors should have a maximum distance of 1.0");
    }
}