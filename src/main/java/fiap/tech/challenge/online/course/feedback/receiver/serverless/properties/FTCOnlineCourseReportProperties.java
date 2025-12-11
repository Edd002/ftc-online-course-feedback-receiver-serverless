package fiap.tech.challenge.online.course.feedback.receiver.serverless.properties;

import java.util.Properties;

public class FTCOnlineCourseReportProperties {

    private final String url;
    private final String apiKeyId;
    private final String apiKeySecret;

    public FTCOnlineCourseReportProperties(Properties applicationProperties) {
        this.url = applicationProperties.getProperty("application.ftc.online.course.report.url");
        this.apiKeyId = applicationProperties.getProperty("application.ftc.online.course.api.key.id");
        this.apiKeySecret = applicationProperties.getProperty("application.ftc.online.course.api.key.secret");
    }

    public String getUrl() {
        return url;
    }

    public String getApiKeyId() {
        return apiKeyId;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }
}