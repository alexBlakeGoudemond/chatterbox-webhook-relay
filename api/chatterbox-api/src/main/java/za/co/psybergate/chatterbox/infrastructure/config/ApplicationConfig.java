package za.co.psybergate.chatterbox.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggingFilter;

// TODO BlakeGoudemond 2025/11/27 | essential an Aspect right?
// TODO BlakeGoudemond 2025/11/27 | can we get a script to teardown the images / containers, recreate jar and then build up?
@Configuration
public class ApplicationConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public FilterRegistrationBean<WebhookLoggingFilter> webhookLoggingFilter() {
        FilterRegistrationBean<WebhookLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebhookLoggingFilter());
        String url = apiPrefix + "/webhook/*";
        registration.addUrlPatterns(url); // only intercept webhook endpoints
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
