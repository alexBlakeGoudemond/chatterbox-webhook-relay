package com.webhook.relay.architecture_rules.definition;

/**
 * The packages defined here are the recommended package structure for a hexagonal architecture.
 *
 * <pre>
 * {@code
 *  |-- <domainName>/
 *      |
 *      |-- adapter/
 *      |   |
 *      |   |-- in/
 *      |   |   |-- .../
 *      |   |
 *      |   |-- out/
 *      |       |-- .../
 *      |       |-- persistence/
 *      |
 *      |-- application/
 *      |   |
 *      |   |-- common/
 *      |   |   |-- .../
 *      |   |
 *      |   |-- domain/
 *      |   |   |-- .../
 *      |   |
 *      |   |-- port/
 *      |   |   |-- in/
 *      |   |   |   |-- .../
 *      |   |   |-- out/
 *      |   |       |-- .../
 *      |   |
 *      |   |-- usecase/
 *      |       |-- .../
 *      |
 *      |-- common/
 *          |-- .../
 * }
 * </pre>
 *
 */
public class HexagonalArchitectureKnownPackages {

    /**
     * Any of these types may be imported for use and should be supported immediately, in all packages
     *
     */
    public static String javaPackage = "java..";

    /**
     * Top level package expected
     *
     */
    public static String adapterPackage = "..adapter..";

    /**
     * Top level package expected
     *
     */
    public static String applicationPackage = "..application..";

    /**
     * Top level package expected
     *
     */
    public static String commonPackage = "..common..";

    /**
     * Types for incoming requests go here.
     * Examples would include:
     * <ul>
     *     <li>Implementations of {@link HexagonalArchitectureKnownPackages#portInboundPackage}</li>
     *     <li>Controllers</li>
     *     <li>Web Filters</li>
     *     <li>Controller Exception Handlers</li>
     *     <li>Actuators</li>
     * </ul>
     *
     */
    public static String adapterInboundPackage = "..adapter.in..";

    /**
     * Types for outgoing requests go here.
     * Examples would include:
     * <ul>
     *     <li>Implementations of {@link HexagonalArchitectureKnownPackages#portOutboundPackage}</li>
     *     <li>Persistence</li>
     *     <li>Delivery Components (eg. HttpPost)</li>
     *     <li>File Resolution (eg. exposing properties)</li>
     * </ul>
     *
     */
    public static String adapterOutboundPackage = "..adapter.out..";

    /**
     * Agnostic, Isolated core types. These types should not have any implementation details - they should be
     * broad and contain Ubiquitous Language. Whatever the Product Owner needs to understand should be here;
     * implementations of the domain types would go in the adapter package.
     *
     */
    public static String applicationDomainPackage = "..application.domain..";

    /**
     * Any types that are used/referenced that are not strictly defined as an inbound or outbound port may
     * go here
     * <p>
     * Examples would include:
     * <ul>
     *     <li>Application Exception</li>
     * </ul>
     *
     */
    public static String applicationCommonPackage = "..application.common..";

    /**
     * Any implementations of {@link HexagonalArchitectureKnownPackages#portInboundPackage}
     * that are designed to NOT be replaceable may go here. I.e. instead of being placed in
     * {@link HexagonalArchitectureKnownPackages#adapterInboundPackage}
     *
     */
    public static String applicationUseCasePackage = "..application.usecase..";

    /**
     * Contracts (Interfaces) for incoming work are defined here
     *
     */
    public static String portInboundPackage = "..application.port.in..";

    /**
     * Contracts (Interfaces) for outgoing work are defined here
     *
     */
    public static String portOutboundPackage = "..application.port.out..";

    /**
     * Persistence types, by design, are implementations and could be swapped out.
     * So they MUST be placed in {@link HexagonalArchitectureKnownPackages#adapterOutboundPackage}
     * <p>
     * Examples of types that would be placed here
     * <ul>
     *     <li>{@link KnownPackagesHelper#persistencePackages()}</li>
     * </ul>
     *
     */
    public static String adapterOutboundPersistencePackage = "..adapter.out.persistence..";

}
