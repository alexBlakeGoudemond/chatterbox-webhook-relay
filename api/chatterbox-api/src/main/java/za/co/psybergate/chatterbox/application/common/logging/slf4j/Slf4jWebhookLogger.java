package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Slf4jWebhookLogger implements WebhookLogger {

    private final Slf4jSignatureLogger signatureLogger;

    private final Slf4jWebhookEventLogger webhookEventLogger;

    private final Slf4jValidationLogger validationLogger;

    private final Slf4jDeliveryLogger deliveryLogger;

    private final Slf4jStorageLogger storageLogger;

    private final Slf4jPollingLogger pollingLogger;

    private final Slf4jProcessingLogger processingLogger;

    private final Slf4jExceptionLogger exceptionLogger;

    private final Slf4jOrchestrationLogger orchestrationLogger;

    @Override
    public void logMissingSignature() {
        signatureLogger.logMissingSignature();
    }

    @Override
    public void logInvalidSignature(String expected, String received) {
        signatureLogger.logInvalidSignature(expected, received);
    }

    @Override
    public void logValidSignature() {
        signatureLogger.logValidSignature();
    }

    @Override
    public void logReceivedWebhookEvent(String event, String delivery) {
        webhookEventLogger.logReceivedWebhookEvent(event, delivery);
    }

    @Override
    public void logCompletion(long ms) {
        webhookEventLogger.logCompletion(ms);
    }

    @Override
    public void logUnknownEventType(String eventType) {
        validationLogger.logUnknownEventType(eventType);
    }

    @Override
    public void logUnrecognizedRepository(String repositoryName) {
        validationLogger.logUnrecognizedRepository(repositoryName);
    }

    @Override
    public void logSendingDtoToDestination(OutboundEvent outboundEvent, String destination) {
        deliveryLogger.logSendingDtoToDestination(outboundEvent, destination);
    }

    @Override
    public void logExceptionDetails(Exception exception) {
        exceptionLogger.logExceptionDetails(exception);
    }

    @Override
    public void logPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        pollingLogger.logPollRecentUpdates(owner, repositoryName, fromDate, untilDate);
    }

    @Override
    public void logPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        pollingLogger.logPollEventType(eventType, owner, repositoryName, fromDate, untilDate);
    }

    @Override
    public void logStoringEvent(Object webhook) {
        storageLogger.logStoringEvent(webhook);
    }

    @Override
    public void logEventStored(Object webhookEvent) {
        storageLogger.logEventStored(webhookEvent);
    }

    @Override
    public void logDeliveringEvent(Object webhookEvent) {
        storageLogger.logDeliveringEvent(webhookEvent);
    }

    @Override
    public void logEventDelivered(Object webhookEvent) {
        storageLogger.logEventDelivered(webhookEvent);
    }

    @Override
    public void logProcessingEvents(DestinationMapping destinationMapping) {
        processingLogger.logProcessingEvents(destinationMapping);
    }

    @Override
    public void logRunnerFoundNoPreviousWebhooks(String repositoryFullName) {
        orchestrationLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousWebhook(WebhookEventReceived latestWebhookEvent) {
        orchestrationLogger.logRunnerFoundPreviousWebhook(latestWebhookEvent);
    }

    @Override
    public void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName) {
        orchestrationLogger.logRunnerFoundNoPreviousPolledEvents(repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceived latestGithubPolledEvent) {
        orchestrationLogger.logRunnerFoundPreviousPolledEvent(latestGithubPolledEvent);
    }

    @Override
    public void logPolledEventsEmpty(String repositoryFullName) {
        storageLogger.logPolledEventsEmpty(repositoryFullName);
    }

    @Override
    public void logWebhookEventsEmpty(String repositoryFullName) {
        storageLogger.logWebhookEventsEmpty(repositoryFullName);
    }

    @Override
    public void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime) {
        orchestrationLogger.logNoPolledEventsFound(repositoryFullName, lastPersistedTime);
    }

    @Override
    public void logPolledEventsFound(List<WebhookPolledEventReceived> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime) {
        orchestrationLogger.logPolledEventsFound(githubPolledEvents, repositoryFullName, lastPersistedTime);
    }

    @Override
    public void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed) {
        processingLogger.logPolledEventProcessed(polledEventsProcessed);
    }

    @Override
    public void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        processingLogger.logWebhookEventProcessed(webhookEventProcessed);
    }

}
