package history;

/**
 * The Interface for reversible actions.
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