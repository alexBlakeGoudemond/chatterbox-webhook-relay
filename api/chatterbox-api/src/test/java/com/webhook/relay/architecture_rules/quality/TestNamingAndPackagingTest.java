package com.webhook.relay.architecture_rules.quality;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class TestNamingAndPackagingTest extends ArchRuleAbstractTest {

    @DisplayName("Tests should match backing class and package")
    @Test
    public void testsShouldMatchBackingClassAndPackage(){
        TestNamingAndPackagingRules.testsShouldMatchBackingClassAndPackage(basePackage())
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainAndTestClasses());
    }

}
