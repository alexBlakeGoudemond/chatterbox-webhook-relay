package za.co.psybergate.chatterbox.application.domain.delivery.model;

import com.fasterxml.jackson.databind.JsonNode;

public record HttpResponseDto(
        int httpStatus,
        String rawBody,
        JsonNode jsonNode
) {

}
