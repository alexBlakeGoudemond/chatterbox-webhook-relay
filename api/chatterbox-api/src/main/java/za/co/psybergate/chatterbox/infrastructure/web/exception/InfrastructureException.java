package za.co.psybergate.chatterbox.infrastructure.web.exception;

/// [InfrastructureException] represents:
///
/// - downstream failure
/// - serialization failure
/// - Unauthorized
/// - unexpected system state
///
/// i.e. server faults, not client faults
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }

}
