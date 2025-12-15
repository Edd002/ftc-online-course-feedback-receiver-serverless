package fiap.tech.challenge.online.course.feedback.receiver.serverless.email;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.email.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.properties.FTCOnlineCourseReportProperties;

import java.util.Properties;

public class FTCOnlineCourseReportQueueService {

    private final FTCOnlineCourseReportProperties ftcOnlineCourseReportProperties;

    public FTCOnlineCourseReportQueueService(Properties applicationProperties) {
        ftcOnlineCourseReportProperties = new FTCOnlineCourseReportProperties(applicationProperties);
    }

    public void send(String hashIdFeedback) {
        try {
            if (hashIdFeedback != null) {
                String payloadString = HttpObjectMapper.writeValueAsString(new FeedbackReportRequest(hashIdFeedback));
                AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ftcOnlineCourseReportProperties.getApiKeyId(), ftcOnlineCourseReportProperties.getApiKeySecret()))).withRegion(Regions.US_EAST_2).build();
                SendMessageRequest sendMessageResult = new SendMessageRequest()
                        .withQueueUrl(ftcOnlineCourseReportProperties.getQueueUrl())
                        .withMessageBody(payloadString)
                        .withDelaySeconds(5);
                sqs.sendMessage(sendMessageResult);
            }
        } catch (Exception e) {
            throw new AmazonSQSException(e.getMessage());
        }
    }
}