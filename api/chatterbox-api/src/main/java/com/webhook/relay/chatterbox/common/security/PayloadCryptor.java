package com.webhook.relay.chatterbox.common.security;

import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
