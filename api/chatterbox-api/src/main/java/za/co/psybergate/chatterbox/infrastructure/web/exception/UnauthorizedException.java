package za.co.psybergate.chatterbox.infrastructure.web.exception;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(message);
    }

}
