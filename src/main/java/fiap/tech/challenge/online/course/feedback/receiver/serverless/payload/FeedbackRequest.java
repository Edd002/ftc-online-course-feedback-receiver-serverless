package fiap.tech.challenge.online.course.feedback.receiver.serverless.payload;

public record FeedbackRequest(UserTypeRequest userType, String email, String accessKey, Boolean urgent, String description, String comment) {
}