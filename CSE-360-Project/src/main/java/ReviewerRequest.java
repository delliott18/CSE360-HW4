import java.time.LocalDateTime;

public class ReviewerRequest {
    private int id;
    private String studentUsername;
    private String reason;
    private String status;   // "PENDING", "APPROVED", "REJECTED"
    private String reviewedBy;  // Instructor who reviewed the request
    private LocalDateTime requestDate;
    private LocalDateTime reviewDate;

    public ReviewerRequest(int id, String studentUsername, String reason, LocalDateTime requestDate) {
        this.id = id;
        this.studentUsername = studentUsername;
        this.reason = reason;
        this.status = "PENDING";
        this.requestDate = requestDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
}