package com.webhook.relay.architecture_rules.arch.condition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.webhook.relay.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;

import java.util.Optional;

public class HaveMatchingProductionClassAndPackage extends ArchCondition<JavaClass> {

    private final JavaClasses allClasses;

    public HaveMatchingProductionClassAndPackage(String basePackage) {
        super("have a matching production class with matching package structure");
        this.allClasses = new ClassFileImporter().importPackages(basePackage);
    }

    @Override
    public void check(JavaClass testClass, ConditionEvents events) {
        String productionClassName = getProductionClassName(testClass);
        JavaClass productionClass = findConcreteProductionClass(productionClassName);
        if (productionClass == null) {
            events.add(SimpleConditionEvent.violated(
                    testClass,
                    String.format("No concrete production class found for test '%s'. Please confirm the implementation class, otherwise consider using @%s()", testClass.getFullName(), MirrorProductionClassForArchitectureRuleTests.class.getSimpleName())
            ));
            return;
        }
        String testPackage = testClass.getPackageName();
        String prodPackage = productionClass.getPackageName();
        if (!testPackage.contains(prodPackage)) {
            events.add(SimpleConditionEvent.violated(
                    testClass,
                    String.format("Test class '%s' does not mirror production class '%s'", testClass.getName(), productionClass.getName())
            ));
        }
    }

    private JavaClass findConcreteProductionClass(String productionClassName) {
        return allClasses.stream()
                .filter(clazz -> clazz.getSimpleName().equals(productionClassName))
                .filter(clazz -> !clazz.getModifiers().contains(JavaModifier.ABSTRACT))
                .findFirst()
                .orElse(null);
    }

    private String getProductionClassName(JavaClass testClass) {
        Optional<MirrorProductionClassForArchitectureRuleTests> productionClassAnnotation = testClass.tryGetAnnotationOfType(MirrorProductionClassForArchitectureRuleTests.class);
        if (productionClassAnnotation.isPresent()) {
            Class<?> productionClass = productionClassAnnotation.get().value();
            return productionClass.getSimpleName();
        }
        return testClass.getSimpleName().replaceFirst("(Test|IT)$", "");
    }

}
