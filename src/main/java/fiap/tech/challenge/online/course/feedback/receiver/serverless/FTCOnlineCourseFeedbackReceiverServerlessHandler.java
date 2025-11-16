package fiap.tech.challenge.online.course.feedback.receiver.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.dao.FTCOnlineCourseFeedbackReceiverServerlessDAO;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
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
        FeedbackRequest feedbackRequest = HttpObjectMapper.readValue(request.getBody(), FeedbackRequest.class);
        if (feedbackRequest == null) {
            context.getLogger().log("Erro de conversão de payload de requisição.", LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(new RuntimeException("O payload para cadastro de feedback não foi informado corretamente."));
        }
        try {
            context.getLogger().log("Requisição recebida em FTC Online Course Feedback Receiver - UserType: " + feedbackRequest.userType() + " - E-mail: " + feedbackRequest.email(), LogLevel.INFO);
            validateAPIGatewayProxyRequestEvent(feedbackRequest);
            Long teacherId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherIdByEmailAndAccessKey(feedbackRequest);
            Long teacherStudentId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherStudentIdByTeacherIdAndStudentEmail(teacherId, feedbackRequest);
            ftcOnlineCourseFeedbackReceiverServerlessDAO.registerFeedback(teacherStudentId, feedbackRequest);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildErrorResponse(feedbackRequest, e);
        }
    }

    private void validateAPIGatewayProxyRequestEvent(FeedbackRequest feedbackRequest) {
        try {
            if (feedbackRequest == null || feedbackRequest.userType() == null || feedbackRequest.email() == null || feedbackRequest.accessKey() == null) {
                throw new InvalidParameterException("O tipo de usuário juntamente com seu o e-mail e chave de acesso são obrigatórios para realizar a cadastro de feedback.");
            }
        } catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage()))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(FeedbackRequest feedbackRequest, Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(feedbackRequest.userType(), feedbackRequest.email(), e.getMessage()))).withIsBase64Encoded(false);
    }
}