package fiap.tech.challenge.online.course.feedback.receiver.serverless.email;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.email.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.properties.FTCOnlineCourseReportProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class FTCOnlineCourseReportService {

    private final FTCOnlineCourseReportProperties ftcOnlineCourseReportProperties;

    public FTCOnlineCourseReportService(Properties applicationProperties) {
        ftcOnlineCourseReportProperties = new FTCOnlineCourseReportProperties(applicationProperties);
    }

    public void sendEmailUrgentFeedback(String hashIdFeedback) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String requestBody = HttpObjectMapper.writeValueAsString(new FeedbackReportRequest(hashIdFeedback));
            SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                    .uri(URI.create(ftcOnlineCourseReportProperties.getUrl()))
                    .method(SdkHttpMethod.POST)
                    .putHeader("Content-Type", "application/json")
                    .putHeader("Content-Length", String.valueOf(Objects.requireNonNull(requestBody).getBytes(StandardCharsets.UTF_8).length))
                    .contentStreamProvider(ContentStreamProvider.fromUtf8String(Objects.requireNonNull(requestBody)))
                    .build();

            AwsCredentialsIdentity credentials = AwsCredentialsIdentity.builder().accessKeyId(ftcOnlineCourseReportProperties.getApiKeyId()).secretAccessKey(ftcOnlineCourseReportProperties.getApiKeySecret()).build();
            RequestBody requestPayload = RequestBody.empty();
            SignedRequest signedRequest = AwsV4HttpSigner.create().sign(awsCredentials -> awsCredentials
                    .identity(credentials)
                    .request(httpRequest)
                    .payload(requestPayload.contentStreamProvider())
                    .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, "lambda")
                    .putProperty(AwsV4HttpSigner.REGION_NAME, Region.US_EAST_2.id()));
            String authorizationHeader = signedRequest.request().firstMatchingHeader("Authorization").orElse(null);

            
            SdkHttpResponse sdkHttpResponse;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}