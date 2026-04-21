package com.webhook.relay.architecture_rules.arch.rule;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

import java.util.List;
import java.util.function.Predicate;

import static com.webhook.relay.architecture_rules.definition.HexagonalArchitectureKnownPackages.portInboundPackage;
import static com.webhook.relay.architecture_rules.definition.HexagonalArchitectureKnownPackages.portOutboundPackage;

public class ClassPredicates {

    public static DescribedPredicate<JavaClass> areNotSyntheticClasses() {
        return DescribedPredicate.describe(
                "are not synthetic classes",
                (JavaClass clazz) -> !clazz.reflect().isSynthetic()
        );
    }

    public static DescribedPredicate<JavaClass> interfacesInPortPackages() {
        return DescribedPredicate.describe(
                "interfaces in port packages",
                (JavaClass clazz) -> clazz.isInterface() &&
                        (packageMatches(portInboundPackage, clazz.getPackageName()) ||
                                packageMatches(portOutboundPackage, clazz.getPackageName()))
        );
    }

    public static boolean packageMatches(String pattern, String packageName) {
        String trimmedPattern = pattern.replaceAll("^\\.\\.|\\.\\.$", "");
        return packageName.contains(trimmedPattern);
    }

    public static final List<String> TEST_SUFFIXES = List.of("Test", "Tests", "IT", "Spec");
    
    public static Predicate<JavaClass> classNameEndsWithTest() {
        return clazz -> TEST_SUFFIXES.stream()
                .anyMatch(suffix -> clazz.getSimpleName().endsWith(suffix));
    }
    
}
