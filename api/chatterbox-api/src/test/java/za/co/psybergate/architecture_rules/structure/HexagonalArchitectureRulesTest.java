package za.co.psybergate.architecture_rules.structure;

import java.util.ArrayList;
import java.util.List;

import static za.co.psybergate.architecture_rules.definition.KnownPackagesHelper.*;

public class HexagonalArchitectureRulesTest extends HexagonalArchitectureRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected List<String> domainAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        return allowedPackages;
    }

    // TODO BlakeGoudemond 2026/02/07 | fun exercise - comment out and see how easy it is to fix - slf4j done
    @Override
    protected List<String> applicationAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        allowedPackages.addAll(springPackages());
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(slf4jPackages());
        return allowedPackages;
    }

    @Override
    protected List<String> adapterAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(springPackages());
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        allowedPackages.addAll(persistencePackages());
        allowedPackages.addAll(apacheHttpPackages());
        allowedPackages.addAll(asyncReactivePackages());
        allowedPackages.addAll(jakartaServletPackages());
        allowedPackages.addAll(micrometerMetricPackages());
        allowedPackages.addAll(slf4jPackages());
        allowedPackages.addAll(javaCryptorPackages());
        return allowedPackages;
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
