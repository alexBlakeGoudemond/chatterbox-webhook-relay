package com.webhook.relay.architecture_rules.transaction;

import com.tngtech.archunit.lang.ArchRule;
import com.webhook.relay.architecture_rules.definition.KnownTypesHelper;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit rules for enforcing consistent use of transactional annotations.
 */
public class TransactionalAnnotationRules {

    /**
     * Rule that prohibits the use of the Jakarta Transactional annotation.
     * <p>
     * Typically used when the project standardizes on the Spring Transactional annotation.
     * </p>
     *
     * @return an {@link ArchRule} prohibiting Jakarta Transactional
     */
    public static ArchRule noClassShouldUseJakartaTransactional() {
        return noClasses()
                .should().beAnnotatedWith(KnownTypesHelper.jakartaTransactionalAnnotation());
    }

    /**
     * Rule that ensures classes annotated with Transactional are using the Spring variant.
     *
     * @return an {@link ArchRule} enforcing Spring Transactional
     */
    public static ArchRule classesShouldOnlyUseSpringTransactional() {
        return classes()
                .that().areAnnotatedWith(KnownTypesHelper.springTransactionalAnnotation())
                .should().beAnnotatedWith(KnownTypesHelper.springTransactionalAnnotation());
    }
}
