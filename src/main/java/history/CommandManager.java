package history;

import java.util.Stack;

/**
 * CommandManager: The brain behind the Undo system.
 * This class tracks and manages all executable actions in the application.
 */
public class CommandManager {

    private Stack<ICommand> history; // Stores all commands that have been executed

    public CommandManager() {
        this.history = new Stack<>();
    }

    /**
     * Executes a new command and pushes it to the top of the history stack.
     */
    public void executeCommand(ICommand cmd) {
        cmd.execute(); // Run the logic
        history.push(cmd); // Add to history stack
    }

    /**
     * Reverses the very last action taken by the user.
     */
    public void undoLastCommand() {

        if (!history.isEmpty()) { // Safety check

            ICommand cmd = history.pop(); // returns the item at the very top of the stack

            cmd.undo(); // Reverse it

        } else {
            System.out.println("No actions to undo.");
        }
    }
}