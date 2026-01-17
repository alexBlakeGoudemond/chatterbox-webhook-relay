package za.co.psybergate.chatterbox.application.usecase.logging;

import za.co.psybergate.chatterbox.domain.github.model.GithubDestinationMapping;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.event.model.WebhookEventDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.domain.event.notification.WebhookEventProcessed;

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

    void logSendingDtoToTeams(GithubEventDto eventDto, String teamsDestination);

    void logSendingDtoToDiscord(GithubEventDto eventDto, String discordDestination);

    void logTeamsResponse(HttpResponseDto httpResponseDto);

    void logExceptionDetails(Exception exception);

    void logGithubPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logGithubPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logStoringEvent(Object webhook);

    void logEventStored(Object webhookEvent);

    void logDeliveringEvent(Object webhookEvent);

    void logEventDelivered(Object webhookEvent);

    void logProcessingEvents(GithubDestinationMapping destinationMapping);

    void logRunnerFoundNoPreviousWebhooks(String repositoryFullName);

    void logRunnerFoundPreviousWebhook(WebhookEventDto latestWebhookEvent);

    void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName);

    void logRunnerFoundPreviousPolledEvent(GithubPolledEventDto latestGithubPolledEvent);

    void logGithubPolledEventsEmpty(String repositoryFullName);

    void logWebhookEventsEmpty(String repositoryFullName);

    void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventsFound(List<GithubPolledEventDto> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed);

    void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
