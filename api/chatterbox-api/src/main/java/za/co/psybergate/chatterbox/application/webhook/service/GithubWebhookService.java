package za.co.psybergate.chatterbox.application.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.webhook.extractor.GithubEventExtractor;
import za.co.psybergate.chatterbox.application.webhook.validator.WebhookValidator;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

@Service
@RequiredArgsConstructor
public class GithubWebhookService implements WebhookService {

    private final WebhookValidator webhookValidator;

    private final GithubEventExtractor eventExtractor;

    private final WebhookLogger webhookLogger;

    private final TeamsSenderService teamsSenderService;

    @Override
    public void process(String eventType, JsonNode rawBody) {
        String repositoryName = getRepositoryName(rawBody);
        webhookValidator.assertAcceptedRepository(repositoryName);
        webhookValidator.assertAcceptedEvent(eventType);

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
