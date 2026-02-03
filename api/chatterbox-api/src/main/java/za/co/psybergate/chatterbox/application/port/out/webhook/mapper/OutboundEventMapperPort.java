package za.co.psybergate.chatterbox.application.port.out.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface OutboundEventMapperPort {

    OutboundEvent map(String eventType, JsonNode payload);

    OutboundEvent map(WebhookEventType webhookEventType, JsonNode payload);

}
