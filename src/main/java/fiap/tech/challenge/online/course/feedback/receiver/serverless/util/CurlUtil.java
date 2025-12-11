package fiap.tech.challenge.online.course.feedback.receiver.serverless.util;

import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CurlUtil {

    public static String getCurlCommand(SdkHttpRequest request, ContentStreamProvider bodyProvider) {
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