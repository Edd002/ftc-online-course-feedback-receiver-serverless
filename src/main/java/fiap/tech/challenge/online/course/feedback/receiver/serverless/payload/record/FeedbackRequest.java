package fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.AssessmentType;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.UserType;

public record FeedbackRequest(UserType userType, String email, String accessKey, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, Boolean feedbackUrgent, String feedbackDescription, String feedbackComment) {
}