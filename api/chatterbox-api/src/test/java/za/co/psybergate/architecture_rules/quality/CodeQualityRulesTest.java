package za.co.psybergate.architecture_rules.quality;

public class CodeQualityRulesTest extends CodeQualityRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
