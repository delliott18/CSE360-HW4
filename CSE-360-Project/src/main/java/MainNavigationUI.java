import javax.swing.*;
import java.awt.*;

public class MainNavigationUI extends JFrame {
    private final String currentUser;
    private final String sessionToken;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public MainNavigationUI(String username, String sessionToken) {
        this.currentUser = username;
        this.sessionToken = sessionToken;

        // Initialize user roles if needed
        MemoryStorage.initializeRoles();
        checkFirstTimeSetup();

        setTitle("Email and Forum System - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create top panel for navigation and user info
        JPanel topPanel = new JPanel(new BorderLayout());

        // Create navigation toolbar
        JToolBar navBar = new JToolBar();
        navBar.setFloatable(false);

        JButton emailButton = new JButton("Email");
        JButton forumButton = new JButton("Forum");
        JButton instructorButton = new JButton("Instructor Panel");

        // Make instructor button only visible to users with instructor role
        instructorButton.setVisible(MemoryStorage.hasRole(username, "INSTRUCTOR"));

        navBar.add(emailButton);
        navBar.addSeparator();
        navBar.add(forumButton);
        navBar.addSeparator();
        navBar.add(instructorButton);

        // Create user panel with logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + username);
        JButton logoutButton = new JButton("Logout");
        userPanel.add(userLabel);
        userPanel.add(logoutButton);

        topPanel.add(navBar, BorderLayout.WEST);
        topPanel.add(userPanel, BorderLayout.EAST);

        // Main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize interfaces
        EmailInterface emailInterface = new EmailInterface(username, sessionToken);
        ForumInterface forumInterface = new ForumInterface(username, sessionToken);
        InstructorPanel instructorPanel = new InstructorPanel(username);

        // Add interfaces to card layout
        mainPanel.add(emailInterface, "EMAIL");
        mainPanel.add(forumInterface, "FORUM");
        mainPanel.add(instructorPanel, "INSTRUCTOR");

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Add button listeners
        emailButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "EMAIL");
            setTitle("Email System - " + username);
        });

        forumButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "FORUM");
            setTitle("Forum System - " + username);
        });

        instructorButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "INSTRUCTOR");
            setTitle("Instructor Panel - " + username);
        });

        logoutButton.addActionListener(e -> {
            // Handle logout
            MemoryStorage.logout(sessionToken);
            dispose(); // Close current window

            // Show login screen
            SwingUtilities.invokeLater(() -> {
                EmailLoginUI loginUI = new EmailLoginUI();
                loginUI.setVisible(true);
            });
        });

        // Show email interface by default
        cardLayout.show(mainPanel, "EMAIL");
    }

    // Helper method to check if user should see the system initialization UI
    private void checkFirstTimeSetup() {
        if (MemoryStorage.getUserRoles(currentUser).isEmpty()) {
            // First time login, assign default roles
            MemoryStorage.addUserRole(currentUser, "STUDENT");

            // If this is the first user, make them an instructor
            if (!MemoryStorage.userExists("admin")) {
                if (currentUser.equals("admin")) {
                    // This is the admin account, assign instructor role
                    MemoryStorage.addUserRole(currentUser, "INSTRUCTOR");
                }
            }
        }
    }
}