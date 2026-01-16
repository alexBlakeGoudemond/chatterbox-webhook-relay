package za.co.psybergate.chatterbox.infrastructure.exception;

public class InternalServerException extends InfrastructureException {

    public InternalServerException(String message, Exception e) {
        super(message, e);
    }

}
