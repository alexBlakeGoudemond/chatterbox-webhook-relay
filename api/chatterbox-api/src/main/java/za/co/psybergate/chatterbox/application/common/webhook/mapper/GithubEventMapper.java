package za.co.psybergate.chatterbox.application.common.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

// TODO BlakeGoudemond 2026/02/03 | Should not mention Github
public interface GithubEventMapper {

    OutboundEvent map(String eventType, JsonNode payload);

    OutboundEvent map(WebhookEventType webhookEventType, JsonNode payload);

}
