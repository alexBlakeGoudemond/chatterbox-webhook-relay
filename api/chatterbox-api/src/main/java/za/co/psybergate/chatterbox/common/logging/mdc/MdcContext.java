package za.co.psybergate.chatterbox.common.logging.mdc;

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
        String threadExecutionId = UUID.randomUUID().toString();
        setThreadExecutionId(threadExecutionId);
    }

    /**
     * Sets the Thread Execution name in the MDC context.
     *
     * @param threadExecutionId the unique identifier of the Thread
     */
    public static void setThreadExecutionId(String threadExecutionId) {
        MDC.put(THREAD_EXECUTION_ID.value(), threadExecutionId);
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

    public static String getThreadId(){
        return MDC.get(THREAD_EXECUTION_ID.value());
    }

    /**
     * Clears the MDC context.
     */
    public static void clear() {
        MDC.clear();
    }

}
