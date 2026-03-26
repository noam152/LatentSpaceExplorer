package history;

import java.util.Stack; // for "stack"

/**
 * CommandManager: The brain behind the Undo system.
 * This class tracks and manages all executable actions in the application.
 */
public class CommandManager {

    // The history stack: Stores all commands that have been executed
    private Stack<ICommand> history;

    /**
     * Constructor: Initializes a clean history stack.
     */
    public CommandManager() {
        this.history = new Stack<>();
    }

    /**
     * Executes a new command and pushes it to the top of the history stack.
     */
    public void executeCommand(ICommand cmd) {
        cmd.execute(); // Run the logic
        history.push(cmd); // Add to history
    }

    /**
     * Reverses the very last action taken by the user.
     */
    public void undoLastCommand() {
        // Safety check: Make sure there is actually something to undo!
        if (!history.isEmpty()) {

            // 'pop' removes and returns the item at the very top of the stack
            ICommand cmd = history.pop();

            cmd.undo(); // Reverse it

        } else {
            // Failsafe: Log that there are no more actions to reverse
            System.out.println("No actions to undo.");
        }
    }
}