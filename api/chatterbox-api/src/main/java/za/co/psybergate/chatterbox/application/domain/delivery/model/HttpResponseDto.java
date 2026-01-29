package za.co.psybergate.chatterbox.application.domain.delivery.model;

import com.fasterxml.jackson.databind.JsonNode;

// TODO BlakeGoudemond 2026/01/27 | consider placing in infra - used with outbound work (deliveries)
public record HttpResponseDto(
        int httpStatus,
        String rawBody,
        JsonNode jsonNode
) {

}
