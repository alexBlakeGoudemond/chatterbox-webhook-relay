package com.webhook.relay.chatterbox.application.common.template;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexTemplateSubstitutor implements TemplateSubstitutor {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    /// From a given [Map] of property values, replace all placeholders in the form
    /// of `${...}` with the appropriate value
    ///
    /// For example, if the values are
    ///
    /// `[{"displayName", "Pull Request Event"}, {"repositoryName", "psyAlexBlakeGoudemond/chatterbox"}]`
    ///
    /// and the textToReplace is
    ///
    /// `📢 ${displayName} for ${repositoryName}`
    ///
    /// then the replacemed String is:
    ///
    /// `📢 Pull Request Event for psyAlexBlakeGoudemond/chatterbox`
    @Override
    public String apply(String textToReplace, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(textToReplace);
        StringBuilder stringBuilder = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object val = values.getOrDefault(key, "");
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(String.valueOf(val)));
        }

        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }

}

