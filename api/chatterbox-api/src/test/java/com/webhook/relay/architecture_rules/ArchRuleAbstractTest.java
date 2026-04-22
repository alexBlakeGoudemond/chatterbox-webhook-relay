package com.webhook.relay.architecture_rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.util.Collections;
import java.util.Set;

/**
 * Base class for ArchUnit tests.
 * <p>
 * Provides common functionality for importing classes and defining the scope of architectural rules.
 * </p>
 */
public abstract class ArchRuleAbstractTest {

    /**
     * Returns the root package of the project being tested.
     * <p>
     * All rules will be applied to classes under this package. (Main directory, not test directory)
     * </p>
     *
     * @return the base package name (e.g., "za.co.company.chatterbox")
     */
    protected String basePackage() {
        return "com.webhook.relay.chatterbox";
    }

    /**
     * Imports all classes under the base package while excluding test classes.
     *
     * @return imported production classes
     */
    protected JavaClasses mainClasses() {
        Set<ImportOption> importOptionsForOnlyMainDirectory = Collections.singleton(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);
        return new ClassFileImporter()
                .withImportOptions(importOptionsForOnlyMainDirectory)
                .importPackages(basePackage());
    }

    /**
     * Imports all classes under the base package including test classes.
     *
     * @return imported production classes
     */
    protected JavaClasses mainAndTestClasses() {
        return new ClassFileImporter()
                .importPackages(basePackage());
    }

    /**
     * Determines whether a rule should pass if no classes match the "that" clause.
     *
     * @return true if the rule should pass when no classes apply, false otherwise
     */
    protected boolean passIfNoClassesApplyToRule() {
        return false;
    }

}
