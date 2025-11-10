package fiap.tech.challenge.online.course.feedback.receiver.serverless.payload;

public record FeedbackResponse(Boolean urgent, String description, String comment, String studentName, String studentEmail) {
}