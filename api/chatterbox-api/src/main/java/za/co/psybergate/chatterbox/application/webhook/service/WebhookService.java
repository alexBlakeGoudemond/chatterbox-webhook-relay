package za.co.psybergate.chatterbox.application.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;

public interface WebhookService {

    void process(String eventType, JsonNode rawBody);

    String getRepositoryName(JsonNode rawBody) throws BadRequestException;

}
