package za.co.psybergate.chatterbox.infrastructure.exception;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(message);
    }

}
