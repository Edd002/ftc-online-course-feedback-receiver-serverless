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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class FTCOnlineCourseReportService {

    private final FTCOnlineCourseReportProperties ftcOnlineCourseReportProperties;

    public FTCOnlineCourseReportService(Properties applicationProperties) {
        ftcOnlineCourseReportProperties = new FTCOnlineCourseReportProperties(applicationProperties);
    }

    public void sendEmailUrgentFeedback(String hashIdFeedback) {
        try {
            URI uri = URI.create(ftcOnlineCourseReportProperties.getUrl());
            ContentStreamProvider payloadContentStreamProvider = ContentStreamProvider.fromUtf8String(Objects.requireNonNull(HttpObjectMapper.writeValueAsString(new FeedbackReportRequest(hashIdFeedback))));
            SdkHttpRequest sdkHttpRequest = SdkHttpRequest.builder()
                    .uri(uri)
                    .method(SdkHttpMethod.POST)
                    .putHeader("Host", uri.getHost())
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
                System.out.println("Response status code: " + response.httpResponse().statusCode());
                System.out.println("Curl: " + getCurlCommand(request.httpRequest(), request.contentStreamProvider().orElse(null)));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getCurlCommand(SdkHttpRequest request, ContentStreamProvider bodyProvider) {
        StringBuilder curl = new StringBuilder("curl -X ").append(request.method().name()).append(" '").append(request.getUri().toString()).append("'");
        request.headers().forEach((key, values) -> {
            for (String value : values) {
                curl.append(" -H '").append(key).append(": ").append(value).append("'");
            }
        });
        if (bodyProvider != null) {
            try {
                String body = new BufferedReader(new InputStreamReader(bodyProvider.newStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                body = body.replace("'", "'\\''");
                curl.append(" -d '").append(body).append("'");
            } catch (Exception e) {
                System.err.println("Could not read request body for curl command: " + e.getMessage());
            }
        }
        return curl.toString();
    }
}

