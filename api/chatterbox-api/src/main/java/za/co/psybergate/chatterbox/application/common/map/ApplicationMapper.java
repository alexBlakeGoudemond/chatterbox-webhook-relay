package za.co.psybergate.chatterbox.application.common.map;

import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;

public class ApplicationMapper {

    public static OutboundEvent mapToOutboundEvent(WebhookPolledEventReceivedDto event) {
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

    public static OutboundEvent mapToOutboundEvent(WebhookEventReceivedDto event) {
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
