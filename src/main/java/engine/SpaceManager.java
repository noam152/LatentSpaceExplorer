package engine;

import model.ILatentEntity;
import math.EuclideanDistance;
import math.IDistanceStrategy;
import state.IProjectionState;
import state.State2D;

import java.util.*;

/**
 * The Brain of the application.
 * It stores all the words, does the heavy math (like finding closest neighbors or analogies),
 * and alerts the screen whenever the data changes so it can redraw.
 */
public class SpaceManager {

    // --- Core State & Registry ---
    private Map<String, ILatentEntity> entities;
    private IDistanceStrategy distanceMetric;
    private List<IObserver> observers;
    private IProjectionState projectionState; // Default is 2D.

    // --- Viewport & Projection Settings ---
    private int xAxisIndex = 0;
    private int yAxisIndex = 1;
    private int zAxisIndex = 2;

    private double rotationX = 0.0;
    private double rotationY = 0.0;

    private int kNeighbors = 5;

    public SpaceManager() {
        this.entities = new HashMap<>();
        this.observers = new ArrayList<>();
        this.distanceMetric = new EuclideanDistance(); // Default strategy
        this.projectionState = new State2D(); // Default strategy
    }

    // --- Observer Pattern Implementation ---
    public void addObserver(IObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Broadcasts an update signal to all registered observers.
     * Should be called whenever the internal state changes.
     */
    public void notifyObservers() {
        for (IObserver observer : observers) {
            observer.update();
        }
    }

    public void setRotationX(double rotationX) {
        this.rotationX = rotationX;
        notifyObservers();
    }

    public void setRotationY(double rotationY) {
        this.rotationY = rotationY;
        notifyObservers();
    }

    public double getRotationX() { return rotationX; }

    public double getRotationY() { return rotationY; }

    public int getXAxisIndex() {return xAxisIndex;}

    public int getYAxisIndex() {return yAxisIndex;}

    public int getZAxisIndex() {return zAxisIndex;}

    public void setDisplayAxes(int xAxis, int yAxis, int zAxis) {
        this.xAxisIndex = xAxis;
        this.yAxisIndex = yAxis;
        this.zAxisIndex = zAxis;
        notifyObservers();
    }

    public void setKNeighbors(int kNeighbors) {
        this.kNeighbors = kNeighbors;
        notifyObservers();
    }

    /**
     * Updates the distance calculation strategy at runtime.
     */

    public IDistanceStrategy getCurrentStrategy() { return this.distanceMetric; }

    public void setStrategy(IDistanceStrategy newStrategy) {
        this.distanceMetric = newStrategy;
        notifyObservers();
    }

    // --- Entity Management ---

    public void addEntity(ILatentEntity entity) {
        entities.put(entity.getIdentifier().toLowerCase(), entity);
    }

    public void setEntities(List<ILatentEntity> newEntities) {
        for (ILatentEntity entity : newEntities) {
            addEntity(entity);
        }
    }

    public ILatentEntity getEntity(String identifier) {
        return entities.get(identifier.toLowerCase());
    }

    public Collection<ILatentEntity> getAllEntities() {
        return entities.values();
    }

    /**
     * Solves word analogies using vector arithmetic: target = v1 - v2 + v3.
     * Returns the closest existing entity to the calculated point.
     */
    public ILatentEntity calculateAnalogy(String w1, String w2, String w3) {
        ILatentEntity e1 = getEntity(w1);
        ILatentEntity e2 = getEntity(w2);
        ILatentEntity e3 = getEntity(w3);

        if (e1 == null || e2 == null || e3 == null) {
            throw new IllegalArgumentException("One or more words not found in dictionary.");
        }

        double[] v1 = e1.getFullVector();
        double[] v2 = e2.getFullVector();
        double[] v3 = e3.getFullVector();
        double[] targetVector = new double[v1.length];

        for (int i = 0; i < targetVector.length; i++) {
            targetVector[i] = v1[i] - v2[i] + v3[i];
        }

        return findClosest(targetVector, Arrays.asList(e1, e2, e3));
    }

    /**
     * Calculates the K-Nearest Neighbors for a given word.
     * Uses a PriorityQueue to efficiently sort entities by distance.
     */

    public List<ILatentEntity> getKNearestNeighbors(String word, int k) {
        ILatentEntity target = getEntity(word);
        if (target == null) return new ArrayList<>();

        // PriorityQueue acts as a min-heap based on distance from target
        PriorityQueue<ILatentEntity> pq = new PriorityQueue<>((a, b) -> {
            double distA = distanceMetric.calculateDistance(target.getFullVector(), a.getFullVector());
            double distB = distanceMetric.calculateDistance(target.getFullVector(), b.getFullVector());
            return Double.compare(distA, distB);
        });

        for (ILatentEntity entity : entities.values()) {
            if (!entity.getIdentifier().equalsIgnoreCase(word)) {
                pq.add(entity);
            }
        }

        List<ILatentEntity> result = new ArrayList<>();
        for (int i = 0; i < k && !pq.isEmpty(); i++) {
            result.add(pq.poll());
        }
        return result;
    }

    public int getKNeighbors() { return kNeighbors; }

    /**
     * Calculates the center for a group of words.
     */

    public ILatentEntity calculateCentroid(List<String> words) {
        if (words == null || words.isEmpty()) return null;

        double[] centroid = null;
        int validWordsCount = 0; // counts how many words exist in our dictionary
        List<ILatentEntity> excludeList = new ArrayList<>(); //our "blacklist"

        for (String w : words) {
            ILatentEntity entity = getEntity(w.trim().toLowerCase());
            if (entity != null && entity.getFullVector() != null) { // check if the word exists in our dictionary
                double[] vec = entity.getFullVector();
                // Initialize the array on the first valid word we find
                if (centroid == null) {
                    centroid = new double[vec.length];
                }
                // Sum up the values dimension by dimension
                for (int i = 0; i < vec.length; i++) {
                    centroid[i] += vec[i];
                }
                validWordsCount++;
                excludeList.add(entity);
            }
        }

        if (validWordsCount == 0) {
            throw new IllegalArgumentException("None of the words were found in the dictionary.");
        }

        // Divide the total sum by the number of words to get the average (the center)
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= validWordsCount;
        }

        return findClosest(centroid, excludeList);
    }

    /**
     * Semantic Scale Math
     */

    public static class ProjectionResult {
        public final String word;
        public final double score;

        public ProjectionResult(String word, double score) {
            this.word = word;
            this.score = score;
        }

        public double getScore() { return score; }
    }

    public Map<String, List<ProjectionResult>> getSemanticPoles(String w1, String w2, int k) {
        ILatentEntity entity1 = getEntity(w1);
        ILatentEntity entity2 = getEntity(w2);

        // return empty lists if one of the words doesn't exist
        if (entity1 == null || entity2 == null) {
            return Map.of("left", new ArrayList<>(), "right", new ArrayList<>());
        }

        double[] v1 = entity1.getFullVector();
        double[] v2 = entity2.getFullVector();
        double[] axis = new double[v1.length];

        double scoreW1 = 0;
        double scoreW2 = 0;

        // Define the direction: Draw a line from w1 to w2 (axis = w2 - w1)
        for (int i = 0; i < v1.length; i++) {
            axis[i] = v2[i] - v1[i];

            // Calculating the dot product for the anchor words
            scoreW1 += v1[i] * axis[i];
            scoreW2 += v2[i] * axis[i];
        }

        List<ProjectionResult> allProjections = new ArrayList<>();

        // Project every word in our universe onto this new axis using the Dot Product
        for (ILatentEntity entity : entities.values()) {

            if (entity.getIdentifier().equalsIgnoreCase(w1) ||
                    entity.getIdentifier().equalsIgnoreCase(w2)) {
                continue;
            }

            double[] currentVector = entity.getFullVector();
            double dotProduct = 0;

            for (int i = 0; i < currentVector.length; i++) {
                dotProduct += currentVector[i] * axis[i];
            }

            allProjections.add(new ProjectionResult(entity.getIdentifier(), dotProduct));
        }

        // Sort the results mathematically (lowest score = closest to w1, highest = closest to w2)
        allProjections.sort(Comparator.comparingDouble(ProjectionResult::getScore));

        Map<String, List<ProjectionResult>> poles = new HashMap<>();

        // This mathematically guarantees we never get an IndexOutOfBoundsException.
        int safeK = Math.min(k, allProjections.size() / 2);

        // Extract the extremes safely using our calculated safeK
        poles.put("left", new ArrayList<>(allProjections.subList(0, safeK)));
        poles.put("right", new ArrayList<>(allProjections.subList(allProjections.size() - safeK, allProjections.size())));

        List<ProjectionResult> leftAnchorList = new ArrayList<>();
        leftAnchorList.add(new ProjectionResult(w1, scoreW1));
        poles.put("anchor_left", leftAnchorList);

        List<ProjectionResult> rightAnchorList = new ArrayList<>();
        rightAnchorList.add(new ProjectionResult(w2, scoreW2));
        poles.put("anchor_right", rightAnchorList);

        return poles;
    }

    /**
     * Internal helper to find the single closest neighbor to a raw vector coordinate.
     */

    private ILatentEntity findClosest(double[] targetVector, List<ILatentEntity> excludeList) {
        ILatentEntity closest = null;
        double minDist = Double.MAX_VALUE;

        for (ILatentEntity entity : entities.values()) {
            // If we have a blacklist and this word is on it, skip to the next word immediately.
            if (excludeList != null && excludeList.contains(entity)) continue;

            // Measure the distance between our target point and the current word
            double dist = distanceMetric.calculateDistance(targetVector, entity.getFullVector());
            if (dist < minDist) {
                minDist = dist;
                closest = entity;
            }
        }
        return closest;
    }

    public IProjectionState getProjectionState() {
        return projectionState;
    }

    public void setProjectionState(IProjectionState newState) {
        this.projectionState = newState;
        notifyObservers(); // Tell the UI to redraw
    }
}