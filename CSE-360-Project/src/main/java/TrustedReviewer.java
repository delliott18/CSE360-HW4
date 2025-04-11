public class TrustedReviewer {
    private String studentUsername;
    private String reviewerUsername;
    private int weightage;      // Higher = more trusted/prioritized

    public TrustedReviewer(String studentUsername, String reviewerUsername, int weightage) {
        this.studentUsername = studentUsername;
        this.reviewerUsername = reviewerUsername;
        this.weightage = weightage;
    }

    // Getters and setters
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }

    public int getWeightage() { return weightage; }
    public void setWeightage(int weightage) { this.weightage = weightage; }
}