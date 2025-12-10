package fiap.tech.challenge.online.course.feedback.receiver.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.config.KMSConfig;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.dao.FTCOnlineCourseFeedbackReceiverServerlessDAO;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.email.FTCOnlineCourseReportService;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.loader.ApplicationPropertiesLoader;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record.error.ErrorResponse;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record.error.InvalidParameterErrorResponse;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Properties;

public class FTCOnlineCourseFeedbackReceiverServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final KMSConfig kmsConfig;
    private static final Properties applicationProperties;
    private static final FTCOnlineCourseFeedbackReceiverServerlessDAO ftcOnlineCourseFeedbackReceiverServerlessDAO;
    private static final FTCOnlineCourseReportService ftcOnlineCourseReportService;

    static {
        try {
            kmsConfig = new KMSConfig();
            applicationProperties = ApplicationPropertiesLoader.loadProperties(kmsConfig);
            ftcOnlineCourseFeedbackReceiverServerlessDAO = new FTCOnlineCourseFeedbackReceiverServerlessDAO(applicationProperties);
            ftcOnlineCourseReportService = new FTCOnlineCourseReportService(applicationProperties);
        } catch (Exception ex) {
            System.err.println("Message: " + ex.getMessage() + " - Cause: " + ex.getCause() + " - Stacktrace: " + Arrays.toString(ex.getStackTrace()));
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            FeedbackRequest feedbackRequest = HttpObjectMapper.readValue(request.getBody(), FeedbackRequest.class);
            if (feedbackRequest == null) {
                context.getLogger().log("Erro de conversão de payload de requisição.", LogLevel.ERROR);
                return buildInvalidParameterErrorResponse(new InvalidParameterException("O payload para cadastro de feedback não foi informado corretamente."));
            }
            context.getLogger().log("Requisição recebida em FTC Online Course Feedback Receiver - UserType: " + feedbackRequest.userType() + " - E-mail: " + feedbackRequest.email(), LogLevel.INFO);
            validateAPIGatewayProxyRequestEvent(feedbackRequest);
            Long teacherId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherIdByEmailAndAccessKey(feedbackRequest);
            Long teacherStudentId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getTeacherStudentIdByTeacherIdAndStudentEmail(teacherId, feedbackRequest);
            Long feedbackId = ftcOnlineCourseFeedbackReceiverServerlessDAO.registerFeedback(teacherStudentId, feedbackRequest);
            String feedbackHashId = ftcOnlineCourseFeedbackReceiverServerlessDAO.getFeedbackHashIdById(feedbackId);
            ftcOnlineCourseReportService.sendEmailUrgentFeedback(feedbackHashId);
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildErrorResponse(e);
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

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(InvalidParameterException e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage(), e.getCause() != null ? e.getCause().toString() : null))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(e.getMessage(), e.getCause() != null ? e.getCause().toString() : null))).withIsBase64Encoded(false);
    }
}