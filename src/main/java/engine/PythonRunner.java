package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * take care of the execution of external Python scripts.
 * the bridge between the Java engine and the Python data.
 */
public class PythonRunner {

    /**
     * A custom alarm for when Python misbehaves.
     */
    public static class PythonExecutionException extends Exception {
        public PythonExecutionException(String message) {
            super(message); // Pass the bad news to the main Exception class
        }
    }

    /**
     * Starts the Python script and waits for it to finish successfully.
     */
    public void runEmbedderScript() throws PythonExecutionException {
        try {
            // Prepare the terminal command: "py -3.12 embedder.py"
            ProcessBuilder pb = new ProcessBuilder("py", "-3.12", "embedder.py");

            // Merge standard error (stderr) into standard output (stdout)
            // to capture Python crash tracebacks in a single readable stream.
            pb.redirectErrorStream(true);

            // Launch the external Python process
            Process p = pb.start();

            // Prepare a bucket to catch everything Python prints out
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            // Continuously read the stream until the Python script stops printing
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Suspend Java execution until Python finishes.
            int exitCode = p.waitFor();

            if (exitCode != 0) {
                // Throw our custom exception containing the full Python error log
                throw new PythonExecutionException("Python script failed with exit code " + exitCode + ".\nOutput:\n" + output.toString());
            }

        } catch (IOException | InterruptedException e) {
            // Something blocked Java from even starting the process
            throw new PythonExecutionException("Failed to execute python process: " + e.getMessage());
        }
    }
}