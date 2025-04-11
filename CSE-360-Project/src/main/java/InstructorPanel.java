import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Panel for instructor functionality, including reviewer request approval
 */
public class InstructorPanel extends JPanel {
    private final String currentUser;
    private DefaultTableModel requestsTableModel;
    private JTable requestsTable;

    private DefaultTableModel studentContentTableModel;
    private JTable studentContentTable;

    /**
     * Constructor for instructor panel
     */
    public InstructorPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setupUI();
        loadPendingRequests();
    }

    private void setupUI() {
        // Create tabbed pane for different instructor functions
        JTabbedPane tabbedPane = new JTabbedPane();

        // Reviewer Request Panel
        JPanel requestsPanel = createRequestsPanel();
        tabbedPane.addTab("Reviewer Requests", requestsPanel);

        // Student Content Review Panel
        JPanel studentContentPanel = createStudentContentPanel();
        tabbedPane.addTab("Student Content", studentContentPanel);
        tabbedPane.addTab("Flagged Items", new FlaggedItemsPanel(currentUser));
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Create table for requests
        String[] columns = {"Student", "Request Date", "Status"};
        requestsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestsTable = new JTable(requestsTableModel);
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pending Reviewer Requests"));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewRequestButton = new JButton("View Request Details");
        JButton viewStudentButton = new JButton("View Student Content");
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(viewRequestButton);
        buttonPanel.add(viewStudentButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        // Add listeners
        viewRequestButton.addActionListener(e -> viewSelectedRequest());

        viewStudentButton.addActionListener(e -> {
            int selectedRow = requestsTable.getSelectedRow();
            if (selectedRow >= 0) {
                String student = (String) requestsTableModel.getValueAt(selectedRow, 0);
                loadStudentContent(student);
                // Switch to student content tab
                ((JTabbedPane) panel.getParent()).setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a request to view student content", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        approveButton.addActionListener(e -> {
            handleRequestAction(true);
        });

        rejectButton.addActionListener(e -> {
            handleRequestAction(false);
        });

        refreshButton.addActionListener(e -> {
            loadPendingRequests();
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void viewSelectedRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String studentUsername = (String) requestsTableModel.getValueAt(selectedRow, 0);

            // Find the request
            Optional<ReviewerRequest> request = MemoryStorage.getPendingReviewerRequests().stream()
                .filter(r -> r.getStudentUsername().equals(studentUsername))
                .findFirst();

            if (request.isPresent()) {
                JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
                                   "Request Details", true);
                dialog.setSize(500, 350);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Request info
                JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                infoPanel.add(new JLabel("Student:"));
                infoPanel.add(new JLabel(request.get().getStudentUsername()));
                infoPanel.add(new JLabel("Date:"));
                infoPanel.add(new JLabel(request.get().getRequestDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

                // Reason
                JTextArea reasonArea = new JTextArea(request.get().getReason());
                reasonArea.setEditable(false);
                reasonArea.setLineWrap(true);
                reasonArea.setWrapStyleWord(true);
                JScrollPane scrollPane = new JScrollPane(reasonArea);
                scrollPane.setBorder(BorderFactory.createTitledBorder("Request Reason"));

                // Buttons
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton approveButton = new JButton("Approve");
                JButton rejectButton = new JButton("Reject");
                JButton closeButton = new JButton("Close");

                approveButton.addActionListener(e -> {
                    MemoryStorage.approveReviewerRequest(request.get().getId(), currentUser);
                    dialog.dispose();
                    loadPendingRequests();
                });

                rejectButton.addActionListener(e -> {
                    MemoryStorage.rejectReviewerRequest(request.get().getId(), currentUser);
                    dialog.dispose();
                    loadPendingRequests();
                });

                closeButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(approveButton);
                buttonPanel.add(rejectButton);
                buttonPanel.add(closeButton);

                panel.add(infoPanel, BorderLayout.NORTH);
                panel.add(scrollPane, BorderLayout.CENTER);
                panel.add(buttonPanel, BorderLayout.SOUTH);

                dialog.add(panel);
                dialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a request to view", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStudentContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Create panel for student information
        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel studentLabel = new JLabel("Student: ");
        JTextField studentField = new JTextField(15);
        studentField.setEditable(false);
        JButton loadButton = new JButton("Load Content");

        studentInfoPanel.add(studentLabel);
        studentInfoPanel.add(studentField);
        studentInfoPanel.add(loadButton);

        // Create table for student content
        String[] columns = {"Type", "Title/Content", "Date", "Status"};
        studentContentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentContentTable = new JTable(studentContentTableModel);
        JScrollPane scrollPane = new JScrollPane(studentContentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student's Questions and Answers"));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewContentButton = new JButton("View Full Content");
        JButton approveRequestButton = new JButton("Approve Reviewer Request");
        JButton rejectRequestButton = new JButton("Reject Reviewer Request");

        buttonPanel.add(viewContentButton);
        buttonPanel.add(approveRequestButton);
        buttonPanel.add(rejectRequestButton);

        // Add listeners
        loadButton.addActionListener(e -> {
            String student = studentField.getText().trim();
            if (!student.isEmpty()) {
                loadStudentContent(student);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No student selected", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewContentButton.addActionListener(e -> {
            int selectedRow = studentContentTable.getSelectedRow();
            if (selectedRow >= 0) {
                String type = (String) studentContentTableModel.getValueAt(selectedRow, 0);
                String content = (String) studentContentTableModel.getValueAt(selectedRow, 1);

                JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
                                           "View " + type, true);
                dialog.setSize(500, 300);
                dialog.setLocationRelativeTo(this);

                JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
                JTextArea contentArea = new JTextArea(content);
                contentArea.setEditable(false);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);

                JScrollPane contentScroll = new JScrollPane(contentArea);
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(evt -> dialog.dispose());

                dialogPanel.add(contentScroll, BorderLayout.CENTER);
                dialogPanel.add(closeButton, BorderLayout.SOUTH);
                dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                dialog.add(dialogPanel);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an item to view", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        approveRequestButton.addActionListener(e -> {
            String student = studentField.getText().trim();
            if (!student.isEmpty()) {
                // Find pending request for this student
                List<ReviewerRequest> requests = MemoryStorage.getPendingReviewerRequests();
                Optional<ReviewerRequest> request = requests.stream()
                    .filter(rr -> rr.getStudentUsername().equals(student))
                    .findFirst();

                if (request.isPresent()) {
                    MemoryStorage.approveReviewerRequest(request.get().getId(), currentUser);
                    JOptionPane.showMessageDialog(this, 
                        "Reviewer request approved for " + student, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPendingRequests();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No pending request found for this student", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No student selected", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        rejectRequestButton.addActionListener(e -> {
            String student = studentField.getText().trim();
            if (!student.isEmpty()) {
                // Find pending request for this student
                List<ReviewerRequest> requests = MemoryStorage.getPendingReviewerRequests();
                Optional<ReviewerRequest> request = requests.stream()
                    .filter(rr -> rr.getStudentUsername().equals(student))
                    .findFirst();

                if (request.isPresent()) {
                    MemoryStorage.rejectReviewerRequest(request.get().getId(), currentUser);
                    JOptionPane.showMessageDialog(this, 
                        "Reviewer request rejected for " + student, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPendingRequests();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No pending request found for this student", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No student selected", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(studentInfoPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPendingRequests() {
        requestsTableModel.setRowCount(0);
        List<ReviewerRequest> requests = MemoryStorage.getPendingReviewerRequests();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (ReviewerRequest request : requests) {
            Object[] row = {
                request.getStudentUsername(),
                request.getRequestDate().format(formatter),
                request.getStatus()
            };
            requestsTableModel.addRow(row);
        }
    }

    private void loadStudentContent(String student) {
        studentContentTableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Load student's questions
        List<Question> questions = MemoryStorage.getQuestions().stream()
            .filter(q -> q.getAuthor().equals(student))
            .toList();

        for (Question question : questions) {
            Object[] row = {
                "Question",
                question.getTitle() + ": " + question.getContent(),
                question.getCreatedAt().format(formatter),
                question.getStatus()
            };
            studentContentTableModel.addRow(row);
        }

        // Load student's answers
        List<Answer> answers = MemoryStorage.getAnswers().stream()
            .filter(a -> a.getAuthor().equals(student))
            .toList();

        for (Answer answer : answers) {
            Object[] row = {
                "Answer",
                answer.getContent(),
                answer.getCreatedAt().format(formatter),
                answer.isAccepted() ? "Accepted" : "Submitted"
            };
            studentContentTableModel.addRow(row);
        }

        // Update student field in the student content panel
        JTextField studentField = getStudentField();
        if (studentField != null) {
            studentField.setText(student);
        }
    }

    private JTextField getStudentField() {
        Component comp = ((JPanel)((JTabbedPane)getComponent(0)).getComponentAt(1)).getComponent(0);
        if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            for (Component c : panel.getComponents()) {
                if (c instanceof JTextField) {
                    return (JTextField) c;
                }
            }
        }
        return null;
    }

    private void handleRequestAction(boolean approve) {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String student = (String) requestsTableModel.getValueAt(selectedRow, 0);

            // Find request ID
            List<ReviewerRequest> requests = MemoryStorage.getPendingReviewerRequests();
            Optional<ReviewerRequest> request = requests.stream()
                .filter(rr -> rr.getStudentUsername().equals(student))
                .findFirst();

            if (request.isPresent()) {
                if (approve) {
                    MemoryStorage.approveReviewerRequest(request.get().getId(), currentUser);
                    JOptionPane.showMessageDialog(this, 
                        "Reviewer request approved for " + student, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    MemoryStorage.rejectReviewerRequest(request.get().getId(), currentUser);
                    JOptionPane.showMessageDialog(this, 
                        "Reviewer request rejected for " + student, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadPendingRequests();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a request", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}