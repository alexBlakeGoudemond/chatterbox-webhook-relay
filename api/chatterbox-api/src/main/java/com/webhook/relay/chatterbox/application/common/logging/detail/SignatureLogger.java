package com.webhook.relay.chatterbox.application.common.logging.detail;

public interface SignatureLogger {

    void logMissingSignature();

    void logInvalidSignature(String expected, String received);

    void logValidSignature();

}
