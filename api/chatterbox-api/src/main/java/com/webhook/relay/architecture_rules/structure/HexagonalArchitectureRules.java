package com.webhook.relay.architecture_rules.structure;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import com.webhook.relay.architecture_rules.arch.condition.HaveAtLeastOneTest;
import com.webhook.relay.architecture_rules.arch.condition.HaveImplementationInAllowedPackages;
import com.webhook.relay.architecture_rules.arch.condition.ImplementTypeFoundInPackages;
import com.webhook.relay.architecture_rules.arch.condition.NotImplementTypesFoundInPackages;
import com.webhook.relay.architecture_rules.arch.rule.ClassPredicates;
import com.webhook.relay.architecture_rules.definition.KnownPackagesHelper;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.webhook.relay.architecture_rules.definition.HexagonalArchitectureKnownPackages.*;

/**
 * Comprehensive set of ArchUnit rules for enforcing Hexagonal (Ports and Adapters) Architecture.
 * <p>
 * This class provides rules to ensure strict separation between the domain, application,
 * and adapter layers, and to enforce naming and placement conventions for ports and adapters.
 * </p>
 */
public class HexagonalArchitectureRules {

    /**
     * Creates {@link ArchRule} that checks that the adapter.in only depends on classes
     * in the given list of packages.
     * <p>
     * By default, adapter.in classes may only depend on:
     * <ul>
     *     <li>..adapter.in..</li>
     *     <li>..common..</li>
     *     <li>..application.port.in..</li>
     *     <li>..application.common..</li>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * Extra packages may be defined as allowed for adapter.in dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     *     List.of("jakarta.servlet.http..");
     * }
     * </pre>
     *
     * @return ArchRule that checks adapter.in dependencies
     */
    public static ArchRule adapterInboundShouldOnlyDependOn(List<String> additionalPackages) {
        List<String> allowed = new ArrayList<>(additionalPackages);
        addIfMissing(allowed, commonPackage);
        addIfMissing(allowed, portInboundPackage);
        addIfMissing(allowed, applicationCommonPackage);
        addIfMissing(allowed, applicationDomainPackage);
        return packageShouldOnlyDependOn(adapterInboundPackage, allowed);
    }

    /**
     * Creates {@link ArchRule} that checks that the adapter.out only depends on classes
     * in the given list of packages.
     * <p>
     * By default, adapter.out classes may only depend on:
     * <ul>
     *     <li>..adapter.out..</li>
     *     <li>..common..</li>
     *     <li>..application.port.out..</li>
     *     <li>..application.common..</li>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * Extra packages may be defined as allowed for adapter.out dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     *     List.of("org.springframework..");
     * }
     * </pre>
     *
     * @return ArchRule that checks adapter.out dependencies
     */
    public static ArchRule adapterOutboundShouldOnlyDependOn(List<String> additionalPackages) {
        List<String> allowed = new ArrayList<>(additionalPackages);
        addIfMissing(allowed, commonPackage);
        addIfMissing(allowed, portOutboundPackage);
        addIfMissing(allowed, applicationCommonPackage);
        addIfMissing(allowed, applicationUseCasePackage);
        addIfMissing(allowed, applicationDomainPackage);
        return packageShouldOnlyDependOn(adapterOutboundPackage, allowed);
    }

    /**
     * Creates {@link ArchRule} that checks that the application only depends on classes
     * in the given list of packages.
     * <p>
     * By default, application classes may only depend on:
     * <ul>
     *     <li>..application..</li>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * Extra packages may be defined as allowed for application dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     *     List.of("org.springframework..");
     * }
     * </pre>
     *
     * @return ArchRule that checks application dependencies
     */
    public static ArchRule applicationOnlyDependsOn(List<String> additionalPackages) {
        List<String> allowed = new ArrayList<>(additionalPackages);
        addIfMissing(allowed, applicationDomainPackage);
        return packageShouldOnlyDependOn(applicationPackage, allowed);
    }

    /**
     * Creates {@link ArchRule} that checks that the domain only depends on classes
     * in the given list of packages.
     * <p>
     * By default, domain classes may only depend on:
     * <ul>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * Extra packages may be defined as allowed for domain dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     *     List.of("com.fasterxml.jackson..");
     * }
     * </pre>
     *
     * @return ArchRule that checks domain dependencies
     */
    public static ArchRule domainOnlyDependsOn(List<String> additionalPackages) {
        return packageShouldOnlyDependOn(applicationDomainPackage, additionalPackages);
    }

    /**
     * Rule that ensures all interfaces in a {@code .port.} package are named with a {@code Port} suffix.
     *
     * @return an {@link ArchRule} for port naming conventions
     */
    public static ArchRule portInterfacesNamedPort() {
        return classes()
                .that().resideInAnyPackage(portInboundPackage, portOutboundPackage)
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Port")
                .because("Port interfaces must be named *Port");
    }

    /**
     * Rule that ensures all classes with a {@code Port} suffix reside in either
     * {@code ..application.port.in..} or {@code ..application.port.out..}.
     *
     * @return an {@link ArchRule} for port placement
     */
    public static ArchRule portsExistInApplicationInOrApplicationOutPackage() {
        return classes()
                .that().haveSimpleNameEndingWith("Port")
                .should().resideInAnyPackage(portInboundPackage, portOutboundPackage)
                .because(String.format("Ports must exist only in: %s or %s", portInboundPackage, portOutboundPackage));
    }

    /**
     * Rule that ensures all classes with an {@code Adapter} suffix reside in either
     * {@code ..adapter.in..} or {@code ..adapter.out..}.
     *
     * @return an {@link ArchRule} for adapter placement
     */
    public static ArchRule adaptersExistOnlyInAdapterPackage() {
        return classes()
                .that().haveSimpleNameEndingWith("Adapter")
                .should().resideInAnyPackage(adapterInboundPackage, adapterOutboundPackage)
                .because(String.format("Ports must exist only in %s or %s", adapterInboundPackage, adapterOutboundPackage));
    }

    /**
     * Rule that prohibits the application layer from depending on the adapter layer.
     *
     * @return an {@link ArchRule} enforcing inward dependency direction from application
     */
    public static ArchRule applicationCannotDependOnAdapter() {
        return classes()
                .that().resideInAPackage(applicationPackage)
                .should().onlyDependOnClassesThat(resideOutsideOfPackage(adapterPackage))
                .because("Application layer must not depend on adapter layer");
    }

    /**
     * Rule that prohibits the domain layer from depending on the adapter layer.
     *
     * @return an {@link ArchRule} enforcing inward dependency direction from domain
     */
    public static ArchRule domainCannotDependOnAdapter() {
        return classes()
                .that().resideInAPackage(applicationDomainPackage)
                .should().onlyDependOnClassesThat(resideOutsideOfPackage(adapterPackage))
                .because("Domain layer must not depend on adapter layer");
    }

    /**
     * Rule that ensures persistence-related packages are only accessed within the adapter outbound adapter.
     *
     * @return an {@link ArchRule} restricting persistence access
     */
    public static ArchRule persistenceOnlyInAdapter() {
        String[] persistencePackages = KnownPackagesHelper.persistencePackages().toArray(new String[0]);
        return noClasses()
                .that().resideOutsideOfPackage(adapterOutboundPackage)
                .should().resideInAnyPackage(persistencePackages)
                .because("Persistence-related classes must be in adapter.out");
    }

    /**
     * Rule that prohibits Spring-specific packages from being used in the domain layer.
     *
     * @return an {@link ArchRule} ensuring a framework-independent domain
     */
    public static ArchRule noSpringInDomain() {
        String[] springPackages = KnownPackagesHelper.springPackages().toArray(new String[0]);
        return noClasses()
                .that().resideInAPackage(applicationDomainPackage)
                .should().resideInAnyPackage(springPackages)
                .because("Domain layer must not depend on Spring");
    }

    /**
     * Rule that prohibits persistence-related packages from being used in the domain layer.
     *
     * @return an {@link ArchRule} ensuring a persistence-independent domain
     */
    public static ArchRule noPersistenceInDomain() {
        String[] persistencePackages = KnownPackagesHelper.persistencePackages().toArray(new String[0]);
        return noClasses()
                .that().resideInAPackage(applicationDomainPackage)
                .should().resideInAnyPackage(persistencePackages)
                .because("Domain layer must not depend on persistence");
    }

    /**
     * Rule that ensures inbound ports are implemented in allowed packages
     * (e.g., {@code adapter.in} or {@code application.usecase}).
     *
     * @return an {@link ArchRule} for inbound port implementation placement
     */
    public static ArchRule inboundPortsImplementedInAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        addIfMissing(allowedPackages, adapterInboundPackage);
        addIfMissing(allowedPackages, applicationUseCasePackage);
        return classes()
                .that().resideInAPackage(portInboundPackage)
                .and().areInterfaces()
                .should(new HaveImplementationInAllowedPackages(allowedPackages))
                .because(String.format("Inbound ports must be implemented in %s or %s", adapterInboundPackage, applicationUseCasePackage));
    }

    /**
     * Rule that ensures outbound ports are implemented only in the {@code adapter.out} package.
     *
     * @return an {@link ArchRule} for outbound port implementation placement
     */
    public static ArchRule outboundPortsImplementedInAdapterOut() {
        List<String> allowedPackages = new ArrayList<>();
        addIfMissing(allowedPackages, adapterOutboundPackage);
        return classes()
                .that().resideInAPackage(portOutboundPackage)
                .and().areInterfaces()
                .should(new HaveImplementationInAllowedPackages(allowedPackages))
                .because(String.format("Outbound ports must be implemented in %s", adapterOutboundPackage));
    }

    /**
     * Rule that ensures classes in the {@code application.usecase} package implement at least one inbound port.
     *
     * @return an {@link ArchRule} for use case implementation
     */
    public static ArchRule applicationUseCaseMustImplementInboundPorts() {
        return classes()
                .that().resideInAPackage(applicationUseCasePackage)
                .and().areNotInterfaces()
                .and().areNotAnonymousClasses()
                .and().areNotLocalClasses()
                .and(ClassPredicates.areNotSyntheticClasses())
                .should(new ImplementTypeFoundInPackages(List.of(portInboundPackage)))
                .because(String.format("%s must contain only implementations of inbound ports", applicationUseCasePackage));
    }

    /**
     * Rule that ensures classes in the {@code application.usecase} package do not implement outbound ports.
     * <p>
     * Use cases should depend on outbound ports, not implement them.
     * </p>
     *
     * @return an {@link ArchRule} for use case constraints
     */
    public static ArchRule applicationUseCaseMustNotImplementOutboundPorts() {
        return classes()
                .that().resideInAPackage(applicationUseCasePackage)
                .and().areNotInterfaces()
                .should(new NotImplementTypesFoundInPackages(List.of(portOutboundPackage)))
                .because(String.format("%s must not contain only implementations of outbound ports", applicationUseCasePackage));
    }

    /**
     * Rule that prohibits interfaces from being defined directly in the {@code application.usecase} package.
     * <p>
     * Interfaces should be defined in {@code port} packages.
     * </p>
     *
     * @return an {@link ArchRule} for use case package structure
     */
    public static ArchRule applicationUseCaseMustNotContainInterfaces() {
        return noClasses()
                .that().resideInAPackage(applicationUseCasePackage)
                .should().beInterfaces()
                .because(String.format("%s must only contain only implementations of inbound ports", applicationUseCasePackage));
    }

    /**
     * Rule that prohibits interfaces in adapter adapter packages,
     * with an exception for persistence-related interfaces (e.g., Spring Data repositories).
     *
     * @return an {@link ArchRule} for adapter adapter structure
     */
    public static ArchRule adapterAdapterMustNotContainInterfacesExceptForPersistence() {
        return noClasses()
                .that().resideInAnyPackage(adapterInboundPackage, adapterOutboundPackage)
                .and().resideOutsideOfPackage(adapterOutboundPersistencePackage)
                .should().beInterfaces()
                .because(String.format("adapter adapte`rs must be concrete implementations and NOT interfaces (except for JPA repositories in %s)", adapterOutboundPersistencePackage));
    }

    /**
     * Rule that prohibits class names from ending with {@code Impl}.
     * <p>
     * Encourages naming classes based on their responsibility rather than their implementation detail.
     * </p>
     *
     * @return an {@link ArchRule} against 'Impl' naming
     */
    public static ArchRule noClassesShouldBeNamedImpl() {
        return noClasses()
                .that().haveSimpleNameEndingWith("Impl")
                .should().beAssignableTo(Object.class)
                .because("Class names should not end with 'Impl'. Name classes by responsibility, not by implementation detail.");
    }

    /**
     * Contracts define
     * */
    public static ArchRule allPortImplementationsShouldHaveTests() {
        return classes()
                .that().areNotInterfaces()
                .and().areNotAnonymousClasses()
                .and().areNotLocalClasses()
                .and(ClassPredicates.areNotSyntheticClasses())
                .and().implement(ClassPredicates.interfacesInPortPackages())
                .should(new HaveAtLeastOneTest())
                .because("Implementations of inbound and outbound ports should have at least one test class.");
    }

    private static void addIfMissing(List<String> additionalPackages, String packageDescription) {
        if (!additionalPackages.contains(packageDescription)) {
            additionalPackages.add(packageDescription);
        }
    }

    private static ClassesShouldConjunction packageShouldOnlyDependOn(String mainPackageName, List<String> additionalPackages) {
        String[] allowedPackages = new String[additionalPackages.size() + 2];
        allowedPackages[0] = mainPackageName;
        allowedPackages[1] = javaPackage;
        for (int i = 0; i < additionalPackages.size(); i++) {
            allowedPackages[i + 2] = additionalPackages.get(i);
        }

        return classes()
                .that().resideInAPackage(mainPackageName)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages);
    }

}
