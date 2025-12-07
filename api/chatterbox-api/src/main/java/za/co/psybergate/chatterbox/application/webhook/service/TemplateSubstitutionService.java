package za.co.psybergate.chatterbox.application.webhook.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO BlakeGoudemond 2025/12/07 | add javadocs to this class
@Service
public class TemplateSubstitutionService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    public String apply(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(template);
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

