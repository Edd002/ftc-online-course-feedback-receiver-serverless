package fiap.tech.challenge.online.course.feedback.receiver.serverless.config;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.loader.ApplicationPropertiesLoader;

import java.util.Properties;

public class DataSourceConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public DataSourceConfig() {
        Properties applicationProperties = ApplicationPropertiesLoader.loadProperties(getClass().getClassLoader());
        this.host = applicationProperties.getProperty("application.datasource.hostname");
        this.port = Integer.parseInt(applicationProperties.getProperty("application.datasource.port"));
        this.database = applicationProperties.getProperty("application.datasource.database");
        this.username = applicationProperties.getProperty("application.datasource.username");
        this.password = applicationProperties.getProperty("application.datasource.password");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabase();
    }
}