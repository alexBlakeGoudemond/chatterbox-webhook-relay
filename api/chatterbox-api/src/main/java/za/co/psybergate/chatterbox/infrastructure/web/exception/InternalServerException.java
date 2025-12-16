package za.co.psybergate.chatterbox.infrastructure.web.exception;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;

public class InternalServerException extends ApplicationException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

}
