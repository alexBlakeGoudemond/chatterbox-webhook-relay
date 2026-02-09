package za.co.psybergate.chatterbox.application.common.logging;

import org.slf4j.MDC;

import java.util.UUID;

import static za.co.psybergate.chatterbox.application.common.logging.MDC_KEYS.REPOSITORY_NAME;
import static za.co.psybergate.chatterbox.application.common.logging.MDC_KEYS.THREAD_EXECUTION_ID;

/**
 * Utility class to centralize MDC (Mapped Diagnostic Context) variables managed in the map.
 */
public class MdcContext {

    private MdcContext() {
        // Prevent instantiation
    }

    /**
     * Initializes the MDC context with a unique execution ID.
     */
    public static void initialize() {
        MDC.put(THREAD_EXECUTION_ID.value(), UUID.randomUUID().toString());
    }

    /**
     * Sets the repository name in the MDC context.
     *
     * @param repositoryName the name of the repository
     */
    public static void setRepositoryName(String repositoryName) {
        if (repositoryName != null) {
            MDC.put(REPOSITORY_NAME.value(), repositoryName);
        }
    }

    /**
     * Clears the MDC context.
     */
    public static void clear() {
        MDC.clear();
    }

}
