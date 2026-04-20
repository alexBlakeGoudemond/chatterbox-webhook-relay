package com.webhook.relay.architecture_rules.quality;

public class CodeQualityRulesTest extends CodeQualityRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "com.webhook.relay.chatterbox";
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
