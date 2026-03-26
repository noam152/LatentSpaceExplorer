package engine;

import model.ILatentEntity;
import math.EuclideanDistance;
import math.IDistanceStrategy;

import java.util.*;

/**
 * SpaceManager serves as the Global State Manager and Computational Engine.
 * It maintains the entity registry, handles the Observer pattern for UI sync,
 * and performs vector-space algorithms (Analogy, K-Nearest Neighbors, Centroids).
 */
public class SpaceManager {

    // --- Core State & Registry ---
    private Map<String, ILatentEntity> entities; // Central lookup table
    private IDistanceStrategy distanceMetric;    // Strategy pattern for distance calculations
    private List<IObserver> observers;           // List of observers for state changes
    private boolean is2DMode = true;             // State variable to determine if the view is flat (2D) or spatial (3D). Default is 2D.

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

    // --- Getters & Setters with Auto-Notification ---

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
     * Demonstrates the Strategy Pattern.
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

    // --- Vector Space Algorithms ---

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

    public int getKNeighbors() { return kNeighbors; }

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

    /**
     * Calculates the center of gravity (centroid) for a group of words.
     */
    public ILatentEntity calculateCentroid(List<String> words) {
        if (words == null || words.isEmpty()) return null;

        double[] centroid = null; // Will hold the sum of all vectors
        int validWordsCount = 0; // Counts how many words actually exist in our dictionary
        List<ILatentEntity> excludeList = new ArrayList<>(); //our "blacklist"

        for (String w : words) {
            ILatentEntity entity = getEntity(w.trim().toLowerCase());
            if (entity != null && entity.getFullVector() != null) { // If the word exists in our dictionary, add its values to the center point
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
     * Internal helper to find the single closest neighbor to a raw vector coordinate.
     */
    private ILatentEntity findClosest(double[] targetVector, List<ILatentEntity> excludeList) {
        ILatentEntity closest = null;
        double minDist = Double.MAX_VALUE;

        for (ILatentEntity entity : entities.values()) {
            // If we have a blacklist AND this word is on it -> skip to the next word immediately!
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

    public boolean is2DMode() { // Checks if the system is currently in 2D mode.
        return is2DMode;
    }

    public void set2DMode(boolean is2D) { // Sets the view mode (2D or 3D) and triggers a UI update.
        this.is2DMode = is2D;
        notifyObservers();
    }
}