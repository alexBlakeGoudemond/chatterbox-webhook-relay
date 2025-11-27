package za.co.psybergate.chatterbox.application.core.exception;

public class ApplicationException extends RuntimeException {

    public ApplicationException(String failedToCalculateHmac, Exception e) {
        super(failedToCalculateHmac, e);
    }

}
