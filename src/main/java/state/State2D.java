package state;

import engine.SpaceManager;

/**
 * This is the baseline state of the system.
 */
public class State2D implements IProjectionState {

    @Override
    public double[] transform(double[] pcaVec, SpaceManager sm) {
        int xAxis = sm.getXAxisIndex();
        int yAxis = sm.getYAxisIndex();

        double x0 = 0.0, y0 = 0.0;

        // Safely extract values from the PCA vector
        if (pcaVec.length > xAxis) { x0 = pcaVec[xAxis]; }
        if (pcaVec.length > yAxis) { y0 = pcaVec[yAxis]; }

        // Return coordinates (Z is always 0.0)
        return new double[]{x0, y0, 0.0};
    }

    @Override
    public boolean is2D() {
        return true;
    }
}