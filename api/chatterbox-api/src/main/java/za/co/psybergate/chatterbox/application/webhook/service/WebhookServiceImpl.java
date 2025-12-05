package za.co.psybergate.chatterbox.application.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final ChatterboxConfigurationProperties configurationProperties;

    @Override
    public void process(String eventType, JsonNode rawBody) {
        String repositoryName = getRepositoryName(rawBody);
        assertAcceptedRepository(repositoryName);
        assertAcceptedEvent(eventType);
        // TODO BlakeGoudemond 2025/12/04 | use this information to
        //  - Prepare a Payload for MS Teams
        //  - Send the Payload to MS Teams
        log.warn("Github Webhook received by Github API");
    }

    @Override
    public void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException {
        if (configurationProperties.containsEvent(eventType)) {
            return;
        }
        log.debug("No ConfigurationProperties Found for eventType: {}", eventType);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized event '%s'", eventType);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException {
        if (configurationProperties.acceptsRepository(repositoryName)) {
            return;
        }
        log.debug("Repository '{}' is not whitelisted as an accepted repository", repositoryName);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized repository '%s'", repositoryName);
        throw new UnrecognizedRequestException(responseContent);
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
