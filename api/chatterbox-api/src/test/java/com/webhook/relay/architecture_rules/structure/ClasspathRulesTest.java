package com.webhook.relay.architecture_rules.structure;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.fail;

public class ClasspathRulesTest extends ArchRuleAbstractTest {

    /**
     * Method that allows the user to specify which Paths are allowed
     * and should be ignored when performing the class path tests
     * <p>
     * For example
     * <pre>
     * {@code
     * List.of("test-classes", "classes/application-dev.yml");
     * }
     * </pre>
     *
     */
    public List<String> allowedPaths() {
        return List.of("test-classes", "classes/application.yml", "classes/application-qa.yml");
    }

    @DisplayName("Compiled code shouldn't contain specific properties files")
    @Test
    public void compiledCodeShouldNotContainPropertiesFiles() {
        List<String> invalidPathNames = ClasspathResourcesRules.compiledCodeShouldNotContainPropertiesFiles(allowedPaths());
        String errorString = getErrorStringForInvalidPathNames(invalidPathNames);
        assertTrue(invalidPathNames.isEmpty(), errorString);
    }

    private static String getErrorStringForInvalidPathNames(List<String> invalidPathNames) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("If a Path is meant to be supported, consider adding it to the allowedPaths(). e.g.) 'test-classes'\n");
        for (String invalidPathName : invalidPathNames) {
            stringBuilder.append(String.format("Resource not supported: %s\n", invalidPathName));
        }
        return stringBuilder.toString();
    }

    @DisplayName("Compiled code shouldn't contain prod profiles")
    @TestFactory
    public Stream<DynamicTest> prodProfilesMustNotExistOnClasspath() {
        List<String> violations = ClasspathResourcesRules.compiledCodeShouldNotContainProdProfiles(allowedPaths());
        return violations.stream()
                .map(mapToDetailedViolationExplanation());
    }

    private Function<String, DynamicTest> mapToDetailedViolationExplanation() {
        return location ->
                DynamicTest.dynamicTest(String.format(
                                "Prod profile found (not allowed) in: %s", location),
                        () -> fail("Forbidden prod profile detected: " + location)
                );
    }

}
