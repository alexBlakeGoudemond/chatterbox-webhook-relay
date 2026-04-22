package com.webhook.relay.chatterbox.application.common.template;

import java.util.Map;

public interface TemplateSubstitutor {

    String apply(String textToReplace, Map<String, String> values);

}
