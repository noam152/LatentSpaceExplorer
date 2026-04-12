package engine;

import model.ILatentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SpaceManagerTest {

    private SpaceManager manager;

    private static class MockEntity implements ILatentEntity {
        private final String word;
        private final double[] vector;

        public MockEntity(String word, double[] vector) {
            this.word = word;
            this.vector = vector;
        }

        @Override
        public String getIdentifier() { return word; }

        @Override
        public double[] getFullVector() { return vector; }

        @Override
        public double[] getPCAVector() { return null; }
    }

    @BeforeEach
    public void setUp() {
        manager = new SpaceManager();

        // --- Core Vocabulary for all tests ---
        manager.addEntity(new MockEntity("poor", new double[]{-10.0, 0.0}));
        manager.addEntity(new MockEntity("rich", new double[]{10.0, 0.0}));
        manager.addEntity(new MockEntity("beggar", new double[]{-8.0, 0.0}));
        manager.addEntity(new MockEntity("debt", new double[]{-5.0, 0.0}));
        manager.addEntity(new MockEntity("luxury", new double[]{5.0, 0.0}));
        manager.addEntity(new MockEntity("millionaire", new double[]{8.0, 0.0}));
    }

    @Test
    public void testGetSemanticPoles_CorrectFilteringAndSorting() {
        int k = 2;
        Map<String, List<SpaceManager.ProjectionResult>> poles = manager.getSemanticPoles("poor", "rich", k);

        List<SpaceManager.ProjectionResult> leftPole = poles.get("left");
        List<SpaceManager.ProjectionResult> rightPole = poles.get("right");

        assertEquals(2, leftPole.size());
        assertEquals(2, rightPole.size());

        // Check that base words are excluded
        assertFalse(leftPole.stream().anyMatch(p -> p.word.equals("poor")));
        assertFalse(rightPole.stream().anyMatch(p -> p.word.equals("rich")));

        // Verify sorting: 'beggar' (-8) < 'debt' (-5)
        assertTrue(leftPole.get(0).getScore() < leftPole.get(1).getScore());
        assertEquals("beggar", leftPole.get(0).word);

        // Verify right side: 'millionaire' (8) should be the most extreme
        assertEquals("millionaire", rightPole.get(1).word);
    }

    @Test
    public void testCalculateAnalogy_KingManWoman_ReturnsQueen() {
        // We add these words inside the test to avoid interference with other tests
        manager.addEntity(new MockEntity("king", new double[]{10.0, 10.0}));
        manager.addEntity(new MockEntity("man", new double[]{10.0, 2.0}));
        manager.addEntity(new MockEntity("woman", new double[]{2.0, 2.0}));
        manager.addEntity(new MockEntity("queen", new double[]{2.0, 10.0}));

        ILatentEntity result = manager.calculateAnalogy("king", "man", "woman");

        assertNotNull(result);
        assertEquals("queen", result.getIdentifier());
    }

    @Test
    public void testGetKNearestNeighbors() {
        // Adding specific words for this localized test
        manager.addEntity(new MockEntity("king", new double[]{10.0, 10.0}));
        manager.addEntity(new MockEntity("prince", new double[]{9.0, 9.0}));

        List<ILatentEntity> neighbors = manager.getKNearestNeighbors("king", 1);

        assertEquals(1, neighbors.size());
        assertEquals("prince", neighbors.get(0).getIdentifier());
    }

    @Test
    public void testCalculateAnalogy_WithMissingWord_ThrowsException() {
        // We expect the code to throw an IllegalArgumentException because "ghost" is missing
        assertThrows(IllegalArgumentException.class, () -> {
            manager.calculateAnalogy("rich", "poor", "ghost");
        }, "Should throw IllegalArgumentException if one of the words is missing");
    }

    @Test
    public void testGetSemanticPoles_WithTinyDictionary() {
        // Our dictionary currently has about 6 words.
        // Let's ask for k=10 (Total 20 needed for both poles).
        int k = 10;
        Map<String, List<SpaceManager.ProjectionResult>> poles = manager.getSemanticPoles("poor", "rich", k);

        // The safety net should kick in and limit the size to dictionary size / 2 (which is 2 words per side)
        assertTrue(poles.get("left").size() <= 3, "Safety net should limit the number of results to half the dictionary size");
        assertDoesNotThrow(() -> manager.getSemanticPoles("poor", "rich", k), "Should not crash even if K is larger than dictionary");
    }

    @Test
    public void testGetKNearestNeighbors_WithEmptyDictionary() {
        SpaceManager emptyManager = new SpaceManager();
        // Testing KNN on an empty manager should return an empty list, not a crash
        List<ILatentEntity> results = emptyManager.getKNearestNeighbors("anything", 5);

        assertTrue(results.isEmpty(), "Should return an empty list when dictionary is empty");
    }
}