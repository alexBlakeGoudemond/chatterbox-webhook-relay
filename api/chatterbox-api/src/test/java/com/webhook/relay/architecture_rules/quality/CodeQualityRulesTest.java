package com.webhook.relay.architecture_rules.quality;

import com.webhook.relay.architecture_rules.ArchRuleAbstractTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class CodeQualityRulesTest extends ArchRuleAbstractTest {

    @DisplayName("No public instance fields")
    @Test
    public void noPublicInstanceFields(){
        CodeQualityRules.noPublicInstanceFields()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

    @DisplayName("No raw runtime exceptions")
    @Test
    public void noRawRuntimeExceptions(){
        CodeQualityRules.noRawRuntimeExceptions()
                .allowEmptyShould(passIfNoClassesApplyToRule())
                .check(mainClasses());
    }

}
