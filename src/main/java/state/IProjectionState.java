package state;

import engine.SpaceManager;

/**
 * Defines the contract for different view modes (2D/3D).
 */

public interface IProjectionState {

    // Calculates the math based on the current state
    double[] transform(double[] pcaVec, SpaceManager sm);

    // Checks if the system is currently in the base 2D mode
    boolean is2D();
}