package com.webhook.relay.architecture_rules.injection;

import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * ArchUnit rules related to dependency injection practices.
 */
public class InjectionRules {

    /**
     * Rule that forbids the use of {@link Autowired} on fields.
     * <p>
     * Encourages the use of constructor injection for better testability and immutability.
     * </p>
     *
     * @return an {@link ArchRule} prohibiting field injection
     */
    public static ArchRule noAutowiredFields() {
        return noFields()
                .should()
                .beAnnotatedWith(Autowired.class)
                .because("Field injection is forbidden — use constructor injection instead");
    }

}
