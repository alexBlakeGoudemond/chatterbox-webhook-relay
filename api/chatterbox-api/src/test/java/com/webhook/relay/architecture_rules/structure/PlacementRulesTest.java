package com.webhook.relay.architecture_rules.structure;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class PlacementRulesTest extends ArchRuleAbstractTest {

    /**
     * Test verifying that repository classes are in the correct package.
     */
    @DisplayName("Repository classes reside in ..repository.. package")
    @Test
    public void repositoryClassesResideInRepositoryPackage() {
        PlacementRules.repositoryClassesResideInRepositoryPackage()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    /**
     * Test verifying that service classes are in the correct package.
     */
    @DisplayName("Service classes reside in ..service.. package")
    @Test
    public void serviceClassesResideInServicePackage() {
        PlacementRules.serviceClassesResideInServicePackage()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    /**
     * Test verifying that controller classes are in the correct package.
     */
    @DisplayName("Controller classes reside in ..controller.. package")
    @Test
    public void controllerClassesResideInControllerPackage() {
        PlacementRules.controllerClassesResideInControllerPackage()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

}
