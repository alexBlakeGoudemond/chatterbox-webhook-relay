package com.webhook.relay.architecture_rules.definition;

/**
 * Helper utility for defining fully qualified class names of commonly used types in architecture rules.
 */
public class KnownTypesHelper {

    /**
     * Fully qualified name for the Jakarta Transactional annotation.
     *
     * @return the Jakarta Transactional class name
     */
    public static String jakartaTransactionalAnnotation() {
        return "jakarta.transaction.Transactional";
    }

    /**
     * Fully qualified name for the Spring Transactional annotation.
     *
     * @return the Spring Transactional class name
     */
    public static String springTransactionalAnnotation() {
        return "org.springframework.transaction.annotation.Transactional";
    }

}
