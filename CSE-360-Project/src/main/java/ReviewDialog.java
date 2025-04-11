import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for creating and editing reviews for questions and answers
 */
public class ReviewDialog extends JDialog {
    private final int contentId;
    private final String contentType;
    private final String currentUser;
    private final Review existingReview;
    private final JTextArea contentArea;
    private final JComboBox<Integer> ratingCombo;
    private final Runnable onComplete;

    /**
     * Constructor for creating a new review
     */
    public ReviewDialog(Frame owner, int contentId, String contentType, String content,
                       String currentUser, Runnable onComplete) {
        this(owner, contentId, contentType, content, currentUser, null, onComplete);
    }

    /**
     * Constructor for editing an existing review
     */
    public ReviewDialog(Frame owner, int contentId, String contentType, String content,
                       String currentUser, Review existingReview, Runnable onComplete) {
        super(owner, existingReview == null ? "Create Review" : "Edit Review", true);
        this.contentId = contentId;
        this.contentType = contentType;
        this.currentUser = currentUser;
        this.existingReview = existingReview;
        this.onComplete = onComplete;

        setSize(600, 500);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content being reviewed
        JTextArea originalContent = new JTextArea(content);
        originalContent.setEditable(false);
        originalContent.setLineWrap(true);
        originalContent.setWrapStyleWord(true);
        JScrollPane originalScrollPane = new JScrollPane(originalContent);
        originalScrollPane.setBorder(BorderFactory.createTitledBorder(
            "Original " + (contentType.equals("QUESTION") ? "Question" : "Answer")));
        originalScrollPane.setPreferredSize(new Dimension(580, 150));

        // Review content
        contentArea = new JTextArea(existingReview != null ? existingReview.getContent() : "");
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane reviewScrollPane = new JScrollPane(contentArea);
        reviewScrollPane.setBorder(BorderFactory.createTitledBorder("Your Review"));
        reviewScrollPane.setPreferredSize(new Dimension(580, 200));

        // Rating
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.add(new JLabel("Rating: "));

        Integer[] ratings = {1, 2, 3, 4, 5};
        ratingCombo = new JComboBox<>(ratings);
        if (existingReview != null) {
            ratingCombo.setSelectedItem(existingReview.getRating());
        }
        ratingPanel.add(ratingCombo);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton(existingReview == null ? "Submit Review" : "Update Review");
        JButton cancelButton = new JButton("Cancel");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contentArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ReviewDialog.this,
                        "Please enter a review", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int rating = (Integer) ratingCombo.getSelectedItem();

                if (existingReview == null) {
                    // Create new review
                    MemoryStorage.addReview(contentId, contentType, currentUser,
                                         contentArea.getText().trim(), rating);
                } else {
                    // Update existing review
                    MemoryStorage.updateReview(existingReview.getId(),
                                            contentArea.getText().trim(), rating);
                }

                dispose();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        mainPanel.add(originalScrollPane, BorderLayout.NORTH);
        mainPanel.add(reviewScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(ratingPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    /**
     * Static method to show review dialog with proper role checking
     */
    public static void showReviewDialog(Frame owner, int contentId, String contentType, 
                                      String content, String currentUser, Runnable onComplete) {
        // Check if user has reviewer role
        if (!MemoryStorage.hasRole(currentUser, "REVIEWER")) {
            JOptionPane.showMessageDialog(owner,
                "You need reviewer privileges to create reviews.",
                "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReviewDialog dialog = new ReviewDialog(owner, contentId, contentType, content, currentUser, onComplete);
        dialog.setVisible(true);
    }

    /**
     * Static method to show edit dialog with proper permission checking
     */
    public static void showEditDialog(Frame owner, Review review, String content,
                                    String currentUser, Runnable onComplete) {
        // Check if user is the author of the review
        if (!review.getReviewerUsername().equals(currentUser)) {
            JOptionPane.showMessageDialog(owner,
                "You can only edit your own reviews.",
                "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReviewDialog dialog = new ReviewDialog(owner, review.getContentId(), 
                                            review.getContentType(), content, 
                                            currentUser, review, onComplete);
        dialog.setVisible(true);
    }

    /**
     * Static method to handle review deletion with permission checking
     */
    public static void handleDelete(Component parent, Review review, String currentUser, Runnable onDelete) {
        if (!review.getReviewerUsername().equals(currentUser)) {
            JOptionPane.showMessageDialog(parent,
                "You can only delete your own reviews.",
                "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to delete this review?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            MemoryStorage.deleteReview(review.getId());
            if (onDelete != null) {
                onDelete.run();
            }
        }
    }
}