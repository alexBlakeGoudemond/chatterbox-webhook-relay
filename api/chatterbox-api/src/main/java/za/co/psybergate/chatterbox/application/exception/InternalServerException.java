package za.co.psybergate.chatterbox.application.exception;

public class InternalServerException extends ApplicationException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

    public InternalServerException(String message) {
        super(message);
    }

}
