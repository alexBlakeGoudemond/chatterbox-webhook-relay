package com.webhook.relay.architecture_rules.arch.condition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.webhook.relay.architecture_rules.arch.rule.ClassPredicates;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HaveImplementationInAllowedPackages extends ArchCondition<JavaClass> {

    private final List<String> allowedPackages;

    public HaveImplementationInAllowedPackages(List<String> allowedPackages) {
        super("have an implementation in " + allowedPackages);
        this.allowedPackages = allowedPackages;
    }

    @Override
    public void check(JavaClass portInterface, ConditionEvents events) {

        Set<JavaClass> implementations = portInterface.getAllSubclasses()
                .stream()
                .filter(clazz -> allowedPackages.stream()
                        .anyMatch(pattern -> ClassPredicates.packageMatches(pattern, clazz.getPackageName())))
                .collect(Collectors.toSet());

        if (implementations.isEmpty()) {
            String message = String.format(
                    "Inbound port %s has no implementation in %s",
                    portInterface.getName(),
                    allowedPackages
            );
            events.add(SimpleConditionEvent.violated(portInterface, message));
        }
    }

}
