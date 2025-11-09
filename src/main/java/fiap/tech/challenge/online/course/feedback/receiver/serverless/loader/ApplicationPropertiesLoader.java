package fiap.tech.challenge.online.course.feedback.receiver.serverless.loader;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationPropertiesLoader {

    public static Properties loadProperties(ClassLoader classLoaderFilePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = classLoaderFilePath.getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo properties de propriedade do sistema: " + e.getMessage());
        }
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            Matcher matcher = pattern.matcher(value);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String envVarName =  matcher.group(1);
                String envVarValue = System.getenv(envVarName) != null ? System.getenv(envVarName) : Dotenv.load().get(envVarName);
                if (envVarValue != null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(envVarValue));
                } else {
                    System.err.println("Variável de ambiente " + envVarName + " não encontrada para a propriedade " + key + ".");
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);
            properties.setProperty(key, sb.toString());
        }
        return properties;
    }
}
