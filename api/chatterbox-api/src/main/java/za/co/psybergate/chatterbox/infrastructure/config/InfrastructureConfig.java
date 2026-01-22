package za.co.psybergate.chatterbox.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import za.co.psybergate.chatterbox.infrastructure.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.out.config.properties.ChatterboxApiProperties;

@Configuration
@RequiredArgsConstructor
public class InfrastructureConfig {

    private final ChatterboxApiProperties chatterboxApiProperties;

    @Bean
    public FilterRegistrationBean<WebhookFilter> applicationWebhookFilter(WebhookFilter webhookFilter) {
        FilterRegistrationBean<WebhookFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(webhookFilter);
        String url = chatterboxApiProperties.getPrefix() + "/webhook/*";
        registration.addUrlPatterns(url); // only intercept webhook endpoints
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
