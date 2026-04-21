package com.webhook.relay.architecture_rules.arch.condition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;

public class NotImplementTypesFoundInPackages extends ArchCondition<JavaClass> {

    private final List<String> packagesNotAllowed;

    public NotImplementTypesFoundInPackages(List<String> packagesNotAllowed) {
        super("should not implement an outbound port");
        this.packagesNotAllowed = packagesNotAllowed.stream()
                .map(this::format)
                .toList();
    }

    @Override
    public void check(JavaClass implementationClass, ConditionEvents events) {
        for (final String packageNotAllowed : packagesNotAllowed) {
            boolean implementsTypeThatIsNotAllowed = implementationClass.getInterfaces().stream()
                    .anyMatch(javaType ->
                            javaType.getName().contains(packageNotAllowed)
                    );
            if (implementsTypeThatIsNotAllowed) {
                String message = String.format(
                        "Implementation class %s implements an interface %s from a package that is not allowed.",
                        implementationClass.getName(),
                        packageNotAllowed
                );
                events.add(SimpleConditionEvent.violated(implementationClass, message));
            }
        }
    }

    private String format(String pattern) {
        return pattern.replaceAll("^\\.\\.|\\.\\.$", "");
    }

}
