package za.co.psybergate.architecture_rules.structure;

public class PlacementRulesTest extends PlacementRulesAbstractTest {

    @Override
    protected String basePackage() {
        return "za.co.psybergate.chatterbox";
    }

    @Override
    protected boolean passIfNoClassesApplyToRule() {
        return true;
    }

}
