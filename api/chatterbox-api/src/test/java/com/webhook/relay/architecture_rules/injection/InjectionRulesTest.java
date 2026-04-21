package com.webhook.relay.architecture_rules.injection;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class InjectionRulesTest extends ArchRuleAbstractTest {

    /**
     * Test verifying that no fields are annotated with @Autowired.
     */
    @DisplayName("No fields annotated with @Autowired")
    @Test
    public void noFieldsShouldBeAnnotatedWithAutowired() {
        InjectionRules.noAutowiredFields()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

}
