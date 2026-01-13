package za.co.psybergate.chatterbox.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.domain.event.WebhookEventRecord;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties.DestinationMapping;
import za.co.psybergate.chatterbox.infrastructure.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.infrastructure.event.WebhookEventProcessed;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class WebhookLogger {

    public void logMissingSignature() {
        log.warn("[Signature] Missing signature");
    }

    public void logInvalidSignature(String expected, String received) {
        log.warn("[Signature] Invalid signature, expected={}, received={}", expected, received);
    }

    public void logValidSignature() {
        log.info("[Signature] Valid signature");
    }

    public void logReceivedWebhookEvent(String event, String delivery) {
        log.info("[Webhook] Received event={} delivery={}", event, delivery);
    }

    public void logCompletion(long ms) {
        log.debug("[Webhook] Completed in {}ms", ms);
    }

    public void logEventReceived(GithubEventDto eventDto) {
        log.debug("[WebhookEvent] received DTO: {}", eventDto);
    }

    public void logUnknownEventType(String eventType) {
        log.debug("[Validation] No ConfigurationProperties Found for eventType: {}", eventType);
    }

    public void logUnrecognizedRepository(String repositoryName) {
        log.debug("[Validation] Repository '{}' is not whitelisted as an accepted repository", repositoryName);
    }

    public void logSendingDtoToTeams(GithubEventDto eventDto, String teamsDestination) {
        log.info("[Delivery] Sending '{}' to MS Teams destination '{}'", eventDto.displayName(), teamsDestination);
    }

    public void logSendingDtoToDiscord(GithubEventDto eventDto, String discordDestination) {
        log.info("[Delivery] Sending '{}' to Discord destination '{}'", eventDto.displayName(), discordDestination);
    }

    public void logTeamsResponse(HttpResponseDto httpResponseDto) {
        log.info("[Delivery] MS Teams Response: {}", httpResponseDto);
    }

    public void logExceptionDetails(Exception exception) {
        log.error("[Exception] Exception encountered: {}", exception.getClass().getSimpleName(), exception);
    }

    public void logGithubPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.info("[GithubAPI] querying '{}/{}' for any updates since {} - {}", owner, repositoryName, fromDate, untilDate);
    }

    public void logGithubPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.debug("[GithubAPI] querying if any {} occurred for '{}/{}' since {} - {}", eventType, owner, repositoryName, fromDate, untilDate);
    }

    public void logStoringEvent(Object webhook) {
        log.debug("[Storage] Storing webhook event: {}", truncate(webhook));
    }

    public void logEventStored(Object webhookEvent) {
        log.trace("[Storage] webhook event stored: {}", truncate(webhookEvent, -1));
    }

    public void logDeliveringEvent(Object webhookEvent) {
        log.debug("[Storage] delivering webhook event: {}", truncate(webhookEvent));
    }

    public void logEventDelivered(Object webhookEvent) {
        log.trace("[Storage] webhook event delivered: {}", truncate(webhookEvent, -1));
    }

    public void logProcessingEvents(DestinationMapping destinationMapping) {
        log.info("[Processing] Processing Received Webhook events for destination: '{}'", destinationMapping.getName());
    }

    public void logRunnerFoundNoPreviousWebhooks(String repositoryFullName) {
        log.warn("[Runner] No previous webhooks found for the destination '{}', will not Poll", repositoryFullName);
    }

    public void logRunnerFoundPreviousWebhook(WebhookEventRecord latestWebhookEvent) {
        log.info("[Runner] Previous webhook found '{}', continuing with Poll", truncate(latestWebhookEvent));
    }

    public void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName) {
        log.warn("[Runner] No previous polled events found for the destination '{}', not participating in Poll", repositoryFullName);
    }

    public void logRunnerFoundPreviousPolledEvent(GithubPolledEventRecord latestGithubPolledEvent) {
        log.info("[Runner] Previous polled event found '{}', continuing with Poll", truncate(latestGithubPolledEvent));
    }

    public void logGithubPolledEventsEmpty(String repositoryFullName) {
        log.warn("[Polling] No GithubPolledEvents found for the destination '{}'", repositoryFullName);
    }

    public void logWebhookEventsEmpty(String repositoryFullName) {
        log.warn("[Processing] No WebhookEvents found for the destination '{}'", repositoryFullName);
    }

    public void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.warn("[Polling] No GithubPolledEvents found for '{}' since '{}'", repositoryFullName, lastPersistedTime);
    }

    public void logPolledEventsFound(List<GithubPolledEventRecord> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.info("[Polling] Found {} GithubPolledEvents for '{}' since '{}'", githubPolledEvents.size(), repositoryFullName, lastPersistedTime);
    }

    public void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed) {
        log.debug("[Listener] PolledEventsProcessed have occurred: {}", polledEventsProcessed);
    }

    public void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        log.debug("[Listener] WebhookEventProcessed has occurred: {}", webhookEventProcessed);
    }

    private String truncate(Object object) {
        return truncate(object.toString(), 300);
    }

    @SuppressWarnings("SameParameterValue")
    private String truncate(Object object, int length) {
        return truncate(object.toString(), length);
    }

    @SuppressWarnings("SameParameterValue")
    private String truncate(String string, int length) {
        if (string == null || string.isEmpty()) {
            throw new ApplicationException("Cannot truncate null/empty string");
        } else if (length <= 0) {
            return string;
        }
        return string.length() > length ? string.substring(0, length - 4) + " ..." : string;
    }

}
