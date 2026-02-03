package za.co.psybergate.chatterbox.application.port.out.webhook.mapper;

import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface OutboundEventMapperPort {

    OutboundEvent map(String eventType, RawEventPayload payload);

    OutboundEvent map(WebhookEventType webhookEventType, RawEventPayload payload);

}
