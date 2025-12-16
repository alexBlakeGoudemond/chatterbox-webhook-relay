package za.co.psybergate.chatterbox.application.exception;

/// [ApplicationException] represents valid HTTP, but invalid for the application
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message, Exception e) {
        super(message, e);
    }

    public ApplicationException(String message) {
        super(message);
    }

}
