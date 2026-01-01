package za.co.psybergate.chatterbox.application.webhook.processing;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;

/// converts raw input to meaningful DTOs
public interface GithubEventExtractor {

    GithubEventDto extract(String eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException;

    GithubEventDto extract(EventType eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException;

}
