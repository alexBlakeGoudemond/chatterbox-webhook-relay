package za.co.psybergate.chatterbox.infrastructure.exception;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(message);
    }

}
