package za.co.psybergate.chatterbox.application.core.exception;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message, Exception e) {
        super(message, e);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

}
