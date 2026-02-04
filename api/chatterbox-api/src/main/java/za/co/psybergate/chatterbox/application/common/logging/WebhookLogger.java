package za.co.psybergate.chatterbox.application.common.logging;

import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookLogger {

    void logMissingSignature();

    void logInvalidSignature(String expected, String received);

    void logValidSignature();

    void logReceivedWebhookEvent(String event, String delivery);

    void logCompletion(long ms);

    void logUnknownEventType(String eventType);

    void logUnrecognizedRepository(String repositoryName);

    void logSendingDtoToDestination(OutboundEvent outboundEvent, String destination);

    void logExceptionDetails(Exception exception);

    void logPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logStoringEvent(Object webhook);

    void logEventStored(Object webhookEvent);

    void logDeliveringEvent(Object webhookEvent);

    void logEventDelivered(Object webhookEvent);

    void logProcessingEvents(DestinationMapping destinationMapping);

    void logRunnerFoundNoPreviousWebhooks(String repositoryFullName);

    void logRunnerFoundPreviousWebhook(WebhookEventReceived latestWebhookEvent);

    void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName);

    void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceived latestGithubPolledEvent);

    void logPolledEventsEmpty(String repositoryFullName);

    void logWebhookEventsEmpty(String repositoryFullName);

    void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventsFound(List<WebhookPolledEventReceived> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed);

    void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
