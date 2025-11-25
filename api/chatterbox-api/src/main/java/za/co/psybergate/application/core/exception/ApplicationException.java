package za.co.psybergate.application.core.exception;

public class ApplicationException extends RuntimeException {

    public ApplicationException(String failedToCalculateHmac, Exception e) {
        super(failedToCalculateHmac, e);
    }

}
