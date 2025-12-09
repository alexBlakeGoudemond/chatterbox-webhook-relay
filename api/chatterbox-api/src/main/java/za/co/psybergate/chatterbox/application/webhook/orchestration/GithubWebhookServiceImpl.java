package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.teams.sending.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidator;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

@Service
@RequiredArgsConstructor
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventExtractorImpl eventExtractor;

    private final WebhookLogger webhookLogger;

    private final TeamsSenderServiceImpl teamsSenderService;

    @Override
    public void process(String eventType, JsonNode rawBody) {
        String repositoryName = getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);

        GithubEventDto eventDto = eventExtractor.extract(eventType, rawBody);
        webhookLogger.logWebhookReceived(eventDto);
        teamsSenderService.process(eventDto);
    }

    @Override
    public String getRepositoryName(JsonNode rawBody) throws BadRequestException {
        String repositoryName = rawBody.path("repository").path("full_name").asText(null);
        if (repositoryName == null) {
            throw new BadRequestException("Unable to parse 'repository.full_name' from raw body");
        }
        return repositoryName;
    }

}
