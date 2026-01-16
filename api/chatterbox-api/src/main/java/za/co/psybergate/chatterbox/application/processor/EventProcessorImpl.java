package za.co.psybergate.chatterbox.application.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.discord.delivery.DiscordSenderService;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.provider.ConfigurationProvider;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderService;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.persistence.dto.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventRecord;
import za.co.psybergate.chatterbox.domain.github.GithubDestinationMapping;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventProcessorImpl implements EventProcessor {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderService teamsSenderService;

    private final DiscordSenderService discordSenderService;

    private final ConfigurationProvider configurationProvider;

    private final GithubPolledStore githubPolledStore;

    private final WebhookReceivedStore webhookReceivedStore;

    @Override
    public void processWebhookEvents() {
        List<GithubDestinationMapping> destinationMappings = configurationProvider.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processWebhookEvents(destinationMapping);
        }
    }

    @Override
    public void processPolledEvents() {
        List<GithubDestinationMapping> destinationMappings = configurationProvider.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processPolledEvents(destinationMapping);
        }
    }

    private void processWebhookEvents(GithubDestinationMapping destinationMapping) {
        for (WebhookEventRecord webhookEventRecord : webhookReceivedStore.getUnprocessedWebhooks(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), webhookEventRecord);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), webhookEventRecord);
            webhookReceivedStore.setProcessedStatus(webhookEventRecord, EventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(GithubDestinationMapping destinationMapping) {
        for (GithubPolledEventRecord latestEventRecord : githubPolledStore.getUnprocessedEvents(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), latestEventRecord);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), latestEventRecord);
            githubPolledStore.setProcessedStatus(latestEventRecord, EventStatus.PROCESSED_SUCCESS);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookEventRecord webhookEventRecord) {
        String destinationUrl = configurationProvider.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(webhookEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            webhookReceivedStore.storeSuccessfulDelivery(webhookEventRecord, teamsDestinationChannel, destinationUrl);
        }else{
            webhookReceivedStore.storeUnsuccessfulDelivery(webhookEventRecord, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, GithubPolledEventRecord polledEventRecord) {
        String destinationUrl = configurationProvider.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            githubPolledStore.storeSuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        }else{
            githubPolledStore.storeUnsuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, GithubPolledEventRecord polledEventRecord) {
        String destinationUrl = configurationProvider.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            githubPolledStore.storeSuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        }else{
            githubPolledStore.storeUnsuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, WebhookEventRecord webhookEvent) {
        String destinationUrl = configurationProvider.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(webhookEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            webhookReceivedStore.storeSuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }else{
            webhookReceivedStore.storeUnsuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }
    }

    private HttpResponseDto deliverToDiscord(WebhookEventRecord webhookEventRecord, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventRecord);
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToDiscord(GithubPolledEventRecord polledEventRecord, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord);
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToTeams(GithubPolledEventRecord polledEventRecord, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord);
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(WebhookEventRecord webhookEventRecord, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventRecord);
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
