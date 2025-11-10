package fiap.tech.challenge.online.course.feedback.receiver.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.UserTypeRequest;
import mock.TestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTCOnlineCourseFeedbackReceiverServerlessHandlerTests {

    @Test
    void handleRequest_AdministratorSearchFeedbackSuccess() {
        FTCOnlineCourseFeedbackReceiverServerlessHandler handler = new FTCOnlineCourseFeedbackReceiverServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        request.setPath("/");
        request.setQueryStringParameters(HttpObjectMapper.convertValue(new FeedbackRequest(UserTypeRequest.ADMINISTRATOR, "administrador1@email.com", "123", false, "Descrição Feedback", "Comentário Feedback"), new TypeReference<>() {}));
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(200, response.getStatusCode().intValue());
    }

    @Test
    void handleRequest_TeacherSearchFeedbackSuccess() {
        FTCOnlineCourseFeedbackReceiverServerlessHandler handler = new FTCOnlineCourseFeedbackReceiverServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        request.setPath("/");
        request.setQueryStringParameters(HttpObjectMapper.convertValue(new FeedbackRequest(UserTypeRequest.TEACHER, "teacher1@email.com", "123", false, "Descrição Feedback", "Comentário Feedback"), new TypeReference<>() {}));
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(200, response.getStatusCode().intValue());
    }

    @Test
    void handleRequest_StudentSearchFeedbackSuccess() {
        FTCOnlineCourseFeedbackReceiverServerlessHandler handler = new FTCOnlineCourseFeedbackReceiverServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("GET");
        request.setPath("/");
        request.setQueryStringParameters(HttpObjectMapper.convertValue(new FeedbackRequest(UserTypeRequest.STUDENT, "student1@email.com", "123", false, "Descrição Feedback", "Comentário Feedback"), new TypeReference<>() {}));
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(200, response.getStatusCode().intValue());
    }
}