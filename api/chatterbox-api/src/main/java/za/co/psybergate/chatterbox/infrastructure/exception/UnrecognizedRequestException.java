package za.co.psybergate.chatterbox.infrastructure.exception;

public class UnrecognizedRequestException extends ApplicationException {

    public UnrecognizedRequestException(String message) {
        super(message);
    }

}
