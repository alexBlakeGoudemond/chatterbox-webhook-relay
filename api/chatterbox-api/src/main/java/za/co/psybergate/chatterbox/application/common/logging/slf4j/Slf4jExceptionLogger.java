package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.ExceptionLogger;

@Slf4j
@Component
public class Slf4jExceptionLogger implements ExceptionLogger {

    @Override
    public void logExceptionDetails(Exception exception) {
        log.error("[Exception] Exception encountered: {}", exception.getClass().getSimpleName(), exception);
    }
}
