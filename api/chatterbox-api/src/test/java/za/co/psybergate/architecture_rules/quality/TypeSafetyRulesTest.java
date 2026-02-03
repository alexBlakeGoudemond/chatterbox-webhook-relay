package za.co.psybergate.architecture_rules.quality;

public class TypeSafetyRulesTest extends TypeSafetyRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
