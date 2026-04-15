# LatentSpace Explorer 🌌

## Overview
**LatentSpace Explorer** is an interactive desktop application designed to visualize and explore the hidden mathematical relationships between words.

Natural Language Processing (NLP) models represent words as complex, high-dimensional vectors. This application takes those abstract arrays of numbers and translates them into an intuitive, visual sandbox. Users can navigate through the "latent space" of language, calculate semantic distances, and even perform mathematical equations using words instead of numbers.

## ✨ Key Features

* **Interactive 3D & 2D Visualization:** Explore the word universe in a classic 2D scatter plot, or switch to a fully interactive 3D environment with Yaw and Pitch camera controls to see how words cluster together in space.
* **PCA Dimensionality Control:** High-dimensional vectors are reduced using Principal Component Analysis (PCA). The app allows users to manually select which principal components (PC) map to the X, Y, and Z axes, revealing different semantic variances and hidden patterns.
* **K-Nearest Neighbors (KNN):** Select any word on the canvas to instantly calculate and list its closest semantic neighbors based on their vector proximity.
* **Word Arithmetic Lab:** Perform literal math with language to solve analogies. For example, inputting `King - Man + Woman` will calculate the resulting vector and find the closest word in the dictionary (e.g., `Queen`).
* **Cluster Centroids:** Input a custom list of words (e.g., *apple, banana, orange*) to calculate their exact mathematical center point, revealing the core concept that connects them.
* **Semantic Scale (1D Projection):** Project the entire dictionary onto a single conceptual axis defined by two opposing words (e.g., mapping all words on a scale from *Good* to *Bad* or *Hot* to *Cold*).
* **Dynamic Distance Metrics:** Switch instantly between **Euclidean Distance** (straight-line physical distance) and **Cosine Similarity** (directional angle) to see how different algorithms interpret the closeness between two concepts.

## ⚙️ Prerequisites
Before running the application, ensure you have the following installed:
* **Java 17** or higher
* **Python 3** (with `python` available in your system PATH)
* **Maven** (integrated into most IDEs)

## 🚀 How to Run

### Option 1: The Visual Way (IntelliJ Maven Tool) - Recommended
This is the easiest way to ensure all dependencies and working directories are handled correctly.

**Phase A: Build the Project (Required for first-time run)**
1. Open the **Maven** tab on the right side of IntelliJ IDEA.
2. Expand **LatentSpaceExplorer** > **Lifecycle**.
3. Double-click **`clean`** to clear any old builds.
4. Double-click **`compile`** and wait for the green "BUILD SUCCESS" message in the terminal.

**Phase B: Run the Engine**
1. Still in the Maven tab, scroll down and expand **Plugins** > **exec**.
2. Double-click **`exec:java`** to launch the application.

### Option 2: The Direct Run
1. Open the project in your IDE and allow Maven to sync dependencies.
2. Navigate to `src/main/java/engine/Main.java`.
3. Right-click the file and select **Run 'Main.main()'**.

