package com.webhook.relay.chatterbox.application.port.in.validation;

import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;

/// Entry point, validates and normalizes input
public interface WebhookRequestValidatorPort {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String owner, String repositoryName) throws UnrecognizedRequestException;

}
