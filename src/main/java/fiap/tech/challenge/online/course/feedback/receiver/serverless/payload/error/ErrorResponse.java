package fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.error;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.UserTypeRequest;

public record ErrorResponse(UserTypeRequest userType, String email, String error) {
}