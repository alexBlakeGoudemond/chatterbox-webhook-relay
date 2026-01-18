package za.co.psybergate.chatterbox.application.usecase.template;

import java.util.Map;

public interface TemplateSubstitutor {

    String apply(String textToReplace, Map<String, String> values);

}
