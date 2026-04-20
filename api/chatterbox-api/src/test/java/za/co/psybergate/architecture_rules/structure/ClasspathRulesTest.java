package com.webhook.relay.architecture_rules.structure;

import java.util.List;

public class ClasspathRulesTest extends ClasspathResourcesRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "com.webhook.relay";
    }

    @Override
    public List<String> allowedPaths() {
        return List.of("test-classes", "classes/application.yml", "classes/application-qa.yml");
    }

}
