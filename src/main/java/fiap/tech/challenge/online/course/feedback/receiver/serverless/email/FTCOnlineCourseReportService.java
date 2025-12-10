package fiap.tech.challenge.online.course.feedback.receiver.serverless.email;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.email.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.properties.FTCOnlineCourseReportProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class FTCOnlineCourseReportService {

    private final FTCOnlineCourseReportProperties ftcOnlineCourseReportProperties;

    public FTCOnlineCourseReportService(Properties applicationProperties) {
        ftcOnlineCourseReportProperties = new FTCOnlineCourseReportProperties(applicationProperties);
    }

    public void sendEmailUrgentFeedback(String hashIdFeedback) {
        try {
            String requestBody = HttpObjectMapper.writeValueAsString(new FeedbackReportRequest(hashIdFeedback));
            SdkHttpRequest sdkHttpRequest = SdkHttpRequest.builder()
                    .uri(URI.create(ftcOnlineCourseReportProperties.getUrl()))
                    .method(SdkHttpMethod.POST)
                    .putHeader("Content-Type", "application/json")
                    .putHeader("Content-Length", String.valueOf(Objects.requireNonNull(requestBody).getBytes(StandardCharsets.UTF_8).length))
                    .build();

            AwsCredentialsIdentity credentials = AwsCredentialsIdentity.builder().accessKeyId(ftcOnlineCourseReportProperties.getApiKeyId()).secretAccessKey(ftcOnlineCourseReportProperties.getApiKeySecret()).build();
            SignedRequest signedRequest = AwsV4HttpSigner.create().sign(awsCredentials -> awsCredentials
                    .identity(credentials)
                    .request(sdkHttpRequest)
                    .payload(RequestBody.empty().contentStreamProvider())
                    .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, "lambda")
                    .putProperty(AwsV4HttpSigner.REGION_NAME, Region.US_EAST_2.id()));
            String authorizationHeader = signedRequest.request().firstMatchingHeader("Authorization").orElse(null);

            try (SdkHttpClient httpClient = AwsCrtHttpClient.builder().build()) {
                HttpExecuteRequest request = HttpExecuteRequest.builder()
                        .contentStreamProvider(ContentStreamProvider.fromUtf8String(Objects.requireNonNull(requestBody)))
                        .request(sdkHttpRequest.toBuilder().putHeader("Authorization", authorizationHeader).build()).build();
                HttpExecuteResponse response = httpClient.prepareRequest(request).call();
                System.out.println("Response: " + response.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}