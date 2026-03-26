package history;

/**
 * ICommand: The blueprint for reversible actions.
 * Part of the Command Design Pattern.
 */
public interface ICommand {

    /**
     * Executes the specific action.
     */
    void execute();

    /**
     * Reverses the action performed by execute().
     */
    void undo();
}