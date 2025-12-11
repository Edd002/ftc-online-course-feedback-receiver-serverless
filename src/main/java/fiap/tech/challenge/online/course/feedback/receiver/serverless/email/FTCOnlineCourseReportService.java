package fiap.tech.challenge.online.course.feedback.receiver.serverless.email;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.email.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.properties.FTCOnlineCourseReportProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
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
            if (hashIdFeedback != null) {
                URI uri = URI.create(ftcOnlineCourseReportProperties.getUrl());
                String payloadString = HttpObjectMapper.writeValueAsString(new FeedbackReportRequest(hashIdFeedback));
                ContentStreamProvider payloadContentStreamProvider = ContentStreamProvider.fromUtf8String(Objects.requireNonNull(payloadString));
                SdkHttpRequest sdkHttpRequest = SdkHttpRequest.builder()
                        .uri(uri)
                        .method(SdkHttpMethod.POST)
                        .putHeader("Host", uri.getHost())
                        .putHeader("Content-Length", String.valueOf(payloadString.getBytes(StandardCharsets.UTF_8).length))
                        .putHeader("Content-Type", "application/json")
                        .build();

                AwsCredentialsIdentity credentials = AwsCredentialsIdentity.builder().accessKeyId(ftcOnlineCourseReportProperties.getApiKeyId()).secretAccessKey(ftcOnlineCourseReportProperties.getApiKeySecret()).build();
                SdkHttpFullRequest signedRequest = (SdkHttpFullRequest) AwsV4HttpSigner.create().sign(r -> r
                                .identity(credentials)
                                .request(sdkHttpRequest)
                                .payload(RequestBody.empty().contentStreamProvider())
                                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, "lambda")
                                .putProperty(AwsV4HttpSigner.REGION_NAME, Region.US_EAST_2.id())
                                .payload(payloadContentStreamProvider))
                        .request();

                try (SdkHttpClient httpClient = AwsCrtHttpClient.builder().build()) {
                    HttpExecuteRequest request = HttpExecuteRequest.builder()
                            .request(signedRequest)
                            .contentStreamProvider(payloadContentStreamProvider)
                            .build();
                    HttpExecuteResponse response = httpClient.prepareRequest(request).call();
                    if (!response.httpResponse().isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}