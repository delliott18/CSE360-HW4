public class UserRole {
    private String username;
    private String role;  // "STUDENT", "REVIEWER", "INSTRUCTOR"

    public UserRole(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}