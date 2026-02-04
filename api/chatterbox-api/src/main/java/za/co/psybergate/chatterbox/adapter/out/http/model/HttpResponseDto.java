package za.co.psybergate.chatterbox.adapter.out.http.model;

import com.fasterxml.jackson.databind.JsonNode;

public record HttpResponseDto(
        int httpStatus,
        String rawBody,
        JsonNode jsonNode
) {

}
