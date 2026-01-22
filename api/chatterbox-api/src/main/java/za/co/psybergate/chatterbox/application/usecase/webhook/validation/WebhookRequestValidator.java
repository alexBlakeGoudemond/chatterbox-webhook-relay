package za.co.psybergate.chatterbox.application.usecase.webhook.validation;

import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;

/// Entry point, validates and normalizes input
public interface WebhookRequestValidator {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String owner, String repositoryName) throws UnrecognizedRequestException;

}
