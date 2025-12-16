package za.co.psybergate.chatterbox.infrastructure.web.exception;

public class InternalServerException extends InfrastructureException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

}
