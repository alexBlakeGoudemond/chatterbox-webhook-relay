package za.co.psybergate.architecture_rules.hexagon;

import za.co.psybergate.architecture_rules.hexagon.HexagonalArchitectureAbstractTest;

import java.util.ArrayList;
import java.util.List;

import static za.co.psybergate.architecture_rules.definition.KnownPackagesHelper.*;

public class HexagonalArchitectureRulesTest extends HexagonalArchitectureAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected List<String> domainAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        return allowedPackages;
    }

    @Override
    protected List<String> applicationAllowedPackages() {
        List<String> allowedPackages = new ArrayList<>();
        allowedPackages.addAll(springPackages());
        allowedPackages.addAll(jacksonPackages());
        allowedPackages.addAll(lombokPackages());
        allowedPackages.addAll(jakartaValidationPackages());
        allowedPackages.addAll(slf4jPackages());
        return allowedPackages;
    }

    @Override
    protected List<String> infrastructureAllowedPackages() {
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

}
