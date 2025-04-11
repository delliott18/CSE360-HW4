import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for sending messages related to reviews
 */
public class ReviewMessageDialog extends JDialog {
    private final String fromUser;
    private final String toUser;
    private final int relatedReviewId;
    private final JTextField subjectField;
    private final JTextArea contentArea;
    private final Runnable onComplete;

    /**
     * Constructor for message dialog
     */
    public ReviewMessageDialog(Frame owner, String fromUser, String toUser, 
                              int relatedReviewId, Runnable onComplete) {
        super(owner, "Send Message", true);
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.relatedReviewId = relatedReviewId;
        this.onComplete = onComplete;

        setSize(500, 400);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // To field (read-only)
        JPanel headerPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        headerPanel.add(new JLabel("To:"));
        headerPanel.add(new JLabel(toUser));

        // Subject field
        headerPanel.add(new JLabel("Subject:"));
        subjectField = new JTextField();
        headerPanel.add(subjectField);

        // Message content
        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setBorder(BorderFactory.createTitledBorder("Message"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send");
        JButton cancelButton = new JButton("Cancel");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (subjectField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ReviewMessageDialog.this,
                        "Please enter a subject", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (contentArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(ReviewMessageDialog.this,
                        "Please enter a message", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MemoryStorage.addMessage(fromUser, toUser, subjectField.getText().trim(),
                                      contentArea.getText().trim(), relatedReviewId);

                dispose();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    /**
     * Static method to show message dialog for a review
     */
    public static void showMessageDialogForReview(Frame owner, Review review, 
                                               String currentUser, Runnable onComplete) {
        // Message goes to the reviewer if student is sending, or to author if reviewer is sending
        String toUser = review.getReviewerUsername().equals(currentUser) 
            ? getContentAuthor(review.getContentId(), review.getContentType())
            : review.getReviewerUsername();

        ReviewMessageDialog dialog = new ReviewMessageDialog(owner, currentUser, toUser, 
                                                          review.getId(), onComplete);
        dialog.setVisible(true);
    }

    /**
     * Helper method to get the author of a question or answer
     */
    private static String getContentAuthor(int contentId, String contentType) {
        if (contentType.equals("QUESTION")) {
            return MemoryStorage.getQuestion(contentId)
                .map(Question::getAuthor)
                .orElse("");
        } else if (contentType.equals("ANSWER")) {
            return MemoryStorage.getAnswers().stream()
                .filter(a -> a.getId() == contentId)
                .findFirst()
                .map(Answer::getAuthor)
                .orElse("");
        }
        return "";
    }
}