package za.co.psybergate.chatterbox.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import za.co.psybergate.chatterbox.infrastructure.filter.WebhookFilter;

// TODO BlakeGoudemond 2025/11/27 | essential an Aspect right?
@Configuration
public class ApplicationConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public FilterRegistrationBean<WebhookFilter> applicationWebhookFilter(WebhookFilter webhookFilter) {
        FilterRegistrationBean<WebhookFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(webhookFilter);
        String url = apiPrefix + "/webhook/*";
        registration.addUrlPatterns(url); // only intercept webhook endpoints
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
