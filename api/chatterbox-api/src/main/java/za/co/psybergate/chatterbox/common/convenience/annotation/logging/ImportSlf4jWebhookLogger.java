package za.co.psybergate.chatterbox.common.convenience.annotation.logging;

import org.springframework.context.annotation.Import;
import za.co.psybergate.chatterbox.application.common.logging.slf4j.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
        Slf4jWebhookLogger.class,
        Slf4jSignatureLogger.class,
        Slf4jWebhookEventLogger.class,
        Slf4jValidationLogger.class,
        Slf4jDeliveryLogger.class,
        Slf4jStorageLogger.class,
        Slf4jPollingLogger.class,
        Slf4jProcessingLogger.class,
        Slf4jExceptionLogger.class,
        Slf4jOrchestrationLogger.class
})
public @interface ImportSlf4jWebhookLogger {

}
