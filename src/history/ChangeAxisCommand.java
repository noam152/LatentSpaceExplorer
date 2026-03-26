package history;

import engine.SpaceManager;

/**
 * ChangeAxisCommand: A command to swap which dimensions (axes) are currently visible.
 * This allows the user to explore different angles of the word embeddings.
 */
public class ChangeAxisCommand implements ICommand {

    private SpaceManager spaceManager;

    // Backup variables for Undo functionality
    private int oldXAxis, oldYAxis, oldZAxis;

    // Target variables for Execute functionality
    private int newXAxis, newYAxis, newZAxis;

    /**
     * Constructor: Captured at the moment the user clicks "Change Axes".
     */
    public ChangeAxisCommand(SpaceManager spaceManager, int newXAxis, int newYAxis, int newZAxis) {
        this.spaceManager = spaceManager;
        this.newXAxis = newXAxis;
        this.newYAxis = newYAxis;
        this.newZAxis = newYAxis;

        // Snapshot: Ask the engine what is currently being displayed BEFORE we change it
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