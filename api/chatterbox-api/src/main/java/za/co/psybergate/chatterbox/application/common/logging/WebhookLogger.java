package za.co.psybergate.chatterbox.application.common.logging;

import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.http.model.HttpResponseDto;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
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

    void logEventReceived(GithubEventDto eventDto);

    void logUnknownEventType(String eventType);

    void logUnrecognizedRepository(String repositoryName);

    void logSendingDtoToTeams(OutboundEvent outboundEvent, String teamsDestination);

    void logSendingDtoToDiscord(OutboundEvent outboundEvent, String discordDestination);

    void logTeamsResponse(HttpResponseDto httpResponseDto);

    void logExceptionDetails(Exception exception);

    void logGithubPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logGithubPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logStoringEvent(Object webhook);

    void logEventStored(Object webhookEvent);

    void logDeliveringEvent(Object webhookEvent);

    void logEventDelivered(Object webhookEvent);

    void logProcessingEvents(DestinationMapping destinationMapping);

    void logRunnerFoundNoPreviousWebhooks(String repositoryFullName);

    void logRunnerFoundPreviousWebhook(WebhookEventReceivedDto latestWebhookEvent);

    void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName);

    void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceivedDto latestGithubPolledEvent);

    void logGithubPolledEventsEmpty(String repositoryFullName);

    void logWebhookEventsEmpty(String repositoryFullName);

    void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventsFound(List<WebhookPolledEventReceivedDto> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed);

    void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
