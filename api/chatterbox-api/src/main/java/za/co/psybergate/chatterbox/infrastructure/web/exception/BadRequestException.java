package za.co.psybergate.chatterbox.infrastructure.web.exception;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(message);
    }

}
