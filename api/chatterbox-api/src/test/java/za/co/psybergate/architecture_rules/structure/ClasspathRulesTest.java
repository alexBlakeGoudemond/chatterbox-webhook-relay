package za.co.psybergate.architecture_rules.structure;

import java.util.List;

public class ClasspathRulesTest extends ClasspathResourcesRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate";
    }

    @Override
    public List<String> allowedPaths() {
        return List.of("test-classes", "classes/application.yml");
    }

}
