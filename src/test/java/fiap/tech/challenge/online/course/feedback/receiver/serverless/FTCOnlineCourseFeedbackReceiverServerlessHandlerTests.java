package fiap.tech.challenge.online.course.feedback.receiver.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.AssessmentType;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.UserType;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.mock.TestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTCOnlineCourseFeedbackReceiverServerlessHandlerTests {

    @Test
    void handleRequest_RegisterFeedbackSuccess() {
        FTCOnlineCourseFeedbackReceiverServerlessHandler handler = new FTCOnlineCourseFeedbackReceiverServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("POST");
        request.setPath("/");
        request.setBody(HttpObjectMapper.writeValueAsString(new FeedbackRequest(UserType.TEACHER, "teacher1@email.com", "123", "student1@email.com", "Nome Assessment 4", AssessmentType.TEST, 5.0, true, "Descrição Assessment 4", "Comentário Assessment 4")));
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(201, response.getStatusCode().intValue());
    }
}