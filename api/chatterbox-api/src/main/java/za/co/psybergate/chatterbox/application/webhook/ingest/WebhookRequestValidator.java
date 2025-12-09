package za.co.psybergate.chatterbox.application.webhook.ingest;

import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

/// Entry point, validates and normalizes input
public interface WebhookRequestValidator {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

}
