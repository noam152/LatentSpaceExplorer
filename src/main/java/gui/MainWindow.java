package gui;

import model.ILatentEntity;
import history.ChangeAxisCommand;
import history.ChangeMetricCommand;
import history.ICommand;
import engine.LatentSpaceEngine;
import engine.IObserver;
import math.CosineDistance;
import math.EuclideanDistance;
import state.State2D;
import state.State3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * MainWindow - The Dashboard of the Application.
 */
public class MainWindow extends JFrame implements IObserver {

    private LatentSpaceEngine SpaceEngine;
    private LatentSpaceView view;
    private JTextArea txtNeighborsDisplay;
    private JSpinner spinX, spinY, spinZ;

    public MainWindow(LatentSpaceEngine SpaceEngine) {
        this.SpaceEngine = SpaceEngine;

        // window setup
        setTitle("LatentSpace Explorer");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Arrange components like a compass

        // Initialize the KNN text area
        txtNeighborsDisplay = new JTextArea(8, 20);
        txtNeighborsDisplay.setEditable(false); // Make the text box read-only.
        txtNeighborsDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtNeighborsDisplay.setBackground(new Color(245, 245, 245));

        // Build the main viewport (the canvas where the space is drawn)
        view = new LatentSpaceView(SpaceEngine.getSpaceManager());
        add(view, BorderLayout.CENTER);

        // Listen for user clicks and engine updates
        view.setOnWordSelected(word -> refreshNeighborsDisplay(word));
        SpaceEngine.getSpaceManager().addObserver(this);
        refreshNeighborsDisplay(null);

        // Assemble the right-side control dashboard
        JPanel controlPanel = createControlPanel();
        JScrollPane scrollPane = new JScrollPane(controlPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(360, getHeight()));
        add(scrollPane, BorderLayout.EAST);
    }

    /**
     * The Main Assembly Line.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Stack items vertically
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Assemble the panels with a 15px transparent gap between them
        panel.add(createSearchPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createMetricPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createAxisPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createUndoPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createDisplayModePanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createCameraPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createKnnPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createArithmeticPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createCentroidPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createDistanceCalcPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createSemanticScalePanel());

        panel.add(Box.createVerticalGlue()); // An invisible spring that pushes all panels to the top

        return panel;
    }

    // Search Module
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Word"));
        searchPanel.setMaximumSize(new Dimension(320, 80)); //constrain the size

        JTextField txtSearch = new JTextField(10);
        JButton btnSearch = new JButton("Search");
        JCheckBox chkAutoCenter = new JCheckBox("Auto-Center", false);

        btnSearch.addActionListener(e -> { //listener
            String wordToFind = txtSearch.getText().trim();
            if (!wordToFind.isEmpty()) {
                ILatentEntity foundWord = view.searchAndSelectWord(wordToFind);
                // "Teleport" the camera to the word if the "auto-center" is active
                if (foundWord != null && chkAutoCenter.isSelected()) {
                    view.zoomAndCenterOnEntity(foundWord);
                }
            }
        });

        // Trigger the search button automatically if the user presses 'Enter' inside the text box
        txtSearch.addActionListener(e -> btnSearch.doClick());

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(chkAutoCenter);
        return searchPanel;
    }

    // Distance Metric Module (Cosine / Euclidean)
    private JPanel createMetricPanel() {
        JPanel metricPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        metricPanel.setMaximumSize(new Dimension(320, 75)); // constrain the size

        JButton btnEuclidean = new JButton("Change to Euclidean");
        btnEuclidean.addActionListener(e -> {
            ICommand cmd = new ChangeMetricCommand(SpaceEngine.getSpaceManager(), new EuclideanDistance());
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });

        JButton btnCosine = new JButton("Change to Cosine");
        btnCosine.addActionListener(e -> {
            ICommand cmd = new ChangeMetricCommand(SpaceEngine.getSpaceManager(), new CosineDistance());
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });

        metricPanel.add(btnEuclidean);
        metricPanel.add(btnCosine);
        return metricPanel;
    }

    // PCA Axes Selection Module
    private JPanel createAxisPanel() {
        JPanel axisPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        axisPanel.setBorder(BorderFactory.createTitledBorder("PCA Axes Selection"));
        axisPanel.setMaximumSize(new Dimension(320, 140));

        spinX = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        spinY = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        spinZ = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));

        axisPanel.add(new JLabel(" X Axis (PC):")); axisPanel.add(spinX);
        axisPanel.add(new JLabel(" Y Axis (PC):")); axisPanel.add(spinY);
        axisPanel.add(new JLabel(" Z Axis (PC):")); axisPanel.add(spinZ);
        axisPanel.add(new JLabel("")); // Empty placeholder to align the button properly

        JButton btnApplyAxes = new JButton("Apply Axes");
        btnApplyAxes.addActionListener(e -> {
            int x = (Integer) spinX.getValue();
            int y = (Integer) spinY.getValue();
            int z = (Integer) spinZ.getValue();
            boolean is3DMode = !SpaceEngine.getSpaceManager().getProjectionState().is2D();

            // Validation: Prevent the user from projecting the same dimension twice
            if (x == y || (is3DMode && (x == z || y == z))) {
                String error = "Axes must be unique!\n";
                if (x == y) error += " X and Y are both " + x + "\n";
                if (is3DMode && x == z) error += " X and Z are both " + x + "\n";
                if (is3DMode && y == z) error += " Y and Z are both " + y + "\n";
                JOptionPane.showMessageDialog(this, error, "Invalid Axes", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ICommand cmd = new ChangeAxisCommand(SpaceEngine.getSpaceManager(), x, y, z);
            SpaceEngine.getCommandManager().executeCommand(cmd);
        });

        axisPanel.add(btnApplyAxes);
        return axisPanel;
    }

    // Undo Module
    private JPanel createUndoPanel() {
        JPanel undoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        undoPanel.setMaximumSize(new Dimension(320, 45));

        JButton btnUndo = new JButton("Undo Last Action");
        btnUndo.addActionListener(e -> SpaceEngine.getCommandManager().undoLastCommand());
        undoPanel.add(btnUndo);

        return undoPanel;
    }

    // Display Mode (2D/3D) Module
    private JPanel createDisplayModePanel() {
        JPanel displayModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        displayModePanel.setBorder(BorderFactory.createTitledBorder("View Mode"));
        displayModePanel.setMaximumSize(new Dimension(320, 60));

        JRadioButton radio2D = new JRadioButton("2D", true);
        JRadioButton radio3D = new JRadioButton("3D", false);

        // ButtonGroup ensures only one radio button can be active at a time
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(radio2D);
        modeGroup.add(radio3D);

        radio2D.addActionListener(e -> SpaceEngine.getSpaceManager().setProjectionState(new State2D()));
        radio3D.addActionListener(e -> SpaceEngine.getSpaceManager().setProjectionState(new State3D()));

        displayModePanel.add(radio2D);
        displayModePanel.add(radio3D);
        return displayModePanel;
    }

    // 3D Camera Controls Module
    private JPanel createCameraPanel() {
        JPanel cameraPanel = new JPanel();
        cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));
        cameraPanel.setBorder(BorderFactory.createTitledBorder("3D Camera Control"));
        cameraPanel.setMaximumSize(new Dimension(320, 150));

        // Yaw Slider (Horizontal Rotation)
        JLabel lblYaw = new JLabel("Rotate Left/Right (Yaw)");
        lblYaw.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider sliderYaw = new JSlider(-180, 180, 0);
        sliderYaw.setMajorTickSpacing(90);
        sliderYaw.setPaintTicks(true);
        sliderYaw.addChangeListener(e -> SpaceEngine.getSpaceManager().setRotationY(Math.toRadians(sliderYaw.getValue())));

        // Pitch Slider (Vertical Rotation)
        JLabel lblPitch = new JLabel("Rotate Up/Down (Pitch)");
        lblPitch.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider sliderPitch = new JSlider(-180, 180, 0);
        sliderPitch.setMajorTickSpacing(90);
        sliderPitch.setPaintTicks(true);
        sliderPitch.addChangeListener(e -> SpaceEngine.getSpaceManager().setRotationX(Math.toRadians(sliderPitch.getValue())));

        JButton btnResetCam = new JButton("Reset Camera");
        btnResetCam.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResetCam.addActionListener(e -> {
            sliderYaw.setValue(0);
            sliderPitch.setValue(0);
        });

        cameraPanel.add(lblYaw);
        cameraPanel.add(sliderYaw);
        cameraPanel.add(lblPitch);
        cameraPanel.add(sliderPitch);
        cameraPanel.add(Box.createVerticalStrut(5));
        cameraPanel.add(btnResetCam);
        return cameraPanel;
    }

    // K-Nearest Neighbors (KNN) Module
    private JPanel createKnnPanel() {
        JPanel knnMasterPanel = new JPanel();
        knnMasterPanel.setLayout(new BoxLayout(knnMasterPanel, BoxLayout.Y_AXIS));
        knnMasterPanel.setBorder(BorderFactory.createTitledBorder("KNN Settings & Results"));
        knnMasterPanel.setMaximumSize(new Dimension(320, 200));

        JPanel kSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        kSettingsPanel.add(new JLabel("Neighbors to show (K):"));

        JSpinner spinK = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        spinK.addChangeListener(e -> SpaceEngine.getSpaceManager().setKNeighbors((Integer) spinK.getValue()));

        // Ensure that manual keyboard input into the spinner is saved instantly
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinK.getEditor();
        editor.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    String inputText = editor.getTextField().getText();

                    if (!inputText.isEmpty()) {
                        int typedValue = Integer.parseInt(inputText);

                        if (typedValue > 50) {
                            spinK.setValue(50); // Snap back to max
                        }

                        if (typedValue < 1) {
                            spinK.setValue(1); // Snap back to minimum
                        }

                        // Save valid input to the engine instantly
                        else if (typedValue >= 1) {
                            spinK.commitEdit();
                        }
                    }}
                catch (ParseException ex) {}
            }
        });

        kSettingsPanel.add(spinK);
        JScrollPane scrollNeighbors = new JScrollPane(txtNeighborsDisplay);
        scrollNeighbors.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        knnMasterPanel.add(kSettingsPanel);
        knnMasterPanel.add(Box.createVerticalStrut(5));
        knnMasterPanel.add(scrollNeighbors);
        return knnMasterPanel;
    }

    // Word Arithmetic Lab Module
    private JPanel createArithmeticPanel() {
        JPanel labPanel = new JPanel();
        labPanel.setLayout(new BoxLayout(labPanel, BoxLayout.Y_AXIS));
        labPanel.setBorder(BorderFactory.createTitledBorder("Word Arithmetic Lab"));
        labPanel.setMaximumSize(new Dimension(320, 150));

        JTextField txtWord1 = new JTextField(7);
        JTextField txtWord2 = new JTextField(7);
        JTextField txtWord3 = new JTextField(7);

        JLabel lblMathResult = new JLabel("Result: ?");
        lblMathResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pnlEquation = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        pnlEquation.add(txtWord1);
        pnlEquation.add(new JLabel(" - "));
        pnlEquation.add(txtWord2);
        pnlEquation.add(new JLabel(" + "));
        pnlEquation.add(txtWord3);

        JButton btnCalculateMath = new JButton("Calculate (=)");
        btnCalculateMath.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCalculateMath.addActionListener(e -> {
            String w1 = txtWord1.getText().trim();
            String w2 = txtWord2.getText().trim();
            String w3 = txtWord3.getText().trim();

            if (w1.isEmpty() || w2.isEmpty() || w3.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all 3 words!");
                return;
            }

            try {
                ILatentEntity resultEntity = SpaceEngine.getSpaceManager().calculateAnalogy(w1, w2, w3);
                lblMathResult.setText("Result: " + (resultEntity != null ? resultEntity.getIdentifier() : "Not found"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error calculating analogy. Check if words exist.");
            }
        });

        labPanel.add(Box.createVerticalStrut(10));
        labPanel.add(pnlEquation);
        labPanel.add(Box.createVerticalStrut(10));
        labPanel.add(btnCalculateMath);
        labPanel.add(Box.createVerticalStrut(15));
        labPanel.add(lblMathResult);
        labPanel.add(Box.createVerticalStrut(10));
        return labPanel;
    }

    // Cluster Centroid Calculation Module
    private JPanel createCentroidPanel() {
        JPanel centroidPanel = new JPanel();
        centroidPanel.setLayout(new BoxLayout(centroidPanel, BoxLayout.Y_AXIS));
        centroidPanel.setBorder(BorderFactory.createTitledBorder("Cluster Centroid"));
        centroidPanel.setMaximumSize(new Dimension(320, 160));

        JLabel lblCentroidInst = new JLabel("Enter words separated by commas:");
        lblCentroidInst.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField txtCentroidInput = new JTextField(20);
        txtCentroidInput.setMaximumSize(new Dimension(280, 25));

        JLabel lblCentroidResult = new JLabel("Center: ?");
        lblCentroidResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnFindCenter = new JButton("Find Center");
        btnFindCenter.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFindCenter.addActionListener(e -> {
            String input = txtCentroidInput.getText();
            if (input == null || input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter at least one word.");
                return;
            }

            // Split the input string into a list of words using Regex (comma separation)
            List<String> wordList = Arrays.asList(input.split("\\s*,\\s*"));
            try {
                ILatentEntity centerEntity = SpaceEngine.getSpaceManager().calculateCentroid(wordList);
                lblCentroidResult.setText("Center: " + (centerEntity != null ? centerEntity.getIdentifier() : "Not found"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error finding centroid. Check if words exist.");
            }
        });

        centroidPanel.add(Box.createVerticalStrut(10));
        centroidPanel.add(lblCentroidInst);
        centroidPanel.add(Box.createVerticalStrut(5));
        centroidPanel.add(txtCentroidInput);
        centroidPanel.add(Box.createVerticalStrut(10));
        centroidPanel.add(btnFindCenter);
        centroidPanel.add(Box.createVerticalStrut(10));
        centroidPanel.add(lblCentroidResult);
        centroidPanel.add(Box.createVerticalStrut(10));
        return centroidPanel;
    }

    // Direct Distance Calculator Module
    private JPanel createDistanceCalcPanel() {
        JPanel distPanel = new JPanel();
        distPanel.setLayout(new BoxLayout(distPanel, BoxLayout.Y_AXIS));
        distPanel.setBorder(BorderFactory.createTitledBorder("Distance Calculator"));
        distPanel.setMaximumSize(new Dimension(320, 140));

        JPanel pnlDistWords = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JTextField txtDist1 = new JTextField(8);
        JTextField txtDist2 = new JTextField(8);
        pnlDistWords.add(txtDist1);
        pnlDistWords.add(new JLabel(" and "));
        pnlDistWords.add(txtDist2);

        JLabel lblDistResult = new JLabel("Distance: ?");
        lblDistResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCalcDist = new JButton("Calculate Distance");
        btnCalcDist.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCalcDist.addActionListener(e -> {
            String w1 = txtDist1.getText().trim();
            String w2 = txtDist2.getText().trim();

            if (w1.isEmpty() || w2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter two words.");
                return;
            }

            ILatentEntity ent1 = SpaceEngine.getSpaceManager().getEntity(w1);
            ILatentEntity ent2 = SpaceEngine.getSpaceManager().getEntity(w2);

            if (ent1 == null || ent2 == null || ent1.getFullVector() == null || ent2.getFullVector() == null) {
                JOptionPane.showMessageDialog(this, "One or both words not found in dictionary.");
                return;
            }

            // Calculate the distance based on the currently active Strategy (Cosine or Euclidean)
            double distance = SpaceEngine.getSpaceManager().getCurrentStrategy().calculateDistance(ent1.getFullVector(), ent2.getFullVector());
            lblDistResult.setText(String.format("Distance: %.4f", distance));
        });

        distPanel.add(Box.createVerticalStrut(10));
        distPanel.add(pnlDistWords);
        distPanel.add(Box.createVerticalStrut(5));
        distPanel.add(btnCalcDist);
        distPanel.add(Box.createVerticalStrut(10));
        distPanel.add(lblDistResult);
        distPanel.add(Box.createVerticalStrut(10));
        return distPanel;
    }

    // Semantic Scale (1D Projection) Module
    private JPanel createSemanticScalePanel() {
        JPanel scalePanel = new JPanel();
        scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS));
        scalePanel.setBorder(BorderFactory.createTitledBorder("Semantic Scale (1D)"));

        scalePanel.setMaximumSize(new Dimension(320, 140));

        JPanel pnlScaleWords = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JTextField txtScale1 = new JTextField(8);
        JTextField txtScale2 = new JTextField(8);

        pnlScaleWords.add(txtScale1);
        pnlScaleWords.add(new JLabel(" vs "));
        pnlScaleWords.add(txtScale2);

        JPanel pnlK = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlK.add(new JLabel("Top K Words:"));

        JSpinner spinScaleK = new JSpinner(new SpinnerNumberModel(15, 1, 200, 1));
        pnlK.add(spinScaleK);

        JButton btnShowScale = new JButton("Project Axis");
        btnShowScale.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnShowScale.addActionListener(e -> {
            String w1 = txtScale1.getText().trim();
            String w2 = txtScale2.getText().trim();

            int k = (Integer) spinScaleK.getValue();

            if (w1.isEmpty() || w2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter two words.");
                return;
            }

            if (SpaceEngine.getSpaceManager().getEntity(w1) == null || SpaceEngine.getSpaceManager().getEntity(w2) == null) {
                JOptionPane.showMessageDialog(this, "One or both words not found in dictionary.");
                return;
            }

            new SemanticScaleWindow(SpaceEngine.getSpaceManager(), w1, w2, k).setVisible(true);
        });

        scalePanel.add(Box.createVerticalStrut(5));
        scalePanel.add(pnlScaleWords);

        scalePanel.add(pnlK);

        scalePanel.add(Box.createVerticalStrut(5));
        scalePanel.add(btnShowScale);
        scalePanel.add(Box.createVerticalStrut(5));

        return scalePanel;
    }

    /**
     * Refreshes the KNN text area when a new word is selected.
     */
    private void refreshNeighborsDisplay(String word) {
        if (word == null) {
            txtNeighborsDisplay.setText(" Click on a word to\n see its neighbors.");
            return;
        }

        int k = SpaceEngine.getSpaceManager().getKNeighbors();
        List<ILatentEntity> neighbors = SpaceEngine.getSpaceManager().getKNearestNeighbors(word, k);

        StringBuilder sb = new StringBuilder();
        sb.append(" Neighbors for '").append(word).append("':\n");
        sb.append(" ----------------------\n");

        for (int i = 0; i < neighbors.size(); i++) {
            sb.append(" ").append(i + 1).append(". ").append(neighbors.get(i).getIdentifier()).append("\n");
        }

        txtNeighborsDisplay.setText(sb.toString());
        txtNeighborsDisplay.setCaretPosition(0); // Force scroll back to top
    }

    /**
     * Triggered automatically when the engine state changes (e.g., Undo, Metric change).
     */
    @Override
    public void update() {
        // Update the neighbor list based on the new engine state
        refreshNeighborsDisplay(view.getSelectedWord());

        // Ensure the UI spinners match the actual internal engine axes (crucial for Undo)
        if (spinX != null && spinY != null && spinZ != null) {
            spinX.setValue(SpaceEngine.getSpaceManager().getXAxisIndex());
            spinY.setValue(SpaceEngine.getSpaceManager().getYAxisIndex());
            spinZ.setValue(SpaceEngine.getSpaceManager().getZAxisIndex());
        }
    }
}