import java.util.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class MemoryStorage {
    private static final Map<String, String> users = new HashMap<>();  // username -> password
    private static final Map<String, String> sessions = new HashMap<>();  // token -> username
    private static final Map<String, Integer> loginAttempts = new HashMap<>();  // username -> attempts
    private static final Map<String, LocalDateTime> lockouts = new HashMap<>();  // username -> lockout time

    private static final List<Email> emails = new ArrayList<>();
    private static final List<Question> questions = new ArrayList<>();
    private static final List<Answer> answers = new ArrayList<>();
    private static final List<Review> reviews = new ArrayList<>();
    private static final List<Message> messages = new ArrayList<>();
    private static final List<TrustedReviewer> trustedReviewers = new ArrayList<>();
    private static final List<ReviewerRequest> reviewerRequests = new ArrayList<>();
    private static final List<UserRole> userRoles = new ArrayList<>();
    private static final List<Flag> flags = new ArrayList<>();

    private static int nextEmailId = 1;
    private static int nextQuestionId = 1;
    private static int nextAnswerId = 1;
    private static int nextReviewId = 1;
    private static int nextMessageId = 1;
    private static int nextReviewerRequestId = 1;
    private static int nextFlagId = 1;


    // Static initializer to create admin account on startup
    static {
        // Pre-register admin account if it doesn't exist
        if (!users.containsKey("admin")) {
            users.put("admin", "adminPass123!");
            addUserRole("admin", "INSTRUCTOR");
            addUserRole("admin", "STUDENT");
            addUserRole("admin", "STAFF");
            addSampleEmails("admin");
        }
    }

    // User Management
    public static void addUser(String username, String password) {
        users.put(username, password);
        addSampleEmails(username);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static String getPassword(String username) {
        return users.get(username);
    }

    public static List<String> getUsernames() {
        return new ArrayList<>(users.keySet());
    }

    // Session Management
    public static String createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }

    public static void logout(String token) {
        sessions.remove(token);
    }

    public static String getUserFromSession(String token) {
        return sessions.get(token);
    }

    // Login Attempt Management
    public static void incrementLoginAttempts(String username) {
        loginAttempts.put(username, loginAttempts.getOrDefault(username, 0) + 1);
        if (loginAttempts.get(username) >= 5) {
            lockouts.put(username, LocalDateTime.now().plusMinutes(15));
        }
    }

    public static void resetLoginAttempts(String username) {
        loginAttempts.remove(username);
        lockouts.remove(username);
    }

    public static boolean isLockedOut(String username) {
        LocalDateTime lockoutTime = lockouts.get(username);
        return lockoutTime != null && lockoutTime.isAfter(LocalDateTime.now());
    }

    public static long getLockoutTimeRemaining(String username) {
        LocalDateTime lockoutTime = lockouts.get(username);
        if (lockoutTime == null || lockoutTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), lockoutTime);
    }

    // Email Management
    public static void addSampleEmails(String username) {
        addEmail("system@forum.com", username, "Welcome to the Forum", 
            "Welcome to our forum system! Feel free to ask questions and help others.");
    }

    public static int addEmail(String fromUser, String toUser, String subject, String content) {
        Email email = new Email();
        email.setId(nextEmailId++);
        email.setFromUser(fromUser);
        email.setToUser(toUser);
        email.setSubject(subject);
        email.setContent(content);
        email.setSentDate(LocalDateTime.now());
        email.setStatus(Email.EmailStatus.SENT);
        email.setRead(false);
        emails.add(email);
        return email.getId();
    }

    public static void saveDraft(String fromUser, String toUser, String subject, String content) {
        Email email = new Email();
        email.setId(nextEmailId++);
        email.setFromUser(fromUser);
        email.setToUser(toUser != null ? toUser : "");
        email.setSubject(subject != null ? subject : "");
        email.setContent(content != null ? content : "");
        email.setSentDate(LocalDateTime.now());
        email.setStatus(Email.EmailStatus.DRAFT);
        email.setRead(false);
        emails.add(email);
    }

    public static void sendDraft(int emailId) {
        emails.stream()
            .filter(e -> e.getId() == emailId && e.getStatus() == Email.EmailStatus.DRAFT)
            .findFirst()
            .ifPresent(e -> {
                // Only send if recipient and subject are specified
                if (!e.getToUser().trim().isEmpty() && !e.getSubject().trim().isEmpty()) {
                    e.setStatus(Email.EmailStatus.SENT);
                    e.setSentDate(LocalDateTime.now());  // Update sent date to now
                }
            });
    }

    public static List<Email> getInboxEmails(String username) {
        return emails.stream()
            .filter(e -> e.getToUser().equals(username) && 
                        e.getStatus() != Email.EmailStatus.DELETED)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static List<Email> getSentEmails(String username) {
        return emails.stream()
            .filter(e -> e.getFromUser().equals(username) && 
                        e.getStatus() == Email.EmailStatus.SENT)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static List<Email> getDrafts(String username) {
        return emails.stream()
            .filter(e -> e.getFromUser().equals(username) && 
                        e.getStatus() == Email.EmailStatus.DRAFT)
            .sorted((e1, e2) -> e2.getSentDate().compareTo(e1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static void markAsRead(int emailId, String username) {
        emails.stream()
            .filter(e -> e.getId() == emailId && e.getToUser().equals(username))
            .findFirst()
            .ifPresent(e -> e.setRead(true));
    }

    public static void deleteEmail(int emailId, String username) {
        emails.stream()
            .filter(e -> e.getId() == emailId && 
                        (e.getFromUser().equals(username) || e.getToUser().equals(username)))
            .findFirst()
            .ifPresent(e -> e.setStatus(Email.EmailStatus.DELETED));
    }

    // Question Management
    public static int addQuestion(String title, String content, String author) {
        Question question = new Question(
            nextQuestionId++,
            title,
            content,
            author,
            LocalDateTime.now(),
            "OPEN"
        );
        questions.add(question);
        return question.getId();
    }

    public static List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public static Optional<Question> getQuestion(int questionId) {
        return questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst();
    }

    public static void updateQuestion(int questionId, String title, String content) {
        questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst()
            .ifPresent(q -> {
                q.setTitle(title);
                q.setContent(content);
            });
    }

    public static void deleteQuestion(int questionId) {
        questions.removeIf(q -> q.getId() == questionId);
        // Also remove all associated answers
        answers.removeIf(a -> a.getQuestionId() == questionId);
    }

    public static List<Question> searchQuestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getQuestions();
        }

        String searchQuery = query.toLowerCase().trim();
        return questions.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(searchQuery) || 
                        q.getContent().toLowerCase().contains(searchQuery) ||
                        q.getAuthor().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());
    }

    // Answer Management
    public static int addAnswer(int questionId, String content, String author) {
        Answer answer = new Answer(
            nextAnswerId++,
            questionId,
            content,
            author,
            LocalDateTime.now(),
            false
        );
        answers.add(answer);

        // Update question status when first answer is added
        updateQuestionStatus(questionId);

        return answer.getId();
    }

    public static List<Answer> getAnswersForQuestion(int questionId) {
        return answers.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .sorted((a1, a2) -> {
                // Show accepted answer first, then sort by date
                if (a1.isAccepted() && !a2.isAccepted()) return -1;
                if (!a1.isAccepted() && a2.isAccepted()) return 1;
                return a2.getCreatedAt().compareTo(a1.getCreatedAt());
            })
            .collect(Collectors.toList());
    }

    public static List<Answer> getAnswers() {
        return new ArrayList<>(answers);
    }

    public static void updateAnswer(int answerId, String content) {
        answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst()
            .ifPresent(a -> a.setContent(content));
    }

    public static void deleteAnswer(int answerId) {
        // First find the associated question
        Optional<Answer> answer = answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst();

        if (answer.isPresent()) {
            int questionId = answer.get().getQuestionId();
            boolean wasAccepted = answer.get().isAccepted();

            // Remove the answer
            answers.removeIf(a -> a.getId() == answerId);

            // If this was the accepted answer or the last answer, update question status
            List<Answer> remainingAnswers = getAnswersForQuestion(questionId);
            if (remainingAnswers.isEmpty() || wasAccepted) {
                questions.stream()
                    .filter(q -> q.getId() == questionId)
                    .findFirst()
                    .ifPresent(q -> q.setStatus("OPEN"));
            }
        }
    }

    public static void acceptAnswer(int answerId, int questionId) {
        // First, unaccept any previously accepted answers
        answers.stream()
            .filter(a -> a.getQuestionId() == questionId)
            .forEach(a -> a.setAccepted(false));

        // Then accept the new answer
        answers.stream()
            .filter(a -> a.getId() == answerId)
            .findFirst()
            .ifPresent(a -> {
                a.setAccepted(true);
                // Update question status
                questions.stream()
                    .filter(q -> q.getId() == questionId)
                    .findFirst()
                    .ifPresent(q -> q.setStatus("ANSWERED"));
            });
    }

    private static void updateQuestionStatus(int questionId) {
        List<Answer> questionAnswers = getAnswersForQuestion(questionId);
        questions.stream()
            .filter(q -> q.getId() == questionId)
            .findFirst()
            .ifPresent(q -> {
                if (questionAnswers.isEmpty()) {
                    q.setStatus("OPEN");
                } else if (questionAnswers.stream().anyMatch(Answer::isAccepted)) {
                    q.setStatus("ANSWERED");
                } else {
                    q.setStatus("IN_PROGRESS");
                }
            });
    }

    // User Role Methods
    public static void addUserRole(String username, String role) {
        // Avoid duplicate roles
        if (!hasRole(username, role)) {
            userRoles.add(new UserRole(username, role));
        }
    }

    public static boolean hasRole(String username, String role) {
        return userRoles.stream()
            .anyMatch(ur -> ur.getUsername().equals(username) && ur.getRole().equals(role));
    }

    public static List<String> getUserRoles(String username) {
        return userRoles.stream()
            .filter(ur -> ur.getUsername().equals(username))
            .map(UserRole::getRole)
            .collect(Collectors.toList());
    }

    // Review Methods
    public static int addReview(int contentId, String contentType, String reviewerUsername, 
                          String content, int rating) {
        Review review = new Review(
            nextReviewId++,
            contentId,
            contentType,
            reviewerUsername,
            content,
            rating,
            LocalDateTime.now()
        );
        reviews.add(review);
        return review.getId();
    }

    public static List<Review> getReviewsByReviewer(String reviewerUsername) {
        return reviews.stream()
            .filter(r -> r.getReviewerUsername().equals(reviewerUsername))
            .collect(Collectors.toList());
    }

    public static List<Review> getReviewsForContent(int contentId, String contentType) {
        return reviews.stream()
            .filter(r -> r.getContentId() == contentId && r.getContentType().equals(contentType))
            .collect(Collectors.toList());
    }

    public static List<Review> getReviewsForContentFromTrustedReviewers(int contentId, String contentType, String studentUsername) {
        List<TrustedReviewer> trusted = getTrustedReviewersForStudent(studentUsername);

        // Get reviews from trusted reviewers and sort by weightage
        List<Review> trustedReviews = reviews.stream()
            .filter(r -> r.getContentId() == contentId && r.getContentType().equals(contentType))
            .filter(r -> trusted.stream().anyMatch(tr -> tr.getReviewerUsername().equals(r.getReviewerUsername())))
            .collect(Collectors.toList());

        // Sort reviews based on trust weightage (highest first)
        trustedReviews.sort((r1, r2) -> {
            int w1 = trusted.stream()
                .filter(tr -> tr.getReviewerUsername().equals(r1.getReviewerUsername()))
                .findFirst().map(TrustedReviewer::getWeightage).orElse(0);

            int w2 = trusted.stream()
                .filter(tr -> tr.getReviewerUsername().equals(r2.getReviewerUsername()))
                .findFirst().map(TrustedReviewer::getWeightage).orElse(0);

            return Integer.compare(w2, w1); // Descending order
        });

        return trustedReviews;
    }

    public static void updateReview(int reviewId, String content, int rating) {
        reviews.stream()
            .filter(r -> r.getId() == reviewId)
            .findFirst()
            .ifPresent(r -> {
                r.setContent(content);
                r.setRating(rating);
                r.setUpdatedAt(LocalDateTime.now());
            });
    }

    public static void deleteReview(int reviewId) {
        reviews.removeIf(r -> r.getId() == reviewId);
    }

    // Message Methods
    public static int addMessage(String fromUser, String toUser, String subject, 
                           String content, int relatedReviewId) {
        Message message = new Message(
            nextMessageId++,
            fromUser,
            toUser,
            subject,
            content,
            LocalDateTime.now(),
            false,
            relatedReviewId
        );
        messages.add(message);
        return message.getId();
    }

    public static List<Message> getMessagesForUser(String username) {
        return messages.stream()
            .filter(m -> m.getToUser().equals(username) || m.getFromUser().equals(username))
            .sorted((m1, m2) -> m2.getSentDate().compareTo(m1.getSentDate()))
            .collect(Collectors.toList());
    }

    public static List<Message> getMessagesRelatedToReview(int reviewId) {
        return messages.stream()
            .filter(m -> m.getRelatedReviewId() == reviewId)
            .sorted((m1, m2) -> m1.getSentDate().compareTo(m2.getSentDate()))
            .collect(Collectors.toList());
    }

    // Trusted Reviewer Methods
    public static void addTrustedReviewer(String studentUsername, String reviewerUsername, int weightage) {
        // Remove existing trusted relationship if exists
        trustedReviewers.removeIf(tr -> 
            tr.getStudentUsername().equals(studentUsername) && 
            tr.getReviewerUsername().equals(reviewerUsername));

        // Add new or updated relationship
        trustedReviewers.add(new TrustedReviewer(studentUsername, reviewerUsername, weightage));
    }

    public static void removeTrustedReviewer(String studentUsername, String reviewerUsername) {
        trustedReviewers.removeIf(tr -> 
            tr.getStudentUsername().equals(studentUsername) && 
            tr.getReviewerUsername().equals(reviewerUsername));
    }

    public static List<TrustedReviewer> getTrustedReviewersForStudent(String studentUsername) {
        return trustedReviewers.stream()
            .filter(tr -> tr.getStudentUsername().equals(studentUsername))
            .sorted((tr1, tr2) -> Integer.compare(tr2.getWeightage(), tr1.getWeightage())) // Highest first
            .collect(Collectors.toList());
    }

    public static void updateTrustedReviewerWeightage(String studentUsername, String reviewerUsername, int newWeightage) {
        trustedReviewers.stream()
            .filter(tr -> tr.getStudentUsername().equals(studentUsername) && 
                        tr.getReviewerUsername().equals(reviewerUsername))
            .findFirst()
            .ifPresent(tr -> tr.setWeightage(newWeightage));
    }

    // Reviewer Request Methods
    public static int addReviewerRequest(String studentUsername, String reason) {
        ReviewerRequest request = new ReviewerRequest(
            nextReviewerRequestId++,
            studentUsername,
            reason,
            LocalDateTime.now()
        );
        reviewerRequests.add(request);
        return request.getId();
    }

    public static List<ReviewerRequest> getPendingReviewerRequests() {
        return reviewerRequests.stream()
            .filter(rr -> rr.getStatus().equals("PENDING"))
            .collect(Collectors.toList());
    }

    public static void approveReviewerRequest(int requestId, String instructorUsername) {
        reviewerRequests.stream()
            .filter(rr -> rr.getId() == requestId)
            .findFirst()
            .ifPresent(rr -> {
                rr.setStatus("APPROVED");
                rr.setReviewedBy(instructorUsername);
                rr.setReviewDate(LocalDateTime.now());

                // Add reviewer role to the student
                String studentUsername = rr.getStudentUsername();
                addUserRole(studentUsername, "REVIEWER");
            });
    }

    public static void rejectReviewerRequest(int requestId, String instructorUsername) {
        reviewerRequests.stream()
            .filter(rr -> rr.getId() == requestId)
            .findFirst()
            .ifPresent(rr -> {
                rr.setStatus("REJECTED");
                rr.setReviewedBy(instructorUsername);
                rr.setReviewDate(LocalDateTime.now());
            });
    }

// Flag Methods

    public static int addFlag(String itemType, int itemId, String staffUsername) {
        if (flags.stream().anyMatch(f ->
                !f.isResolved() &&
                f.getItemType().equals(itemType) &&
                f.getItemId()   == itemId)) {
            return -1; // already flagged
        }
        Flag f = new Flag(nextFlagId++, itemType, itemId, staffUsername,
                        LocalDateTime.now());
        flags.add(f);
        return f.getId();
    }

    public static void setFlagNote(int flagId, String note) {
        flags.stream()
            .filter(f -> f.getId() == flagId && !f.isResolved())
            .findFirst()
            .ifPresent(f -> f.setNote(note));
    }

    public static void resolveFlag(int flagId, String resolverUsername) {
        flags.stream()
            .filter(f -> f.getId() == flagId && !f.isResolved())
            .findFirst()
            .ifPresent(f -> f.resolve(resolverUsername, LocalDateTime.now()));
    }

    public static Optional<Flag> getActiveFlag(String itemType, int itemId) {
        return flags.stream()
                    .filter(f -> !f.isResolved() &&
                                f.getItemType().equals(itemType) &&
                                f.getItemId() == itemId)
                    .findFirst();
    }

    public static List<Flag> getFlags(boolean includeResolved) {
        if (includeResolved) return new ArrayList<>(flags);
        return flags.stream()
                    .filter(f -> !f.isResolved())
                    .collect(Collectors.toList());
    }


    // System initialization
    public static void initializeRoles() {
        // Only create admin role if not already exists
        if (!hasRole("admin", "INSTRUCTOR")) {
            addUserRole("admin", "INSTRUCTOR");
        }
    }
}