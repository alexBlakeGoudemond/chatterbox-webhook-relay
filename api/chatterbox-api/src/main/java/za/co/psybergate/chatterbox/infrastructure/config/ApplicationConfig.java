package za.co.psybergate.chatterbox.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import za.co.psybergate.chatterbox.infrastructure.config.properties.*;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

@Configuration
@EnableConfigurationProperties({
        ChatterboxApiProperties.class,
        ChatterboxDeliveryTeamsTemplateCardAdaptiveProperties.class,
        ChatterboxDestinationTeamsProperties.class,
        ChatterboxSecurityWebhookGithubProperties.class,
        ChatterboxSourceGithubPayloadProperties.class,
        ChatterboxSourceGithubRepositoryProperties.class,
})
public class ApplicationConfig {

    private final ChatterboxApiProperties chatterboxApiProperties;

    public ApplicationConfig(ChatterboxApiProperties chatterboxApiProperties) {
        this.chatterboxApiProperties = chatterboxApiProperties;
    }

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
