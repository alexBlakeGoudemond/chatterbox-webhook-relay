package za.co.psybergate.chatterbox.application.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface GithubEventMapper {

    GithubEventDto map(String eventType, JsonNode payload);

    GithubEventDto map(EventType eventType, JsonNode payload);

}
