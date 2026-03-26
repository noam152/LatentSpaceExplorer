package engine;

/**
 * Defines the contract for the Observer design pattern.
 * Any component (like the GUI) that needs to react to changes in the engine's state
 * must implement this interface to listen for update events.
 */

public interface IObserver {

    /**
     * Triggered by the engine (the subject) to notify this observer
     * that a state change has occurred and a refresh is required.
     */
    void update();
}