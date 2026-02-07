package za.co.psybergate.chatterbox.application.common.map;

import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

public class ApplicationMapper {

    public static OutboundEvent mapToOutboundEvent(WebhookPolledEventReceived event) {
        return new OutboundEvent(
                event.id(),
                event.sourceId(),
                event.webhookEventType().name(),
                event.displayName(),
                event.repositoryFullName(),
                event.senderName(),
                event.eventUrl(),
                event.eventUrlDisplayText(),
                event.extraDetail(),
                event.payload()
        );
    }

    public static OutboundEvent mapToOutboundEvent(WebhookEventReceived event) {
        return new OutboundEvent(
                event.id(),
                event.webhookId(),
                event.webhookEventType().name(),
                event.displayName(),
                event.repositoryFullName(),
                event.senderName(),
                event.eventUrl(),
                event.eventUrlDisplayText(),
                event.extraDetail(),
                event.payload()
        );
    }

}
