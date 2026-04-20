package com.webhook.relay.architecture_rules.structure;

public class PlacementRulesTest extends PlacementRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "com.webhook.relay.chatterbox";
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
