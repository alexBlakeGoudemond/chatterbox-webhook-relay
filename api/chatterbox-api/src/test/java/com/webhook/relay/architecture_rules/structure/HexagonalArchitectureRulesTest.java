package com.webhook.relay.architecture_rules.structure;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static com.webhook.relay.architecture_rules.definition.KnownPackagesHelper.*;

public class HexagonalArchitectureRulesTest extends ArchRuleAbstractTest {

    /**
     * Returns a list of additional packages that are allowed in the domain layer.
     * <p>
     * By default, domain classes may only depend on:
     * <ul>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * This method allows each project to specify extra packages that are
     * acceptable for domain dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     * @Override
     * protected List<String> domainAllowedPackages() {
     *     return List.of("com.fasterxml.jackson..");
     * }
     * }
     * </pre>
     *
     * @return list of package patterns allowed in the domain layer
     */
    protected List<String> domainAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        return allowedPackages;
    }

    /**
     * Returns a list of additional packages that are allowed in the application layer.
     * <p>
     * By default, application classes may only depend on:
     * <ul>
     *     <li>..application..</li>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * This method allows each project to specify extra packages that are
     * acceptable for application dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     * @Override
     * protected List<String> applicationAllowedPackages() {
     *     return List.of("org.springframework..");
     * }
     * }
     * </pre>
     *
     * @return list of package patterns allowed in the application layer
     */
    protected List<String> applicationAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        allowedPackages.addAll(springPackages());
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(slf4jPackages());
        return allowedPackages;
    }

    /**
     * Returns a list of additional packages that are allowed in the adapter layer.
     * <p>
     * By default, adapter classes may only depend on:
     * <ul>
     *     <li>..adapter..</li>
     *     <li>..application..</li>
     *     <li>..application.domain..</li>
     *     <li>java..</li>
     * </ul>
     * This method allows each project to specify extra packages that are
     * acceptable for adapter dependencies.
     * </p>
     * <p>
     * Example:
     * <pre>
     * {@code
     * @Override
     * protected List<String> adapterAllowedPackages() {
     *     return List.of("org.springframework..");
     * }
     * }
     * </pre>
     *
     * @return list of package patterns allowed in the adapter layer
     */
    protected List<String> adapterAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(springPackages());
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        allowedPackages.addAll(persistencePackages());
        allowedPackages.addAll(apacheHttpPackages());
        allowedPackages.addAll(asyncReactivePackages());
        allowedPackages.addAll(jakartaServletPackages());
        allowedPackages.addAll(micrometerMetricPackages());
        allowedPackages.addAll(slf4jPackages());
        allowedPackages.addAll(javaCryptorPackages());
        return allowedPackages;
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

    @DisplayName("adapter.in depends on valid types")
    @Test
    public void adapterInboundReferencesOnlyAdapterInbound(){
        HexagonalArchitectureRules.adapterInboundShouldOnlyDependOn(adapterAllowedPackages())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("adapter.out depends on valid types")
    @Test
    public void adapterOutboundReferencesOnlyAdapterOutbound(){
        HexagonalArchitectureRules.adapterOutboundShouldOnlyDependOn(adapterAllowedPackages())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Application does not depend on adapter")
    @Test
    public void applicationDoesNotDependOnAdapter() {
        HexagonalArchitectureRules.applicationOnlyDependsOn(applicationAllowedPackages())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
        HexagonalArchitectureRules.applicationCannotDependOnAdapter()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Domain does not depend on adapter or Application")
    @Test
    public void domainIsIndependent() {
        HexagonalArchitectureRules.domainOnlyDependsOn(domainAllowedPackages())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
        HexagonalArchitectureRules.domainCannotDependOnAdapter()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Port interfaces are named Port")
    @Test
    public void portInterfacesNamedPort() {
        HexagonalArchitectureRules.portInterfacesNamedPort()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Persistence is only in adapter")
    @Test
    public void persistenceOnlyInAdapter() {
        HexagonalArchitectureRules.persistenceOnlyInAdapter()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("No Spring in Domain")
    @Test
    public void noSpringInDomain(){
        HexagonalArchitectureRules.noSpringInDomain()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("No Persistence in Domain")
    @Test
    public void noPersistenceInDomain(){
        HexagonalArchitectureRules.noPersistenceInDomain()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Ports exist in either application.port.in or application.port.out")
    @Test
    public void portsShouldExistOnlyInApplicationInOrApplicationOut(){
        HexagonalArchitectureRules.portsExistInApplicationInOrApplicationOutPackage()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Adapters exist in adapter.in or adapter.out")
    @Test
    public void adaptersExistOnlyInAdapter(){
        HexagonalArchitectureRules.adaptersExistOnlyInAdapterPackage()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Inbound ports have implementations in allowed packages")
    @Test
    public void applicationInboundPorts_HaveImplementationsInAllowedPackages(){
        HexagonalArchitectureRules.inboundPortsImplementedInAllowedPackages()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Outbound ports have implementations in adapter.out")
    @Test
    public void applicationOutboundPorts_HaveImplementationsInAdapterOut(){
        HexagonalArchitectureRules.outboundPortsImplementedInAdapterOut()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Application UseCase must implement Inbound Ports")
    @Test
    public void applicationUseCaseMustImplementInboundPorts(){
        HexagonalArchitectureRules.applicationUseCaseMustImplementInboundPorts()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Application UseCase must not implement Outbound Ports")
    @Test
    public void applicationUseCaseMustNotImplementOutboundPorts(){
        HexagonalArchitectureRules.applicationUseCaseMustNotImplementOutboundPorts()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Application UseCase must not contain interfaces")
    @Test
    public void applicationUseCaseMustNotContainInterfaces(){
        HexagonalArchitectureRules.applicationUseCaseMustNotContainInterfaces()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("Adapter must not contain interfaces except for persistence")
    @Test
    public void adapterAdapterMustNotContainInterfacesExceptForPersistence(){
        HexagonalArchitectureRules.adapterAdapterMustNotContainInterfacesExceptForPersistence()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("No classes should be named Impl")
    @Test
    public void noClassesShouldBeNamedImpl(){
        HexagonalArchitectureRules.noClassesShouldBeNamedImpl()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("All port implementations should have tests")
    @Test
    public void allPortImplementationsShouldHaveTests(){
        HexagonalArchitectureRules.allPortImplementationsShouldHaveTests()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainAndTestClasses());
    }

}
