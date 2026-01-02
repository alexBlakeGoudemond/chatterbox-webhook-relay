package za.co.psybergate.chatterbox.application.coordinator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties.DestinationMapping;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryCoordinatorImpl implements DeliveryCoordinator {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderServiceImpl teamsSenderService;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    private final GithubPolledStore githubPolledStore;

    private final WebhookReceivedStore webhookReceivedStore;

    @Override
    public void processWebhookEvents() {
        List<DestinationMapping> destinationMappings = repositoryProperties.getDestinationMapping();
        for (DestinationMapping destinationMapping : destinationMappings) {
            processWebhookEvents(destinationMapping);
        }
    }

    private void processWebhookEvents(DestinationMapping destinationMapping) {
        for (WebhookEvent webhookEvent : webhookReceivedStore.getLatestWebhooks(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), webhookEvent);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, WebhookEvent webhookEvent) {
        HttpResponseDto httpResponseDto = deliverToTeams(webhookEvent, teamsDestinationChannel);
        if (httpResponseDto.httpStatus() == HttpStatus.OK.value()) {
            String destinationUrl = destinationTeamsProperties.getUrl(teamsDestinationChannel);
            webhookReceivedStore.storeDelivery(webhookEvent, teamsDestinationChannel, destinationUrl);
            webhookReceivedStore.setProcessedStatus(webhookEvent, EventStatus.PROCESSED_SUCCESS);
        }else{
            webhookReceivedStore.setProcessedStatus(webhookEvent, EventStatus.PROCESSED_FAILURE, httpResponseDto.rawBody());
        }
    }

    @Override
    public void processPolledEvents() {
        List<DestinationMapping> destinationMappings = repositoryProperties.getDestinationMapping();
        for (DestinationMapping destinationMapping : destinationMappings) {
            processPolledEvents(destinationMapping);
        }
    }

    private void processPolledEvents(DestinationMapping destinationMapping) {
        for (GithubPolledEvent latestEvent : githubPolledStore.getLatestEvents(destinationMapping.getName())) {
            deliverToTeams(destinationMapping.getTeamsDestinationChannel(), latestEvent);
        }

    }

    @SuppressWarnings("DuplicatedCode")
    private void deliverToTeams(String teamsDestinationChannel, GithubPolledEvent polledEvent) {
        HttpResponseDto httpResponseDto = deliverToTeams(polledEvent, teamsDestinationChannel);
        if (httpResponseDto.httpStatus() == HttpStatus.OK.value()) {
            String destinationUrl = destinationTeamsProperties.getUrl(teamsDestinationChannel);
            githubPolledStore.storeDelivery(polledEvent, teamsDestinationChannel, destinationUrl);
            githubPolledStore.setProcessedStatus(polledEvent, EventStatus.PROCESSED_SUCCESS);
        }else{
            githubPolledStore.setProcessedStatus(polledEvent, EventStatus.PROCESSED_FAILURE, httpResponseDto.rawBody());
        }
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
        webhookLogger.logWebhookReceived(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto, teamsDestination);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

}
