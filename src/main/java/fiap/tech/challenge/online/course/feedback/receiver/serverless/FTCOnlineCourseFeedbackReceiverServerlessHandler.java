package fiap.tech.challenge.online.course.feedback.receiver.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.dao.FTCOnlineCourseFeedbackReceiverServerlessDAO;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.UserTypeRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.error.ErrorResponse;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.error.InvalidParameterErrorResponse;

import java.security.InvalidParameterException;

public class FTCOnlineCourseFeedbackReceiverServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final FTCOnlineCourseFeedbackReceiverServerlessDAO ftcOnlineCourseFeedbackReceiverServerlessDAO;

    static {
        ftcOnlineCourseFeedbackReceiverServerlessDAO = new FTCOnlineCourseFeedbackReceiverServerlessDAO();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            FeedbackRequest feedbackRequest = validateAPIGatewayProxyRequestEvent(request);
            context.getLogger().log("Requisição recebida em FTC Online Course Feedback Receiver - UserType: " + feedbackRequest.userType()  + " - E-mail: " + feedbackRequest.email(), LogLevel.INFO);
            Long teacherId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherIdByEmailAndAccessKey(feedbackRequest);
            Long teacherStudentId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherStudentIdByTeacherIdAndStudentEmail(teacherId, feedbackRequest);
            ftcOnlineCourseFeedbackReceiverServerlessDAO.registerFeedback(teacherStudentId, feedbackRequest);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildErrorResponse(request, e);
        }
    }

    private FeedbackRequest validateAPIGatewayProxyRequestEvent(APIGatewayProxyRequestEvent request) {
        try {
            if (request.getQueryStringParameters() == null || request.getQueryStringParameters().get("userType") == null || request.getQueryStringParameters().get("email") == null || request.getQueryStringParameters().get("accessKey") == null) {
                throw new InvalidParameterException("O tipo de usuário juntamente com seu o e-mail e chave de acesso são obrigatórios para realizar a cadastro de feedback.");
            }
            return HttpObjectMapper.readValue(request.getBody(), FeedbackRequest.class);
        } catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage()))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(APIGatewayProxyRequestEvent request, Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(UserTypeRequest.valueOf(request.getQueryStringParameters().get("userType")), request.getQueryStringParameters().get("email"), e.getMessage()))).withIsBase64Encoded(false);
    }
}