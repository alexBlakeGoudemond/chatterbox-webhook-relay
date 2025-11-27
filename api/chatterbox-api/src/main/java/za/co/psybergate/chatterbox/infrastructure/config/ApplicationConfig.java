package za.co.psybergate.chatterbox.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggingFilter;

// TODO BlakeGoudemond 2025/11/27 | keep going from here. Is the doFilter separate from logback? if so, use logback
// TODO BlakeGoudemond 2025/11/27 | also use SignatureValidationLogger
@Configuration
public class ApplicationConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public FilterRegistrationBean<WebhookLoggingFilter> webhookLoggingFilter() {
        FilterRegistrationBean<WebhookLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebhookLoggingFilter());
        registration.addUrlPatterns(apiPrefix + "/webhook/*"); // only intercept webhook endpoints
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
