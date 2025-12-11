package fiap.tech.challenge.online.course.feedback.receiver.serverless.email.payload;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.AssessmentType;

public record FeedbackReportResponse(Boolean urgent, String description, String comment, String administradorName, String administratorEmail, String teacherName, String teacherEmail, String studentName, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, String createdIn) {
}