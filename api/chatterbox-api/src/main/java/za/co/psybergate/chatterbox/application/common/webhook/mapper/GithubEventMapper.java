package za.co.psybergate.chatterbox.application.common.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;

public interface GithubEventMapper {

    GithubEventDto map(String eventType, JsonNode payload);

    GithubEventDto map(WebhookEventType webhookEventType, JsonNode payload);

}
