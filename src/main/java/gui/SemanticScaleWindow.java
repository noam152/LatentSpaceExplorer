package gui;

import engine.SpaceManager;
import engine.SpaceManager.ProjectionResult;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SemanticScaleWindow: A clean dashboard that displays the two extreme poles
 * of a semantic axis (e.g., the top words closest to "Poor" vs "Rich").
 */
public class SemanticScaleWindow extends JFrame {

    public SemanticScaleWindow(SpaceManager spaceManager, String w1, String w2, int k) {

        if (spaceManager.getEntity(w1) == null || spaceManager.getEntity(w2) == null) {
            JOptionPane.showMessageDialog(null, "One of the words is missing from the dictionary!");
            return;
        }

        setTitle("Semantic Poles: " + w1 + " <---> " + w2);
        setSize(900, 500);
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //  bring the filtered data from our updated Brain (SpaceManager)
        Map<String, List<ProjectionResult>> poles = spaceManager.getSemanticPoles(w1, w2, k);

        // Set up the Main Window Layout
        setLayout(new BorderLayout(10, 10));

        JLabel headerLabel = new JLabel(w1.toUpperCase() + " vs " + w2.toUpperCase(), SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(headerLabel, BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        splitPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // The Right Pole list (Closest to w2) is sorted smallest to largest.
        List<ProjectionResult> rightWords = poles.get("right");
        Collections.reverse(rightWords);

        // Create the two lists using our helper function
        JPanel leftPanel = createPolePanel("Closest to: " + w1, poles.get("left"), new Color(180, 50, 50));
        JPanel rightPanel = createPolePanel("Closest to: " + w2, rightWords, new Color(50, 150, 50));

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);

        add(splitPanel, BorderLayout.CENTER);
    }

    /**
     * Helper function to build a beautiful list panel for a specific pole.
     */
    private JPanel createPolePanel(String title, List<ProjectionResult> words, Color borderColor) {
        JPanel panel = new JPanel(new BorderLayout());

        // Create a styled border with the title
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 2), title));

        // DefaultListModel is the data container for a JList
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (words != null) {
            for (ProjectionResult pr : words) {
                // Formatting: Limits the score to 3 decimal points, and pads the word to 20 characters
                String formattedLine = String.format("%-20s [ Score: %6.3f ]", pr.word, pr.score);
                listModel.addElement(formattedLine);
            }
        }

        // Create the UI List component
        JList<String> list = new JList<>(listModel);
        list.setFont(new Font("Monospaced", Font.BOLD, 14));
        list.setBackground(new Color(245, 245, 245)); // Light gray background
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Wrap the list in a ScrollPane in case K is large
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}