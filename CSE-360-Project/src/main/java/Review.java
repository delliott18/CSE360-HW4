import java.time.LocalDateTime;

public class Review {
    private int id;
    private int contentId;          // ID of question or answer being reviewed
    private String contentType;     // "QUESTION" or "ANSWER"
    private String reviewerUsername;
    private String content;
    private int rating;             // 1-5 stars
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review(int id, int contentId, String contentType, String reviewerUsername, 
                 String content, int rating, LocalDateTime createdAt) {
        this.id = id;
        this.contentId = contentId;
        this.contentType = contentType;
        this.reviewerUsername = reviewerUsername;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContentId() { return contentId; }
    public void setContentId(int contentId) { this.contentId = contentId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}