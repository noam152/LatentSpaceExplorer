package history;

import engine.SpaceManager;
import math.IDistanceStrategy;

/**
 * This class captures the old strategy so it can revert back if needed.
 */
public class ChangeMetricCommand implements ICommand {

    private SpaceManager spaceManager;
    private IDistanceStrategy oldStrategy; // Backup for Undo
    private IDistanceStrategy newStrategy; // The target strategy

    public ChangeMetricCommand(SpaceManager spaceManager, IDistanceStrategy newStrategy) {
        this.spaceManager = spaceManager;
        this.newStrategy = newStrategy;
        this.oldStrategy = spaceManager.getCurrentStrategy(); // We save the current state before we change it.
    }

    /**
     * Executes the change: Swaps the engine's math logic.
     */
    @Override
    public void execute() {
        spaceManager.setStrategy(newStrategy); // Apply new logic
        System.out.println("Changed distance metric to new strategy.");
    }

    /**
     * Reverts the change: Re-installs the previous math logic.
     */
    @Override
    public void undo() {
        spaceManager.setStrategy(oldStrategy); // Restore old logic
        System.out.println("Undid metric change. Reverted to previous strategy.");
    }
}