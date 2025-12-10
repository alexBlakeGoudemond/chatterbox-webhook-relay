package za.co.psybergate.chatterbox.application.teams.factory.template;

import java.util.Map;

public interface TeamsTemplateSubstitutor {

    String apply(String textToReplace, Map<String, String> values);

}
