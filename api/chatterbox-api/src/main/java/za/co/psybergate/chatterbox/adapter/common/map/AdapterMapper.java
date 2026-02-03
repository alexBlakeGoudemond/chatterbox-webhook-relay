package za.co.psybergate.chatterbox.adapter.common.map;

import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEventDeliveryLog;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;
import za.co.psybergate.chatterbox.application.domain.event.model.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class AdapterMapper {

    public static WebhookPolledEventReceivedDto mapToGithubPolledEventRecord(GithubPolledEvent polledEvent) {
        return new WebhookPolledEventReceivedDto(
                polledEvent.getId(),
                polledEvent.getRepositoryFullName(),
                polledEvent.getSourceId(),
                polledEvent.getWebhookEventType(),
                polledEvent.getDisplayName(),
                polledEvent.getSenderName(),
                polledEvent.getEventUrl(),
                polledEvent.getEventUrlDisplayText(),
                polledEvent.getExtraDetail(),
                polledEvent.getPayload(),
                polledEvent.getWebhookEventStatus(),
                polledEvent.getErrorMessage(),
                polledEvent.getFetchedAt(),
                polledEvent.getProcessedAt()
        );
    }

    public static WebhookPolledEventDeliveryDto mapToGithubPolledEventDeliveryRecord(GithubPolledEventDeliveryLog deliveryLog) {
        return new WebhookPolledEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getGithubPolledEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getWebhookEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    public static EventPayloadMapping mapToEventPayloadMapping(GithubEventMapping githubMapping) {
        return EventPayloadMapping.builder()
                .displayName(githubMapping.getDisplayName())
                .fields(githubMapping.getFields().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> EventPayloadMapping.IncomingMappingFieldKeys.valueOf(e.getKey().name()),
                                Map.Entry::getValue
                        )))
                .build();
    }

    public static WebhookEventReceivedDto mapToWebhookEventReceivedDto(WebhookEvent webhookEvent) {
        return new WebhookEventReceivedDto(webhookEvent.getId(),
                webhookEvent.getRepositoryFullName(),
                webhookEvent.getWebhookId(),
                webhookEvent.getWebhookEventType(),
                webhookEvent.getDisplayName(),
                webhookEvent.getSenderName(),
                webhookEvent.getEventUrl(),
                webhookEvent.getEventUrlDisplayText(),
                webhookEvent.getExtraDetail(),
                webhookEvent.getPayload(),
                webhookEvent.getWebhookEventStatus(),
                webhookEvent.getErrorMessage(),
                webhookEvent.getReceivedAt(),
                webhookEvent.getProcessedAt());
    }

    public static WebhookEventDeliveryDto mapToWebhookEventDeliveryRecord(WebhookEventDeliveryLog deliveryLog) {
        return new WebhookEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getWebhookEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getWebhookEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    public static WebhookEventDeliveryLog mapToWebhookEventDeliveryLog(OutboundEvent outboundEvent, String destinationName, String destinationUrl, WebhookEventStatus processedStatus) {
        return new WebhookEventDeliveryLog(outboundEvent.id(),
                destinationName,
                destinationUrl,
                processedStatus,
                LocalDateTime.now());
    }

    public static WebhookEventDeliveryLog mapToWebhookEventDeliveryLog(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        return mapToWebhookEventDeliveryLog(outboundEvent,
                destinationName,
                destinationUrl,
                WebhookEventStatus.PROCESSED_SUCCESS);
    }

    public static WebhookEvent mapToWebhookEvent(OutboundEvent outboundEvent) {
        return new WebhookEvent(
                outboundEvent.sourceId(),
                outboundEvent.repository(),
                outboundEvent.type(),
                outboundEvent.title(),
                outboundEvent.actor(),
                outboundEvent.url(),
                outboundEvent.displayText(),
                outboundEvent.extra(),
                outboundEvent.payload(),
                WebhookEventStatus.RECEIVED,
                LocalDateTime.now());
    }

    public static GithubPolledEvent mapToGithubPolledEvent(OutboundEvent outboundEvent) {
        return new GithubPolledEvent(
                outboundEvent.sourceId(),
                outboundEvent.repository(),
                outboundEvent.type(),
                outboundEvent.title(),
                outboundEvent.actor(),
                outboundEvent.url(),
                outboundEvent.displayText(),
                outboundEvent.extra(),
                outboundEvent.payload(),
                WebhookEventStatus.RECEIVED,
                LocalDateTime.now());
    }
}
