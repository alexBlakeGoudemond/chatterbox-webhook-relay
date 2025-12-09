package za.co.psybergate.chatterbox.application.webhook.service;

import java.util.Map;

public interface TemplateSubstitutionService {

    String apply(String textToReplace, Map<String, String> values);

}
