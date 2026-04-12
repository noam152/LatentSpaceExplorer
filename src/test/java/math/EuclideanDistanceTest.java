package math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EuclideanDistanceTest {

    @Test
    public void testCalculateDistance() {

        EuclideanDistance calculator = new EuclideanDistance();
        double[] vectorA = {0.0, 0.0};
        double[] vectorB = {3.0, 4.0};

        double result = calculator.calculateDistance(vectorA, vectorB);

        assertEquals(5.0, result, 0.0001, "The Euclidean distance should be exactly 5.0");
    }
}