package gui;

import model.ILatentEntity;
import history.ChangeAxisCommand;
import history.ChangeMetricCommand;
import history.ICommand;
import engine.LatentSpaceEngine;
import math.CosineDistance;
import math.EuclideanDistance;

import javax.swing.*; // Swing: Java's primary Graphical User Interface toolkit (Windows, Buttons, Spinners)
import java.awt.*; // AWT (Abstract Window Toolkit): Used for layouts, dimensions, colors, and styling
// Event Listeners: Allow the program to react to user hardware inputs (Keyboard presses)
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException; // Exception handling for when text cannot be converted to numbers (used in JSpinner)
import java.util.Arrays;
import java.util.List;

/**
 * MainWindow serves as the primary container for the application's GUI.
 * It assembles the 3D visualization canvas and the interactive control dashboard.
 */
public class MainWindow extends JFrame { //Java's standard window class

    private LatentSpaceEngine SpaceEngine;
    private LatentSpaceView view;

    public MainWindow(LatentSpaceEngine SpaceEngine) {
        this.SpaceEngine = SpaceEngine;

        setTitle("LatentSpace Explorer - 3D Version");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Kills process on exit

        // Defines the architectural layout of the window using the Compass-style manager.
        // It divides the frame into 5 regions (North, South, East, West, Center).
        // The Center dynamically expands to fill all remaining free space.
        setLayout(new BorderLayout());

        // Initialize and position the 3D Viewport in the center
        view = new LatentSpaceView(SpaceEngine.getSpaceManager());
        add(view, BorderLayout.CENTER);

        // Build the Control Dashboard and wrap it in a scrollable pane (JScrollPane).
        // This is crucial because the dashboard is tall and might overflow on smaller screens.
        JPanel controlPanel = createControlPanel();
        JScrollPane scrollPane = new JScrollPane(controlPanel);
        // Disable horizontal scrolling for better User Experience.
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null); // Remove default borders for a cleaner look
        scrollPane.setPreferredSize(new Dimension(360, getHeight())); // Lock the preferred width of the sidebar to 360 pixels so it doesn't crush the 3D view.

        add(scrollPane, BorderLayout.EAST); // Dock the dashboard to the right side of the screen
    }

    /**
     * Constructs the modular control dashboard containing all user inputs.
     * Uses a vertical BoxLayout to stack control modules on top of each other.
     */
    private JPanel createControlPanel() {

        /**
         * --- System Controls (Metric & Undo) ---
         */

        JPanel panel = new JPanel(); // Create the main master panel that will hold all the control modules.
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Set the layout manager to stack everything vertically (Top to Bottom).
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Add an invisible padding of 15 pixels around the edges (Top, Left, Bottom, Right)

        // Create a sub-panel with a Grid layout: 3 rows, 1 column, and 5px gaps.
        // This ensures all buttons are perfectly uniform in size.
        JPanel sysPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        sysPanel.setMaximumSize(new Dimension(320, 100)); // Constrain the size so it doesn't stretch vertically

        JButton btnEuclidean = new JButton("Change to Euclidean");
        btnEuclidean.addActionListener(e -> {
            // Encapsulate the action into a Command object instead of executing directly.
            // This is the core of the Command Pattern, enabling the Undo functionality.
            ICommand cmd = new ChangeMetricCommand(SpaceEngine.getSpaceManager(), new EuclideanDistance());
            // Pass the command to the Invoker (CommandManager) to execute and store in history.
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });

        JButton btnCosine = new JButton("Change to Cosine");
        btnCosine.addActionListener(e -> {
            // Encapsulate the action into a Command object instead of executing directly.
            // This is the core of the Command Pattern, enabling the Undo functionality.
            ICommand cmd = new ChangeMetricCommand(SpaceEngine.getSpaceManager(), new CosineDistance());
            // 4. Pass the command to the Invoker (CommandManager) to execute and store in history
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });

        JButton btnUndo = new JButton("Undo Last Action");
        btnUndo.addActionListener(e -> {
            // Tell the Invoker to pop the last command from the stack and reverse it.
            SpaceEngine.getCommandManager().undoLastCommand();
        });

        // Assemble the module: add the buttons to the grid panel.
        sysPanel.add(btnEuclidean);
        sysPanel.add(btnCosine);
        sysPanel.add(btnUndo);

        /**
         * --- PCA Axes Selection ---
         */

        //Create a panel with a 3x2 grid layout (3 rows, 2 columns) with 5px gaps.
        JPanel axisPanel = new JPanel(new GridLayout(3, 2, 5, 5)); //GridLayout - Equal cells
        axisPanel.setBorder(BorderFactory.createTitledBorder("PCA Axes Selection")); // Add a titled border around the module
        axisPanel.setMaximumSize(new Dimension(320, 110));// Constrain the size so it doesn't stretch vertically

        axisPanel.add(new JLabel(" X Axis (PC):")); // Row 1: X-Axis Label and Spinner
        // SpinnerModel: Initial Value=0, Min=0, Max=10, Step=1
        JSpinner spinX = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        axisPanel.add(spinX);

        axisPanel.add(new JLabel(" Y Axis (PC):")); // Row 2: Y-Axis Label and Spinner
        // SpinnerModel: Initial Value=0, Min=0, Max=10, Step=1
        JSpinner spinY = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        axisPanel.add(spinY);


        axisPanel.add(new JLabel("")); // Spacer to push the button to the 2nd column

        JButton btnApplyAxes = new JButton("Apply Axes");
        // Extract the chosen numbers from the spinners
        btnApplyAxes.addActionListener(e -> {
            int x = (Integer) spinX.getValue();
            int y = (Integer) spinY.getValue();

            // Validation: Prevent projecting the same dimension twice
            if (x == y) {
                JOptionPane.showMessageDialog(this, "X and Y axes must be different!");
                return;
            }

            // Create and dispatch the Command to update the axes (supports Undo).
            ICommand cmd = new ChangeAxisCommand(SpaceEngine.getSpaceManager(), x, y);
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });
        axisPanel.add(btnApplyAxes); // Add the "Apply Axes" execution button to the panel

        /**
         * --- 3D Camera Controls ---
         */

        JPanel cameraPanel = new JPanel(); // Create a blank panel for the camera controls
        cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS)); // Arrange everything inside it vertically (Top to Bottom)
        cameraPanel.setBorder(BorderFactory.createTitledBorder("3D Camera Control")); // Draw a visible border around it with the title "3D Camera Control
        cameraPanel.setMaximumSize(new Dimension(320, 150)); // Stop the panel from stretching too big on the screen

        JLabel lblYaw = new JLabel("Rotate Left/Right (Yaw)"); // Create simple text saying "Rotate Left/Right"
        lblYaw.setAlignmentX(Component.CENTER_ALIGNMENT); // Push the text exactly to the center of the panel

        JSlider sliderYaw = new JSlider(-180, 180, 0); // Create a slider: Min -180, Max 180, Starts at 0
        sliderYaw.setMajorTickSpacing(90);// Add a small line (tick) every 90 degrees on the slider
        sliderYaw.setPaintTicks(true); // Make those tiny lines visible to the user

        // Real-time listener: Updates engine state as slider moves
        sliderYaw.addChangeListener(e -> {
            double radians = Math.toRadians(sliderYaw.getValue());
            SpaceEngine.getSpaceManager().setRotationY(radians);
        });

        JLabel lblPitch = new JLabel("Rotate Up/Down (Pitch)"); // Create text for the Up/Down rotation
        lblPitch.setAlignmentX(Component.CENTER_ALIGNMENT); // Center this text too
        // Create the second slider for Up/Down
        JSlider sliderPitch = new JSlider(-180, 180, 0);
        sliderPitch.setMajorTickSpacing(90);
        sliderPitch.setPaintTicks(true);

        // Listen to the Up/Down slider
        sliderPitch.addChangeListener(e -> {
            double radians = Math.toRadians(sliderPitch.getValue());
            SpaceEngine.getSpaceManager().setRotationX(radians);
        });

        JButton btnResetCam = new JButton("Reset Camera"); // Create a clickable "Reset Camera" button
        btnResetCam.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Wait for the user to click the button
        btnResetCam.addActionListener(e -> {
            sliderYaw.setValue(0);
            sliderPitch.setValue(0);
        });

        // glue all these items onto cameraPanel
        cameraPanel.add(lblYaw);      // Add the "Rotate Left/Right (Yaw)" text label
        cameraPanel.add(sliderYaw);   // Add the slider that controls the Yaw (Y-axis) rotation
        cameraPanel.add(lblPitch);    // Add the "Rotate Up/Down (Pitch)" text label
        cameraPanel.add(sliderPitch); // Add the slider that controls the Pitch (X-axis) rotation

        cameraPanel.add(Box.createVerticalStrut(5)); // Add 5 pixels of empty transparent space as a spacer
        cameraPanel.add(btnResetCam); // Add the Reset button at the bottom

        /**
         * --- K-Nearest Neighbors Setting ---
         */
        // Create a panel and arrange items in a single centered row (FlowLayout)
        // with a 10px horizontal gap and 0px vertical gap between them.
        JPanel kPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        kPanel.setMaximumSize(new Dimension(320, 30)); // Lock the height to 30px so this row stays nice and thin.
        kPanel.add(new JLabel("Neighbors to show (K):"));

        // Create the spinner (starts at 5, minimum 1, maximum 50, jumps by 1).
        JSpinner spinK = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        spinK.addChangeListener(e -> { // Listen to the UP/DOWN arrows of the spinner.
            int newK = (Integer) spinK.getValue();
            SpaceEngine.getSpaceManager().setKNeighbors(newK);
        });

        // Ensure manual keyboard inputs are saved immediately
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinK.getEditor();
        // Attach a physical keyboard listener directly to that text field.
        editor.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try { spinK.commitEdit(); } // Force the Spinner to immediately save the typed text as its official value!
                catch (ParseException ex) {} // Ignore errors if the user typed something invalid
            }
        });
        kPanel.add(spinK);  // add the fully working spinner to the panel.

        /**
         * --- Word Arithmetic Lab (Analogy) ---
         */

        JPanel labPanel = new JPanel(); // Create the main container for this lab, stacked vertically
        labPanel.setLayout(new BoxLayout(labPanel, BoxLayout.Y_AXIS));
        labPanel.setBorder(BorderFactory.createTitledBorder("Word Arithmetic Lab"));
        labPanel.setMaximumSize(new Dimension(320, 150));

        // Create three empty text boxes for typing words (width of ~7 characters each)
        JTextField txtWord1 = new JTextField(7);
        JTextField txtWord2 = new JTextField(7);
        JTextField txtWord3 = new JTextField(7);

        // Create a label to show the final answer, styled with Bold font and Blue color
        JLabel lblMathResult = new JLabel("Result: ?");
        lblMathResult.setFont(new Font("Arial", Font.BOLD, 14)); // change Font
        lblMathResult.setForeground(Color.BLUE); // change textColor
        lblMathResult.setAlignmentX(Component.CENTER_ALIGNMENT); //pot it in thr center

        // Create an INNER panel just for the equation, arranged side-by-side (FlowLayout)
        JPanel pnlEquation = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        // Add the boxes and math symbols left-to-right to build: [WORD] - [WORD] + [WORD]
        pnlEquation.add(txtWord1);
        pnlEquation.add(new JLabel(" - "));
        pnlEquation.add(txtWord2);
        pnlEquation.add(new JLabel(" + "));
        pnlEquation.add(txtWord3);

        JButton btnCalculateMath = new JButton("Calculate (=)"); // Create the action button and center it
        btnCalculateMath.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCalculateMath.addActionListener(e -> { // Listen for a click on the Calculate button
            String w1 = txtWord1.getText().trim();
            String w2 = txtWord2.getText().trim();
            String w3 = txtWord3.getText().trim();

            if (w1.isEmpty() || w2.isEmpty() || w3.isEmpty()) { // Validation: Check if the user forgot to type a word in one of the boxes
                JOptionPane.showMessageDialog(this, "Please fill in all 3 words!");
                return;
            }

            try {
                // Execute calculation via engine and update UI
                ILatentEntity resultEntity = SpaceEngine.getSpaceManager().calculateAnalogy(w1, w2, w3);
                // 11. Update the blue label with the winner's name (or "Not found" if null)
                lblMathResult.setText("Result: " + (resultEntity != null ? resultEntity.getIdentifier() : "Not found"));
            } catch (Exception ex) {
                // Error handling to prevent UI crashes
                JOptionPane.showMessageDialog(this, "Error calculating analogy. Check if words exist.");
            }
        });
        // assemble the main panel with transparent spacers
        labPanel.add(Box.createVerticalStrut(10)); // 10px transparent vertical spacer
        labPanel.add(pnlEquation);                 // The inner equation panel created earlier
        labPanel.add(Box.createVerticalStrut(10)); // 10px transparent vertical spacer
        labPanel.add(btnCalculateMath);            // The calculation execution button
        labPanel.add(Box.createVerticalStrut(15)); // 15px transparent vertical spacer
        labPanel.add(lblMathResult);               // The blue label displaying the final result
        labPanel.add(Box.createVerticalStrut(10)); // Final 10px transparent vertical spacer at the bottom

        /**
         * --- Cluster Centroid (Center Point) ---
         */

        JPanel centroidPanel = new JPanel(); // Create the main panel for the centroid
        centroidPanel.setLayout(new BoxLayout(centroidPanel, BoxLayout.Y_AXIS)); // Set vertical layout
        centroidPanel.setBorder(BorderFactory.createTitledBorder("Cluster Centroid"));
        centroidPanel.setMaximumSize(new Dimension(320, 160)); // Constrain the maximum size to prevent the panel from stretching

        JLabel lblCentroidInst = new JLabel("Enter words separated by commas:"); // Explanatory label for the user (Centered)
        lblCentroidInst.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtCentroidInput = new JTextField(20); // Text field for user input
        txtCentroidInput.setMaximumSize(new Dimension(280, 25)); // CRITICAL UI FIX: Lock the height to 25px so it doesn't stretch vertically

        // Label to display the calculated result (Bold, Green, Centered)
        JLabel lblCentroidResult = new JLabel("Center: ?");
        lblCentroidResult.setFont(new Font("Arial", Font.BOLD, 14));
        lblCentroidResult.setForeground(new Color(0, 128, 0));
        lblCentroidResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnFindCenter = new JButton("Find Center"); // Button to trigger the calculation (Centered)
        btnFindCenter.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFindCenter.addActionListener(e -> { // Listen for button clicks
            String input = txtCentroidInput.getText(); // Get the text inputted by the user
            if (input == null || input.trim().isEmpty()) { // Validation: Ensure input is not empty
                JOptionPane.showMessageDialog(this, "Please enter at least one word.");
                return;
            }

            // Parse input string into a list of words using Regex (split by comma)
            List<String> wordList = Arrays.asList(input.split("\\s*,\\s*"));

            try {
                // Ask the Engine to calculate the center point of these words
                ILatentEntity centerEntity = SpaceEngine.getSpaceManager().calculateCentroid(wordList);
                lblCentroidResult.setText("Center: " + (centerEntity != null ? centerEntity.getIdentifier() : "Not found")); // Update the green label with the result
            } catch (Exception ex) { // Error handling if words don't exist in the dictionary
                JOptionPane.showMessageDialog(this, "Error finding centroid. Check if words exist in dictionary.");
            }
        });

        // Add components with transparent vertical spacers (Struts)
        centroidPanel.add(Box.createVerticalStrut(10)); // 10px top margin
        centroidPanel.add(lblCentroidInst);             // Instructions label
        centroidPanel.add(Box.createVerticalStrut(5));  // Small gap
        centroidPanel.add(txtCentroidInput);            // Input box
        centroidPanel.add(Box.createVerticalStrut(10)); // Spacer before button
        centroidPanel.add(btnFindCenter);               // Calculate button
        centroidPanel.add(Box.createVerticalStrut(10)); // Spacer before result
        centroidPanel.add(lblCentroidResult);           // Result label
        centroidPanel.add(Box.createVerticalStrut(10)); // 10px bottom margin

        /**
         * --- Distance Calculator ---
         */

        JPanel distPanel = new JPanel(); // Create the main panel for the distance calculator
        distPanel.setLayout(new BoxLayout(distPanel, BoxLayout.Y_AXIS)); // Set vertical layout
        distPanel.setBorder(BorderFactory.createTitledBorder("Distance Calculator"));
        distPanel.setMaximumSize(new Dimension(320, 140)); // Lock maximum size to prevent vertical stretching

        JPanel pnlDistWords = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Create an inner panel for the text boxes, arranged side-by-side
        // Create two text input fields (width of 8 characters each)
        JTextField txtDist1 = new JTextField(8);
        JTextField txtDist2 = new JTextField(8);
        // Add them to the inner panel with an "and" label in between: [WORD1] and [WORD2]
        pnlDistWords.add(txtDist1);
        pnlDistWords.add(new JLabel(" and "));
        pnlDistWords.add(txtDist2);

        // Create a label to show the calculated distance (Bold, Pink, Centered)
        JLabel lblDistResult = new JLabel("Distance: ?");
        lblDistResult.setFont(new Font("Arial", Font.BOLD, 14));
        lblDistResult.setForeground(new Color(204, 0, 102));
        lblDistResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create the calculate button and center it
        JButton btnCalcDist = new JButton("Calculate Distance");
        btnCalcDist.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCalcDist.addActionListener(e -> { // Listen for button clicks
            String w1 = txtDist1.getText().trim();
            String w2 = txtDist2.getText().trim();

            if (w1.isEmpty() || w2.isEmpty()) { // Check if the user left a box empty
                JOptionPane.showMessageDialog(this, "Please enter two words.");
                return;
            }
            // Ask the Engine to fetch the 3D data (entities) for these words
            ILatentEntity ent1 = SpaceEngine.getSpaceManager().getEntity(w1);
            ILatentEntity ent2 = SpaceEngine.getSpaceManager().getEntity(w2);

            // Validation: Ensure both entities exist and have valid vectors
            if (ent1 == null || ent2 == null || ent1.getFullVector() == null || ent2.getFullVector() == null) {
                JOptionPane.showMessageDialog(this, "One or both words not found in dictionary.");
                return;
            }

            // Calculate distance dynamically using the currently active Strategy (Euclidean / Cosine)
            double distance = SpaceEngine.getSpaceManager().getCurrentStrategy().calculateDistance(ent1.getFullVector(), ent2.getFullVector());
            lblDistResult.setText(String.format("Distance: %.4f", distance)); // Update label, formatting the number to 4 decimal places (e.g., 0.1234)
        });

        // Add components with transparent vertical spacers
        distPanel.add(Box.createVerticalStrut(10)); // 10px top margin
        distPanel.add(pnlDistWords);                // The inner words panel
        distPanel.add(Box.createVerticalStrut(5));  // 5px gap
        distPanel.add(btnCalcDist);                 // Calculate button
        distPanel.add(Box.createVerticalStrut(10)); // 10px gap
        distPanel.add(lblDistResult);               // Result label
        distPanel.add(Box.createVerticalStrut(10)); // 10px bottom margin

        /**
         * --- Semantic Scale (1D Projection) ---
         */

        JPanel scalePanel = new JPanel(); // Create the main panel for the 1D scale module
        scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS)); // Set vertical layout (stack top to bottom)
        scalePanel.setBorder(BorderFactory.createTitledBorder("Semantic Scale (1D)"));
        scalePanel.setMaximumSize(new Dimension(320, 100)); // Lock max size to prevent stretching

        JPanel pnlScaleWords = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Create an inner panel for words side-by-side (FlowLayout)
        // Text boxes for the two opposing words
        JTextField txtScale1 = new JTextField(8);
        JTextField txtScale2 = new JTextField(8);
        // Add boxes with "vs" in the middle: [Box1] vs [Box2]
        pnlScaleWords.add(txtScale1);
        pnlScaleWords.add(new JLabel(" vs "));
        pnlScaleWords.add(txtScale2);

        JButton btnShowScale = new JButton("Project Axis"); // Create and center the execution button
        btnShowScale.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnShowScale.addActionListener(e -> { // Listen for button clicks
            String w1 = txtScale1.getText().trim();
            String w2 = txtScale2.getText().trim();

            if (w1.isEmpty() || w2.isEmpty()) { // Empty fields validation
                JOptionPane.showMessageDialog(this, "Please enter two words.");
                return;
            }

            // Dictionary validation: check if both words exist in the model
            if (SpaceEngine.getSpaceManager().getEntity(w1) == null ||
                    SpaceEngine.getSpaceManager().getEntity(w2) == null) {
                JOptionPane.showMessageDialog(this, "One or both words not found in dictionary.");
                return;
            }

            // Launch a secondary detached window for 1D projection
            new SemanticScaleWindow(SpaceEngine.getSpaceManager(), w1, w2).setVisible(true);
        });
        // Assemble the scale panel with small 5px gaps
        scalePanel.add(Box.createVerticalStrut(5)); // Top gap
        scalePanel.add(pnlScaleWords);              // Words input
        scalePanel.add(Box.createVerticalStrut(5)); // Middle gap
        scalePanel.add(btnShowScale);               // Action button
        scalePanel.add(Box.createVerticalStrut(5)); // Bottom gap

        /**
         * Final assembly: Attach all to the parent panel
         */

        //Stack all the modules we built into the main right-side panel!
        panel.add(sysPanel);                        // System controls (Undo/Metric)
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(axisPanel);                       // PCA axes selection
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(cameraPanel);                     // 3D Camera controls
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(kPanel);                          // KNN setting spinner
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(labPanel);                        // Word arithmetic lab
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(centroidPanel);                   // Cluster centroid calculation
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(distPanel);                       // Distance calculator
        panel.add(Box.createVerticalStrut(15));     // 15px spacer
        panel.add(scalePanel);                      // Semantic scale (1D)

        // Invisible spring to push all panels to the top, preventing vertical spread
        panel.add(Box.createVerticalGlue());

        return panel;
    }
}