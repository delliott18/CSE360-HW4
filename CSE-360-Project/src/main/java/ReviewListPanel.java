import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for displaying reviews with filtering options
 */
public class ReviewListPanel extends JPanel {
    private final String currentUser;
    private final int contentId;
    private final String contentType;
    private final JPanel reviewsPanel;
    private final JCheckBox trustedOnlyCheckbox;

    /**
     * Constructor for review list panel
     */
    public ReviewListPanel(String currentUser, int contentId, String contentType) {
        this.currentUser = currentUser;
        this.contentId = contentId;
        this.contentType = contentType;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trustedOnlyCheckbox = new JCheckBox("Show Only Trusted Reviewers");
        JButton refreshButton = new JButton("Refresh");

        controlPanel.add(trustedOnlyCheckbox);
        controlPanel.add(refreshButton);

        // Reviews panel with scroll
        reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(reviewsPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Reviews"));
        scrollPane.setPreferredSize(new Dimension(580, 300));

        // Add listeners
        trustedOnlyCheckbox.addActionListener(e -> loadReviews());
        refreshButton.addActionListener(e -> loadReviews());

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load reviews
        loadReviews();
    }

    /**
     * Load reviews into the panel
     */
    public void loadReviews() {
        reviewsPanel.removeAll();

        List<Review> reviews;
        if (trustedOnlyCheckbox.isSelected()) {
            reviews = MemoryStorage.getReviewsForContentFromTrustedReviewers(
                contentId, contentType, currentUser);
        } else {
            reviews = MemoryStorage.getReviewsForContent(contentId, contentType);
        }

        // Always ensure a layout for content
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));

        if (reviews.isEmpty()) {
            JLabel noReviewsLabel = new JLabel("No reviews available");
            noReviewsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noReviewsLabel.setFont(noReviewsLabel.getFont().deriveFont(Font.BOLD, 14f));
            JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            wrapperPanel.add(noReviewsLabel);
            reviewsPanel.add(wrapperPanel);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Review review : reviews) {
                JPanel reviewPanel = new JPanel(new BorderLayout(5, 5));
                reviewPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createEtchedBorder()
                ));

                // Header with reviewer info and rating
                JPanel headerPanel = new JPanel(new BorderLayout());

                JPanel reviewerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                reviewerPanel.add(new JLabel("Reviewer: " + review.getReviewerUsername()));

                // Check if this reviewer is trusted
                List<TrustedReviewer> trusted = MemoryStorage.getTrustedReviewersForStudent(currentUser);
                boolean isTrusted = trusted.stream()
                    .anyMatch(tr -> tr.getReviewerUsername().equals(review.getReviewerUsername()));

                if (isTrusted) {
                    int weightage = trusted.stream()
                        .filter(tr -> tr.getReviewerUsername().equals(review.getReviewerUsername()))
                        .findFirst()
                        .map(TrustedReviewer::getWeightage)
                        .orElse(0);

                    JLabel trustedLabel = new JLabel(" (Trusted: " + weightage + ")");
                    trustedLabel.setForeground(new Color(0, 120, 0));
                    reviewerPanel.add(trustedLabel);
                }

                reviewerPanel.add(new JLabel(" | " + review.getCreatedAt().format(formatter)));

                JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                ratingPanel.add(new JLabel("Rating: " + review.getRating() + "/5"));

                headerPanel.add(reviewerPanel, BorderLayout.WEST);
                headerPanel.add(ratingPanel, BorderLayout.EAST);

                // Review content
                JTextArea contentArea = new JTextArea(review.getContent());
                contentArea.setEditable(false);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);
                contentArea.setBackground(new Color(250, 250, 250));
                contentArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JScrollPane contentScroll = new JScrollPane(contentArea);
                contentScroll.setPreferredSize(new Dimension(550, 100));

                // Button panel
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                // Message button
                JButton messageButton = new JButton("Message Reviewer");
                messageButton.addActionListener(e -> {
                    ReviewMessageDialog.showMessageDialogForReview(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        review,
                        currentUser,
                        this::loadReviews
                    );
                });
                buttonPanel.add(messageButton);

                // Edit/delete buttons if current user is the reviewer
                if (review.getReviewerUsername().equals(currentUser)) {
                    JButton editButton = new JButton("Edit");
                    JButton deleteButton = new JButton("Delete");

                    editButton.addActionListener(e -> {
                        // Get content for this review
                        String content = getContentText(review.getContentId(), review.getContentType());

                        ReviewDialog.showEditDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this),
                            review,
                            content,
                            currentUser,
                            this::loadReviews
                        );
                    });

                    deleteButton.addActionListener(e -> {
                        ReviewDialog.handleDelete(this, review, currentUser, this::loadReviews);
                    });

                    buttonPanel.add(editButton);
                    buttonPanel.add(deleteButton);
                }

                // Trust controls if current user is not the reviewer
                if (!review.getReviewerUsername().equals(currentUser)) {
                    JButton trustButton = new JButton(isTrusted ? "Adjust Trust" : "Trust Reviewer");
                    trustButton.addActionListener(e -> {
                        String reviewer = review.getReviewerUsername();
                        int currentWeightage = 5;

                        if (isTrusted) {
                            currentWeightage = trusted.stream()
                                .filter(tr -> tr.getReviewerUsername().equals(reviewer))
                                .findFirst()
                                .map(TrustedReviewer::getWeightage)
                                .orElse(5);
                        }

                        SpinnerNumberModel model = new SpinnerNumberModel(currentWeightage, 1, 10, 1);

                        JSpinner spinner = new JSpinner(model);
                        int option = JOptionPane.showConfirmDialog(
                            this,
                            spinner,
                            "Set Trust Weightage for " + reviewer,
                            JOptionPane.OK_CANCEL_OPTION
                        );

                        if (option == JOptionPane.OK_OPTION) {
                            int newWeightage = (Integer) spinner.getValue();
                            MemoryStorage.addTrustedReviewer(currentUser, reviewer, newWeightage);
                            loadReviews();
                        }
                    });

                    buttonPanel.add(trustButton);
                }

                // Add components to review panel
                reviewPanel.add(headerPanel, BorderLayout.NORTH);
                reviewPanel.add(contentScroll, BorderLayout.CENTER);
                reviewPanel.add(buttonPanel, BorderLayout.SOUTH);

                // Add to main panel
                reviewsPanel.add(reviewPanel);
                reviewsPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Force proper size and layout update
        reviewsPanel.revalidate();
        reviewsPanel.repaint();
    }

    /**
     * Helper method to get content text for a question or answer
     */
    private String getContentText(int contentId, String contentType) {
        if (contentType.equals("QUESTION")) {
            return MemoryStorage.getQuestion(contentId)
                .map(Question::getContent)
                .orElse("");
        } else if (contentType.equals("ANSWER")) {
            return MemoryStorage.getAnswers().stream()
                .filter(a -> a.getId() == contentId)
                .findFirst()
                .map(Answer::getContent)
                .orElse("");
        }
        return "";
    }
}