package state;

import engine.SpaceManager;

public class State3D implements IProjectionState {

    @Override
    public double[] transform(double[] pcaVec, SpaceManager sm) {

        int xAxis = sm.getXAxisIndex();
        int yAxis = sm.getYAxisIndex();
        int zAxis = sm.getZAxisIndex();

        double x0 = 0.0, y0 = 0.0, z0 = 0.0;

        if (pcaVec.length > xAxis) { x0 = pcaVec[xAxis]; }
        if (pcaVec.length > yAxis) { y0 = pcaVec[yAxis]; }
        if (pcaVec.length > zAxis) { z0 = pcaVec[zAxis]; }

        // How the user tilting the camera right now
        double rotX = sm.getRotationX(); // Up/Down tilt (Pitch)
        double rotY = sm.getRotationY(); // Left/Right tilt (Yaw)

        // The Carousel (Yaw / Left-Right rotation)
        // it's like looking at the dot from above. We spin it around the center like a carousel.
        // This mixes the X (left/right) and Z (depth) positions, but Y (height) stays the same.
        double x1 = x0 * Math.cos(rotY) + z0 * Math.sin(rotY);
        double z1 = -x0 * Math.sin(rotY) + z0 * Math.cos(rotY);
        double y1 = y0;

        // The Ferris Wheel (Pitch / Up-Down rotation)
        // it's like looking at the dot from the side. We spin it up and down like a Ferris Wheel.
        // This mixes the Y (height) and our new Z (depth) positions, but X stays the same.
        double y2 = y1 * Math.cos(rotX) - z1 * Math.sin(rotX);
        double z2 = y1 * Math.sin(rotX) + z1 * Math.cos(rotX);
        double x2 = x1;

        return new double[]{x2, y2, z2};
    }

    @Override
    public boolean is2D() {
        return false; // We are in 3D mode.
    }
}