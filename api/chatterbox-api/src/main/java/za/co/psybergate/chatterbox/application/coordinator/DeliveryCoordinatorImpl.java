package za.co.psybergate.chatterbox.application.coordinator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

@Component
@RequiredArgsConstructor
public class DeliveryCoordinatorImpl implements DeliveryCoordinator {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderServiceImpl teamsSenderService;

    @Override
    public void handleTeamsDeliveries() {
        // TODO BlakeGoudemond 2026/01/01 | fetch from DB - Teams work to process
        // TODO BlakeGoudemond 2026/01/01 | foreach event, identify teamsDestination
        // TODO BlakeGoudemond 2026/01/01 | deliver to teams
    }

    private HttpResponseDto deliverToTeams(GithubEventDto eventDto, String teamsDestination) {
        webhookLogger.logWebhookReceived(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto, teamsDestination);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

}
