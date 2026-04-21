package com.webhook.relay.architecture_rules.arch.condition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;

public class ImplementTypeFoundInPackages extends ArchCondition<JavaClass> {

    private final List<String> packagesAllowed;

    public ImplementTypeFoundInPackages(List<String> packagesAllowed) {
        super("implement an inbound port");
        this.packagesAllowed = packagesAllowed.stream()
                .map(this::format)
                .toList();
    }

    @Override
    public void check(JavaClass implementationClass, ConditionEvents events) {
        String implementationPackageName = null;
        for (String allowedPackage : packagesAllowed) {
            boolean implementsTypeThatIsAllowed = implementationClass.getInterfaces().stream()
                    .anyMatch(javaType ->
                            javaType.getName().contains(allowedPackage)
                    );
            if (implementsTypeThatIsAllowed) {
                implementationPackageName = allowedPackage;
                break;
            }
        }
        if (implementationPackageName == null) {
            String message = String.format(
                    "Implementation class %s does not implement any interfaces present in %s",
                    implementationClass.getName(),
                    packagesAllowed
            );
            events.add(SimpleConditionEvent.violated(implementationClass, message));
        }
    }

    private String format(String pattern) {
        return pattern.replaceAll("^\\.\\.|\\.\\.$", "");
    }


}
