package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookService {

    void process(String eventType, JsonNode rawBody);

    String getRepositoryName(JsonNode rawBody) throws BadRequestException;

}
