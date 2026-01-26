package za.co.psybergate.chatterbox.infrastructure.adapter.out.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HttpResponseHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) {
        int status = response.getCode();
        String rawBody = null;
        JsonNode jsonNode = null;
        if (response.getEntity() != null) {
            rawBody = getAsString(response);
            String contentType = response.getEntity().getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                jsonNode = getJsonNode(rawBody);
            }
        }
        return new HttpResponseDto(status, rawBody, jsonNode);
    }

    private JsonNode getJsonNode(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new ApplicationException("Unexpected issue when converting String into a JsonNode", e);
        }
    }

    private String getAsString(ClassicHttpResponse response) {
        try {
            return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationException("Unable to parse the Response Body into a String", e);
        }
    }

}
