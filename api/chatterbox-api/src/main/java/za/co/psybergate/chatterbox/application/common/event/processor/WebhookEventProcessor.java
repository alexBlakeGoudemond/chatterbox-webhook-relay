package za.co.psybergate.chatterbox.application.common.event.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.port.out.discord.delivery.DiscordSenderPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.teams.delivery.TeamsSenderPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubDestinationMapping;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookEventProcessor implements EventProcessor {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderPort teamsSenderPort;

    private final DiscordSenderPort discordSenderPort;

    private final WebhookConfigurationResolverPort webhookConfigurationResolverPort;

    private final GithubPolledEventStorePort githubPolledEventStorePort;

    private final WebhookEventStorePort webhookEventStorePort;

    @Override
    public void processWebhookEvents() {
        List<GithubDestinationMapping> destinationMappings = webhookConfigurationResolverPort.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processWebhookEvents(destinationMapping);
        }
    }

    @Override
    public void processPolledEvents() {
        List<GithubDestinationMapping> destinationMappings = webhookConfigurationResolverPort.getDestinationMapping();
        for (GithubDestinationMapping destinationMapping : destinationMappings) {
            webhookLogger.logProcessingEvents(destinationMapping);
            processPolledEvents(destinationMapping);
        }
    }

    private void processWebhookEvents(GithubDestinationMapping destinationMapping) {
        for (WebhookEventReceivedDto webhookEventReceivedDto : webhookEventStorePort.getUnprocessedWebhooks(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), webhookEventReceivedDto);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), webhookEventReceivedDto);
            webhookEventStorePort.setProcessedStatus(webhookEventReceivedDto, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(GithubDestinationMapping destinationMapping) {
        for (WebhookPolledEventReceivedDto latestEventRecord : githubPolledEventStorePort.getUnprocessedEvents(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), latestEventRecord);
            deliverToDiscord(destinationMapping.getDiscordDestinationChannel(), latestEventRecord);
            githubPolledEventStorePort.setProcessedStatus(latestEventRecord, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookEventReceivedDto webhookEventReceivedDto) {
        String destinationUrl = webhookConfigurationResolverPort.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(webhookEventReceivedDto, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            webhookEventStorePort.storeSuccessfulDelivery(webhookEventReceivedDto, teamsDestinationChannel, destinationUrl);
        } else {
            webhookEventStorePort.storeUnsuccessfulDelivery(webhookEventReceivedDto, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookPolledEventReceivedDto polledEventRecord) {
        String destinationUrl = webhookConfigurationResolverPort.getTeamsUrl(teamsDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToTeams(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.ACCEPTED.value()) {
            githubPolledEventStorePort.storeSuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        } else {
            githubPolledEventStorePort.storeUnsuccessfulDelivery(polledEventRecord, teamsDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, WebhookPolledEventReceivedDto polledEventRecord) {
        String destinationUrl = webhookConfigurationResolverPort.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(polledEventRecord, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            githubPolledEventStorePort.storeSuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        } else {
            githubPolledEventStorePort.storeUnsuccessfulDelivery(polledEventRecord, discordDestinationChannel, destinationUrl);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToDiscord(String discordDestinationChannel, WebhookEventReceivedDto webhookEvent) {
        String destinationUrl = webhookConfigurationResolverPort.getDiscordUrl(discordDestinationChannel);
        HttpResponseDto httpResponseDto = deliverToDiscord(webhookEvent, destinationUrl);
        if (httpResponseDto.httpStatus() == HttpStatus.NO_CONTENT.value()) {
            webhookEventStorePort.storeSuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        } else {
            webhookEventStorePort.storeUnsuccessfulDelivery(webhookEvent, discordDestinationChannel, destinationUrl);
        }
    }

    private HttpResponseDto deliverToDiscord(WebhookEventReceivedDto webhookEventReceivedDto, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventReceivedDto.webhookEventType(), webhookEventReceivedDto.displayName(), webhookEventReceivedDto.repositoryFullName(), webhookEventReceivedDto.senderName(), webhookEventReceivedDto.eventUrl(), webhookEventReceivedDto.eventUrlDisplayText(), webhookEventReceivedDto.extraDetail());
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToDiscord(WebhookPolledEventReceivedDto polledEventRecord, String discordDestinationUrl) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord.webhookEventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
        return deliverToDiscord(eventDto, discordDestinationUrl);
    }

    private HttpResponseDto deliverToTeams(WebhookPolledEventReceivedDto polledEventRecord, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(polledEventRecord.webhookEventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(WebhookEventReceivedDto webhookEventReceivedDto, String teamsDestinationChannel) {
        GithubEventDto eventDto = new GithubEventDto(webhookEventReceivedDto.webhookEventType(), webhookEventReceivedDto.displayName(), webhookEventReceivedDto.repositoryFullName(), webhookEventReceivedDto.senderName(), webhookEventReceivedDto.eventUrl(), webhookEventReceivedDto.eventUrlDisplayText(), webhookEventReceivedDto.extraDetail());
        return deliverToTeams(eventDto, teamsDestinationChannel);
    }

    private HttpResponseDto deliverToTeams(GithubEventDto eventDto, String teamsDestination) {
        webhookLogger.logEventReceived(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderPort.process(eventDto, teamsDestination);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

    private HttpResponseDto deliverToDiscord(GithubEventDto eventDto, String discordDestinationUrl) {
        webhookLogger.logEventReceived(eventDto);
        HttpResponseDto httpResponseDto = discordSenderPort.process(eventDto, discordDestinationUrl);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

}
