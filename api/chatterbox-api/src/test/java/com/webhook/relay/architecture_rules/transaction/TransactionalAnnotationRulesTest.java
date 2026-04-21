package com.webhook.relay.architecture_rules.transaction;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class TransactionalAnnotationRulesTest extends ArchRuleAbstractTest {

    /**
     * Test verifying that Jakarta Transactional is not used.
     */
    @DisplayName("Jakarta Transactional annotation not used")
    @Test
    public void noClassShouldUseJakartaTransactional() {
        TransactionalAnnotationRules.noClassShouldUseJakartaTransactional()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    /**
     * Test verifying that only Spring Transactional is used.
     */
    @DisplayName("Spring Transactional annotation can be used")
    @Test
    public void classesShouldUseOnlySpringTransactional() {
        TransactionalAnnotationRules.classesShouldOnlyUseSpringTransactional()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

}
