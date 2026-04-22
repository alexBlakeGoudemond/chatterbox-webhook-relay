package com.webhook.relay.architecture_rules.quality;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * ArchUnit rules for maintaining general code quality and encapsulation.
 */
public class CodeQualityRules {

    /**
     * Rule that prohibits public instance fields in any class.
     * <p>
     * Promotes encapsulation by requiring the use of accessors or other mechanisms
     * instead of exposing internal state directly.
     * </p>
     *
     * @return an {@link ArchRule} prohibiting public instance fields
     */
    public static ArchRule noPublicInstanceFields() {
        return noFields()
                .that().arePublic()
                .and().areNotStatic()
                .should().haveNameMatching(".*")
                .because("Public instance fields expose internal state. Use encapsulation instead.");
    }

    /**
     * Rule that prohibits methods from declaring that they throw {@link RuntimeException}.
     * <p>
     * Encourages the use of more specific domain or application exceptions
     * to provide better context for error handling.
     * </p>
     *
     * @return an {@link ArchRule} prohibiting the declaration of raw runtime exceptions
     */
    public static ArchRule noRawRuntimeExceptions() {
        return  noMethods()
                .that().declareThrowableOfType(RuntimeException.class)
                .should().haveNameMatching(".*")
                .because("Do not throw raw RuntimeException. Use domain- or application-specific exceptions instead.");
    }

}
