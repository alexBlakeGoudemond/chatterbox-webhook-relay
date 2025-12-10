package za.co.psybergate.chatterbox.application.webhook.processing;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

/// converts raw input to meaningful DTOs
public interface GithubEventExtractor {

    GithubEventDto extract(String eventType, JsonNode payload) throws ConstraintViolationException;

}
