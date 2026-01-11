package za.co.psybergate.chatterbox.application.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.discord.delivery.DiscordSenderService;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderService;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties.DestinationMapping;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventProcessorImpl implements EventProcessor {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderService teamsSenderService;

    private final DiscordSenderService discordSenderService;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    private final ChatterboxDestinationDiscordProperties destinationDiscordProperties;

    private final GithubPolledStore githubPolledStore;

    private final WebhookReceivedStore webhookReceivedStore;

    @Override
    public void processWebhookEvents() {
        List<DestinationMapping> destinationMappings = repositoryProperties.getDestinationMapping();
        for (DestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processWebhookEvents(destinationMapping);
        }
    }

    @Override
    public void processPolledEvents() {
        List<DestinationMapping> destinationMappings = repositoryProperties.getDestinationMapping();
        for (DestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processPolledEvents(destinationMapping);
        }
    }

    private void processWebhookEvents(DestinationMapping destinationMapping) {
        for (WebhookEvent webhookEvent : webhookReceivedStore.getLatestWebhooks(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), webhookEvent);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), webhookEvent);
            webhookReceivedStore.setProcessedStatus(webhookEvent, EventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(DestinationMapping destinationMapping) {
        for (GithubPolledEvent latestEvent : githubPolledStore.getLatestEvents(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), latestEvent);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), latestEvent);
            githubPolledStore.setProcessedStatus(latestEvent, EventStatus.PROCESSED_SUCCESS);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookEvent webhookEvent) {
        String destinationUrl = destinationTeamsProperties.getUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(webhookEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            webhookReceivedStore.storeSuccessfulDelivery(webhookEvent, teamsDestinationChannel, destinationUrl);
        }else{
            webhookReceivedStore.storeUnsuccessfulDelivery(webhookEvent, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, GithubPolledEvent polledEvent) {
        String destinationUrl = destinationTeamsProperties.getUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(polledEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            githubPolledStore.storeSuccessfulDelivery(polledEvent, teamsDestinationChannel, destinationUrl);
        }else{
            githubPolledStore.storeUnsuccessfulDelivery(polledEvent, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, GithubPolledEvent polledEvent) {
        String destinationUrl = destinationDiscordProperties.getUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(polledEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            githubPolledStore.storeSuccessfulDelivery(polledEvent, discordDestinationChannel, destinationUrl);
        }else{
            githubPolledStore.storeUnsuccessfulDelivery(polledEvent, discordDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, WebhookEvent webhookEvent) {
        String destinationUrl = destinationDiscordProperties.getUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(webhookEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            webhookReceivedStore.storeSuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }else{
            webhookReceivedStore.storeUnsuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }
    }

    private HttpResponseDto deliverToDiscord(WebhookEvent webhookEvent, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(webhookEvent);
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToDiscord(GithubPolledEvent polledEvent, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(polledEvent);
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToTeams(GithubPolledEvent polledEvent, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(polledEvent);
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(WebhookEvent webhookEvent, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(webhookEvent);
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(GithubEventDto eventDto, String teamsDestination) {
        webhookLogger.logEventReceived(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto, teamsDestination);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

    private HttpResponseDto deliverToDiscord(GithubEventDto eventDto, String discordDestinationUrl) {
        webhookLogger.logEventReceived(eventDto);
        HttpResponseDto httpResponseDto = discordSenderService.process(eventDto, discordDestinationUrl);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

}
