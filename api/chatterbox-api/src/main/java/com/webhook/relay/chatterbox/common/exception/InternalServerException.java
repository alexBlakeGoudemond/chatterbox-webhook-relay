package com.webhook.relay.chatterbox.common.exception;

public class InternalServerException extends InfrastructureException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

}
