package engine;

import model.ILatentEntity;
import history.CommandManager;

import java.io.File;// Java library to interact with physical files on the hard drive.
import java.io.IOException; //Handles errors that might occur during file reading/writing.
import java.util.List;

/**
 * LatentSpaceEngine is the core backbone of the application.
 * It coordinates the initialization pipeline (Python execution, data loading)
 * and serves as the central access point for the UI to interact with the system's logic.
 */
public class LatentSpaceEngine {

    private SpaceManager spaceManager;
    private CommandManager commandManager;
    private PythonRunner pythonRunner;
    private GsonDataLoader dataLoader;

    public LatentSpaceEngine() {
        this.spaceManager = new SpaceManager();
        this.commandManager = new CommandManager();
        this.pythonRunner = new PythonRunner();
        this.dataLoader = new GsonDataLoader();
    }

    /**
     * Wakes up the application.
     * Checks for cached data to optimize startup time, triggers Python generation if missing,
     * and loads the entities into the main SpaceManager memory.
     */
    public void initialize() throws PythonRunner.PythonExecutionException, IOException {

        // File paths for the expected data files
        File fullVectorsFile = new File("full_vectors.json");
        File pcaVectorsFile = new File("pca_vectors.json");

        // Optimization - Check if data is already cached on disk
        if (fullVectorsFile.exists() && pcaVectorsFile.exists()) {
            System.out.println("Step 1: JSON files already exist. Skipping Python execution! (Speed Boost)");
        } else {
            // Data is missing, trigger the external Python script to generate it
            System.out.println("Step 1: Running Python script to generate data (this may take a minute)...");
            pythonRunner.runEmbedderScript();
        }

        // Parse the generated JSON files into Java objects
        System.out.println("Step 2: Loading data from JSON files...");
        List<ILatentEntity> data = dataLoader.loadEntities("full_vectors.json", "pca_vectors.json");

        // Step 3: Inject the parsed data into the central state manager
        System.out.println("Step 3: Setting up the space...");
        spaceManager.setEntities(data);

        System.out.println("Engine initialized successfully and ready for research!");
    }

    // --- Controlled access points for the GUI ---

    public SpaceManager getSpaceManager() {
        return spaceManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}