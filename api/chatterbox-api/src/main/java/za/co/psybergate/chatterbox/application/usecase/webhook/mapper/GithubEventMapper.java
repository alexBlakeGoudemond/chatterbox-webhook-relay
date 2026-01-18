package za.co.psybergate.chatterbox.application.usecase.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;

public interface GithubEventMapper {

    GithubEventDto map(String eventType, JsonNode payload);

    GithubEventDto map(EventType eventType, JsonNode payload);

}
