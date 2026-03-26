package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Orchestrates the execution of external Python scripts.
 * Acts as a bridge between the Java engine and the Python data generation pipeline.
 */
public class PythonRunner {

    /**
     * A custom domain exception used to encapsulate and identify Python-specific runtime errors.
     */
    public static class PythonExecutionException extends Exception {
        public PythonExecutionException(String message) {
            super(message);
        }
    }

    /**
     * Starts the Python script and waits for it to finish successfully.
     */
    public void runEmbedderScript() throws PythonExecutionException {
        try {
            // Define the OS command to execute (equivalent to typing in the terminal)
            ProcessBuilder pb = new ProcessBuilder("py", "-3.12", "embedder.py");

            // Merge standard error (stderr) into standard output (stdout)
            // to capture Python crash tracebacks in a single readable stream.
            pb.redirectErrorStream(true);

            // Launch the external Python process
            Process p = pb.start();

            // Capture the live console output from the Python script into memory for debugging purposes
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())); // Create a buffered reader to efficiently capture the text output from the Python process.
            StringBuilder output = new StringBuilder(); // Use a StringBuilder for memory-efficient accumulation of the multi-line console output
            String line; // Temporary "bucket" to hold one line of text at a time during the loop.

            // Continuously read the stream until the Python script stops printing
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Suspend Java execution until Python finishes.
            // By OS convention, an exit code of 0 indicates success. Anything else is a failure.
            int exitCode = p.waitFor();

            if (exitCode != 0) {
                // Throw our custom exception containing the full Python error log
                throw new PythonExecutionException("Python script failed with exit code " + exitCode + ".\nOutput:\n" + output.toString());
            }

        } catch (IOException | InterruptedException e) {
            // Catch catastrophic OS-level failures (e.g., Python is not installed, or the thread was killed)
            throw new PythonExecutionException("Failed to execute python process: " + e.getMessage());
        }
    }
}