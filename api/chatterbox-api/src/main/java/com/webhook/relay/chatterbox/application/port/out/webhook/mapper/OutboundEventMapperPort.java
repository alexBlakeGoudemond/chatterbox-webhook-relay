package com.webhook.relay.chatterbox.application.port.out.webhook.mapper;

import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;

public interface OutboundEventMapperPort {

    OutboundEvent map(String eventType, RawEventPayload payload);

    OutboundEvent map(WebhookEventType webhookEventType, RawEventPayload payload);

}
