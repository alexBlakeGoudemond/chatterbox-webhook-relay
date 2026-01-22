package za.co.psybergate.chatterbox;

import za.co.psybergate.architecture_rules.hexagon.HexagonalArchitectureAbstractTest;

import java.util.List;

public class ArchitectureRulesTest extends HexagonalArchitectureAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected List<String> domainAllowedPackages() {
        return List.of("com.fasterxml.jackson..", "lombok..", "jakarta.validation..");
    }

    @Override
    protected List<String> applicationAllowedPackages() {
        return List.of("org.springframework..", "com.fasterxml.jackson..", "lombok..", "jakarta.validation..", "org.slf4j..");
    }

    @Override
    protected List<String> infrastructureAllowedPackages() {
        return List.of("org.springframework..", "com.fasterxml.jackson..", "lombok..", "jakarta.persistence..", "org.hibernate..", "org.apache..", "reactor.core.publisher..", "jakarta.validation..");
    }

}
