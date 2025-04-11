import javax.swing.*;
import java.awt.*;

/**
 * Dialog for students to request reviewer status
 */
public class ReviewerRequestDialog extends JDialog {
    private final String currentUser;
    private final JTextArea reasonArea;

    /**
     * Constructor for reviewer request dialog
     */
    public ReviewerRequestDialog(Frame owner, String currentUser) {
        super(owner, "Request Reviewer Status", true);
        this.currentUser = currentUser;

        setSize(500, 300);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Instruction label
        JLabel instructionLabel = new JLabel(
            "<html>Please explain why you would like to become a reviewer.<br>" +
            "Instructors will review your questions and answers to determine eligibility.</html>");
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Reason text area
        reasonArea = new JTextArea();
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Reason for Request"));
        scrollPane.setPreferredSize(new Dimension(480, 150));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit Request");
        JButton cancelButton = new JButton("Cancel");

        submitButton.addActionListener(e -> {
            if (reasonArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please provide a reason for your request", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Pass the reason to the storage
            int requestId = MemoryStorage.addReviewerRequest(currentUser, reasonArea.getText().trim());
            dispose();
            JOptionPane.showMessageDialog(getOwner(), 
                "Your request has been submitted and is pending review by an instructor.", 
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(instructionLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        setMinimumSize(new Dimension(500, 300));
        pack();
    }

    /**
     * Static method to show the reviewer request dialog
     */
    public static void showDialog(Frame owner, String currentUser) {
        // Check if user already has reviewer role
        if (MemoryStorage.hasRole(currentUser, "REVIEWER")) {
            JOptionPane.showMessageDialog(owner, 
                "You already have reviewer privileges.", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Check if there's already a pending request
        boolean hasPendingRequest = MemoryStorage.getPendingReviewerRequests().stream()
            .anyMatch(rr -> rr.getStudentUsername().equals(currentUser));

        if (hasPendingRequest) {
            JOptionPane.showMessageDialog(owner, 
                "You already have a pending reviewer request.", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ReviewerRequestDialog dialog = new ReviewerRequestDialog(owner, currentUser);
        dialog.setVisible(true);
    }
}