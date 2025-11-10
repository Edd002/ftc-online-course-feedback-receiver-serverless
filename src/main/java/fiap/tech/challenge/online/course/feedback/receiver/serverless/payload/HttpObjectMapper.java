package fiap.tech.challenge.online.course.feedback.receiver.serverless.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpObjectMapper {

    private static final ObjectMapper payloadObjectMapper = new ObjectMapper();

    public static String writeValueAsString(Object value) {
        try {
            return payloadObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return payloadObjectMapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return payloadObjectMapper.convertValue(fromValue, toValueType);
    }

    public static <T> T convertValue(Object fromValue, TypeReference<T> toValueType) {
        return payloadObjectMapper.convertValue(fromValue, toValueType);
    }
}
