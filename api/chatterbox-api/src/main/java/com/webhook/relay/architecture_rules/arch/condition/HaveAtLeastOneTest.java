package com.webhook.relay.architecture_rules.arch.condition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.webhook.relay.architecture_rules.arch.rule.ClassPredicates;

import java.util.Set;

public class HaveAtLeastOneTest extends ArchCondition<JavaClass> {

    public HaveAtLeastOneTest() {
        super("have at least one test");
    }

    @Override
    public void check(final JavaClass clazz, ConditionEvents events) {
        Set<JavaClass> allClasses = clazz.getPackage().getClasses();
        boolean hasTest = allClasses.stream()
                .filter(ClassPredicates.classNameEndsWithTest())
                .anyMatch(testClass ->
                        testClass.getSimpleName().startsWith(clazz.getSimpleName()) ||
                        testClass.getDirectDependenciesFromSelf().stream()
                                .anyMatch(dependency -> dependency.getTargetClass().equals(clazz))
                );
        if (!hasTest) {
            events.add(SimpleConditionEvent.violated(
                    clazz,
                    String.format("Could not find a test for '%s', please ensure any test class contains suffix '%s'", clazz.getFullName(), ClassPredicates.TEST_SUFFIXES)
            ));
        }
    }

}