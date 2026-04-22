package com.webhook.relay.architecture_rules.quality;

import com.tngtech.archunit.lang.ArchRule;
import com.webhook.relay.architecture_rules.arch.condition.HaveMatchingProductionClassAndPackage;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit rules for maintaining general test code quality and package naming.
 */
public class TestNamingAndPackagingRules {

    public static ArchRule testsShouldMatchBackingClassAndPackage(String basePackage) {
        return classes()
                .that().haveSimpleNameEndingWith("Test")
                .or().haveSimpleNameEndingWith("IT")
                .should(new HaveMatchingProductionClassAndPackage(basePackage));
    }

}
