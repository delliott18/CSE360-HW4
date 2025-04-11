import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TrustedReviewerDialog extends JDialog {
    private final String currentUser;
    private DefaultTableModel tableModel;
    private JTable reviewersTable;
    private JSpinner weightageSpinner;

    public TrustedReviewerDialog(Frame owner, String currentUser) {
        super(owner, "Manage Trusted Reviewers", true);
        this.currentUser = currentUser;

        setSize(500, 400);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table for trusted reviewers
        String[] columns = {"Reviewer", "Weightage"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reviewersTable = new JTable(tableModel);
        reviewersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reviewersTable.setPreferredScrollableViewportSize(new Dimension(450, 200));

        JScrollPane scrollPane = new JScrollPane(reviewersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Trusted Reviewers"));
        scrollPane.setPreferredSize(new Dimension(450, 200));

        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // Add reviewer panel
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField reviewerField = new JTextField(15);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 10, 1);
        weightageSpinner = new JSpinner(spinnerModel);
        JButton addButton = new JButton("Add/Update");

        addPanel.add(new JLabel("Reviewer:"));
        addPanel.add(reviewerField);
        addPanel.add(new JLabel("Weightage (1-10):"));
        addPanel.add(weightageSpinner);
        addPanel.add(addButton);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remove");
        JButton closeButton = new JButton("Close");

        actionPanel.add(removeButton);
        actionPanel.add(closeButton);

        controlPanel.add(addPanel);
        controlPanel.add(actionPanel);

        // Add listeners
        addButton.addActionListener(e -> {
            String reviewerUsername = reviewerField.getText().trim();
            if (reviewerUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a reviewer username", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if reviewer exists and has reviewer role
            if (!MemoryStorage.userExists(reviewerUsername) || 
                !MemoryStorage.hasRole(reviewerUsername, "REVIEWER")) {
                JOptionPane.showMessageDialog(this, 
                    "This user does not exist or is not a reviewer", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int weightage = (Integer) weightageSpinner.getValue();
            MemoryStorage.addTrustedReviewer(currentUser, reviewerUsername, weightage);
            loadTrustedReviewers();
            reviewerField.setText("");
        });

        removeButton.addActionListener(e -> {
            int selectedRow = reviewersTable.getSelectedRow();
            if (selectedRow >= 0) {
                String reviewer = (String) tableModel.getValueAt(selectedRow, 0);
                MemoryStorage.removeTrustedReviewer(currentUser, reviewer);
                loadTrustedReviewers();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a reviewer to remove", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dispose());

        // Add components to dialog
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);

        // Load data and show dialog
        setMinimumSize(new Dimension(500, 400));
        loadTrustedReviewers();
        pack();
    }

    private void loadTrustedReviewers() {
        tableModel.setRowCount(0);
        List<TrustedReviewer> trustedReviewers = MemoryStorage.getTrustedReviewersForStudent(currentUser);

        for (TrustedReviewer tr : trustedReviewers) {
            Object[] row = {tr.getReviewerUsername(), tr.getWeightage()};
            tableModel.addRow(row);
        }
    }

    public static void showDialog(Frame owner, String currentUser) {
        TrustedReviewerDialog dialog = new TrustedReviewerDialog(owner, currentUser);
        dialog.setVisible(true);
    }
}