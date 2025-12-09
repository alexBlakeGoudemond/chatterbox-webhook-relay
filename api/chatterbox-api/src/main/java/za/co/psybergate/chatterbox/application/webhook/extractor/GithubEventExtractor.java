package za.co.psybergate.chatterbox.application.webhook.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface GithubEventExtractor {

    GithubEventDto extract(String eventType, JsonNode payload) throws ConstraintViolationException;

}
