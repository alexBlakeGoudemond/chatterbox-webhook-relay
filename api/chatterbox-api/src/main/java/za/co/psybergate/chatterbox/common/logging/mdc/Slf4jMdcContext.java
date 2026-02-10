package za.co.psybergate.chatterbox.common.logging.mdc;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.MdcContext;

import java.util.UUID;

import static za.co.psybergate.chatterbox.application.common.logging.MDC_KEYS.REPOSITORY_NAME;
import static za.co.psybergate.chatterbox.application.common.logging.MDC_KEYS.THREAD_EXECUTION_ID;

/**
 * Utility class to centralize MDC (Mapped Diagnostic Context) variables managed in the map.
 */
@Component
public class Slf4jMdcContext implements MdcContext {

    /**
     * Initializes the MDC context with a unique execution ID.
     */
    @Override
    public void initialize() {
        String threadExecutionId = UUID.randomUUID().toString();
        MDC.put(THREAD_EXECUTION_ID.value(), threadExecutionId);
    }

    /**
     * Sets the repository name in the MDC context.
     *
     * @param repositoryName the name of the repository
     */
    @Override
    public void setRepositoryName(String repositoryName) {
        if (repositoryName != null) {
            MDC.put(REPOSITORY_NAME.value(), repositoryName);
        }
    }

    /**
     * Clears the MDC context.
     */
    @Override
    public void clear() {
        MDC.clear();
    }

}
