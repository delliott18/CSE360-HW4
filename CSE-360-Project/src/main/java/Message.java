import java.time.LocalDateTime;

public class Message {
    private int id;
    private String fromUser;
    private String toUser;
    private String subject;
    private String content;
    private LocalDateTime sentDate;
    private boolean isRead;
    private int relatedReviewId;  // If this message is related to a review

    public Message(int id, String fromUser, String toUser, String subject, 
                  String content, LocalDateTime sentDate, boolean isRead, int relatedReviewId) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.subject = subject;
        this.content = content;
        this.sentDate = sentDate;
        this.isRead = isRead;
        this.relatedReviewId = relatedReviewId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getRelatedReviewId() { return relatedReviewId; }
    public void setRelatedReviewId(int relatedReviewId) { this.relatedReviewId = relatedReviewId; }
}