package za.co.psybergate.chatterbox.application.usecase.event.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.discord.delivery.DiscordSenderService;
import za.co.psybergate.chatterbox.application.usecase.webhook.resolution.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.github.model.GithubDestinationMapping;
import za.co.psybergate.chatterbox.application.usecase.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStore;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStore;
import za.co.psybergate.chatterbox.domain.event.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.event.dto.WebhookEventDto;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderService;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventProcessorServiceImpl implements EventProcessorService {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderService teamsSenderService;

    private final DiscordSenderService discordSenderService;

    private final WebhookConfigurationResolver webhookConfigurationResolver;

    private final GithubPolledEventStore githubPolledEventStore;

    private final WebhookEventStore webhookEventStore;

    @Override
    public void processWebhookEvents() {
        List<GithubDestinationMapping> destinationMappings = webhookConfigurationResolver.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processWebhookEvents(destinationMapping);
        }
    }

    @Override
    public void processPolledEvents() {
        List<GithubDestinationMapping> destinationMappings = webhookConfigurationResolver.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processPolledEvents(destinationMapping);
        }
    }

    private void processWebhookEvents(GithubDestinationMapping destinationMapping) {
        for (WebhookEventDto webhookEventDto : webhookEventStore.getUnprocessedWebhooks(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), webhookEventDto);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), webhookEventDto);
            webhookEventStore.setProcessedStatus(webhookEventDto, EventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(GithubDestinationMapping destinationMapping) {
        for (GithubPolledEventDto latestEventRecord : githubPolledEventStore.getUnprocessedEvents(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), latestEventRecord);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), latestEventRecord);
            githubPolledEventStore.setProcessedStatus(latestEventRecord, EventStatus.PROCESSED_SUCCESS);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookEventDto webhookEventDto) {
        String destinationUrl = webhookConfigurationResolver.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(webhookEventDto, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            webhookEventStore.storeSuccessfulDelivery(webhookEventDto, teamsDestinationChannel, destinationUrl);
        } else {
            webhookEventStore.storeUnsuccessfulDelivery(webhookEventDto, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, GithubPolledEventDto polledEventRecord) {
        String destinationUrl = webhookConfigurationResolver.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            githubPolledEventStore.storeSuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        } else {
            githubPolledEventStore.storeUnsuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, GithubPolledEventDto polledEventRecord) {
        String destinationUrl = webhookConfigurationResolver.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            githubPolledEventStore.storeSuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        } else {
            githubPolledEventStore.storeUnsuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, WebhookEventDto webhookEvent) {
        String destinationUrl = webhookConfigurationResolver.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(webhookEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            webhookEventStore.storeSuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        } else {
            webhookEventStore.storeUnsuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }
    }

    private HttpResponseDto deliverToDiscord(WebhookEventDto webhookEventDto, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventDto.eventType(), webhookEventDto.displayName(), webhookEventDto.repositoryFullName(), webhookEventDto.senderName(), webhookEventDto.eventUrl(), webhookEventDto.eventUrlDisplayText(), webhookEventDto.extraDetail());
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToDiscord(GithubPolledEventDto polledEventRecord, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord.eventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToTeams(GithubPolledEventDto polledEventRecord, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord.eventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(WebhookEventDto webhookEventDto, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventDto.eventType(), webhookEventDto.displayName(), webhookEventDto.repositoryFullName(), webhookEventDto.senderName(), webhookEventDto.eventUrl(), webhookEventDto.eventUrlDisplayText(), webhookEventDto.extraDetail());
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
