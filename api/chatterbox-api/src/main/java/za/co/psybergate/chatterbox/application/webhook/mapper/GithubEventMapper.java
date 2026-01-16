package za.co.psybergate.chatterbox.application.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface GithubEventMapper {

    GithubEventDto map(String eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException;

    GithubEventDto map(EventType eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException;

}
