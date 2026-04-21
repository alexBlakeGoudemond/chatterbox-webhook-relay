package com.webhook.relay.architecture_rules.quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly specify the production class associated with a test class.
 * This is useful when the test class name does not follow the standard naming convention
 * (e.g., [ProductionClassName]Test or [ProductionClassName]IT).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MirrorProductionClassForArchitectureRuleTests {

    /**
     * @return The production class.
     */
    Class<?> value();

}
