package za.co.psybergate.chatterbox.application.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookService {

    void process(String eventType, JsonNode rawBody);

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

    String getRepositoryName(JsonNode rawBody) throws BadRequestException;

}
