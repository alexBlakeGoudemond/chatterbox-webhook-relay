package za.co.psybergate.chatterbox.infrastructure.template;

import java.util.Map;

public interface TemplateSubstitutor {

    String apply(String textToReplace, Map<String, String> values);

}
