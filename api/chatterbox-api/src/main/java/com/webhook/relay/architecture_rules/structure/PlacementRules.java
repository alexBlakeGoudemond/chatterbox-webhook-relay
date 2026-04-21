package com.webhook.relay.architecture_rules.structure;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit rules for enforcing class placement based on naming conventions.
 */
public class PlacementRules {

    /**
     * Rule that ensures classes with a name ending in {@code Repository} reside in a {@code repository} package.
     *
     * @return an {@link ArchRule} for repository placement
     */
    public static ArchRule repositoryClassesResideInRepositoryPackage(){
        return classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..repository..")
                .because("Repositories must live in a ..repository.. package");
    }

    /**
     * Rule that ensures classes with a name ending in {@code Service} reside in a {@code service} package.
     *
     * @return an {@link ArchRule} for service placement
     */
    public static ArchRule serviceClassesResideInServicePackage(){
        return classes()
                .that().haveSimpleNameEndingWith("Service")
                .should().resideInAPackage("..service..")
                .because("Services must live in a ..service.. package");
    }

    /**
     * Rule that ensures classes with a name ending in {@code Controller} reside in a {@code controller} package.
     *
     * @return an {@link ArchRule} for controller placement
     */
    public static ArchRule controllerClassesResideInControllerPackage(){
        return classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..controller..")
                .because("Controllers must live in a ..controller.. package");
    }

}
