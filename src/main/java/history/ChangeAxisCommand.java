package history;

import engine.SpaceManager;

/**
 * A command to swap which dimensions (axes) are currently visible.
 */
public class ChangeAxisCommand implements ICommand {

    private SpaceManager spaceManager;
    private int oldXAxis, oldYAxis, oldZAxis; // Backup variables for Undo functionality
    private int newXAxis, newYAxis, newZAxis; // Target variables for Execute functionality

    public ChangeAxisCommand(SpaceManager spaceManager, int newXAxis, int newYAxis, int newZAxis) {
        this.spaceManager = spaceManager;
        this.newXAxis = newXAxis;
        this.newYAxis = newYAxis;
        this.newZAxis = newZAxis;

        // Snapshot: Ask the engine what is currently being displayed before we change it
        this.oldXAxis = spaceManager.getXAxisIndex();
        this.oldYAxis = spaceManager.getYAxisIndex();
        this.oldZAxis = spaceManager.getZAxisIndex();
    }

    /**
     * Execute: Tells the engine to point its "camera" to the new dimensions.
     */
    @Override
    public void execute() {
        // Change the view in the engine
        spaceManager.setDisplayAxes(newXAxis, newYAxis, newZAxis);
    }

    /**
     * Undo: Tells the engine to return to the previous view.
     */
    @Override
    public void undo() {
        // Revert to the old view
        spaceManager.setDisplayAxes(oldXAxis, oldYAxis, oldZAxis);
    }
}