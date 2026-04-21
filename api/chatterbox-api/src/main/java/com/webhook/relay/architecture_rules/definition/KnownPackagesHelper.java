package com.webhook.relay.architecture_rules.definition;

import java.util.Arrays;
import java.util.List;

/**
 * Helper utility for defining package patterns commonly used in architecture rules.
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class KnownPackagesHelper {

    /**
     * Package pattern for Spring Framework classes.
     *
     * @return a list containing the Spring package pattern
     */
    public static List<String> springPackages(){
        return Arrays.asList("org.springframework..");
    }

    /**
     * Package pattern for Lombok classes.
     *
     * @return a list containing the Lombok package pattern
     */
    public static List<String> lombokPackages(){
        return Arrays.asList("lombok..");
    }

    /**
     * Package pattern for Jackson (JSON) classes.
     *
     * @return a list containing the Jackson package pattern
     */
    public static List<String> jacksonPackages(){
        return Arrays.asList("com.fasterxml.jackson..");
    }

    /**
     * Package pattern for Jakarta Validation classes.
     *
     * @return a list containing the Jakarta Validation package pattern
     */
    public static List<String> jakartaValidationPackages(){
        return Arrays.asList("jakarta.validation..");
    }

    /**
     * Package pattern for Jakarta Servlet classes.
     *
     * @return a list containing the Jakarta Servlet package pattern
     */
    public static List<String> jakartaServletPackages(){
        return Arrays.asList("jakarta.servlet..");
    }

    /**
     * Package pattern for SLF4J logging classes.
     *
     * @return a list containing the SLF4J package pattern
     */
    public static List<String> slf4jPackages(){
        return Arrays.asList("org.slf4j..");
    }

    /**
     * Package patterns for Apache HTTP Client classes.
     *
     * @return a list containing Apache HTTP package patterns
     */
    public static List<String> apacheHttpPackages(){
        return Arrays.asList("org.apache.hc.client5.http..", "org.apache.hc.core5.http..");
    }

    /**
     * Defines package patterns for persistence and ORM infrastructure.
     * <p>
     * This includes the Jakarta Persistence API (JPA) and vendor implementations
     * such as Hibernate. Typical types in these packages include {@code EntityManager},
     * {@code @Entity}, and Hibernate-specific classes for managing database
     * entities and queries.
     * <p>
     * Commonly used to implement repositories, data access layers, and
     * transactional operations in the application.
     *
     * @return a list of package patterns covering persistence-related types
     */
    public static List<String> persistencePackages(){
        return Arrays.asList("jakarta.persistence..", "org.hibernate..");
    }

    /**
     * Defines package patterns for reactive programming infrastructure based on Project Reactor.
     * <p>
     * This includes the Reactor Core library, which provides the fundamental reactive types
     * such as {@code Mono} and {@code Flux}. These types represent asynchronous, non-blocking
     * computations and data streams, and are commonly used by Spring WebFlux and other
     * reactive components.
     * <p>
     * Often used in asynchronous workflows, such as performing HTTP requests or
     * other non-blocking operations.
     *
     * @return a list of package patterns covering Reactor Core reactive types
     */
    public static List<String> asyncReactivePackages(){
        return Arrays.asList("reactor.core..");
    }

    /**
     * Package patterns for Micrometer metrics classes.
     *
     * @return a list containing Micrometer package patterns
     */
    public static List<String> micrometerMetricPackages(){
        return Arrays.asList("io.micrometer..");
    }

    /**
     * Package pattern for Java Cryptography Extension (JCE) classes.
     *
     * @return a list containing JCE package patterns
     */
    public static List<String> javaCryptorPackages(){
        return Arrays.asList("javax.crypto..");
    }

}
