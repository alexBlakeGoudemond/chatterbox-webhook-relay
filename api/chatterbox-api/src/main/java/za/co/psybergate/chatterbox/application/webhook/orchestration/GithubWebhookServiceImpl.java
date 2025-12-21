package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingService;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidator;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventExtractorImpl eventExtractor;

    private final WebhookLogger webhookLogger;

    private final TeamsSenderServiceImpl teamsSenderService;

    private final JsonConverter jsonConverter;

    private final GithubPollingService githubPollingService;

    @Override
    public void process(String eventType, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);

        GithubEventDto eventDto = eventExtractor.extract(eventType, rawBody);
        webhookLogger.logWebhookReceived(eventDto);
        webhookLogger.logSendingDtoToTeams(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        webhookLogger.logTeamsResponse(httpResponseDto);
    }

    @Override
    public void pollGithubForChanges(String repositoryName, LocalDateTime lastReceivedTime) {
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        GithubRepositoryInformationDto recentUpdates = githubPollingService.getRecentUpdates(repositoryName, lastReceivedTime);
        System.out.println("recentUpdates = " + recentUpdates);
        throw new ApplicationException("Not yet finished - need to convert updates into list of GithubEventDto and send");
    }

}
