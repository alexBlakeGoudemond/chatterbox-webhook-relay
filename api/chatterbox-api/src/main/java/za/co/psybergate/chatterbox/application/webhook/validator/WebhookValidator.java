package za.co.psybergate.chatterbox.application.webhook.validator;

import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookValidator {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

}
