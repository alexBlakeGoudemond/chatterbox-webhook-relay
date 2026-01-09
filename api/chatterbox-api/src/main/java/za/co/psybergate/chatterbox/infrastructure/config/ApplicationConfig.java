package za.co.psybergate.chatterbox.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import za.co.psybergate.chatterbox.infrastructure.config.properties.*;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

@Configuration
@EnableConfigurationProperties({
        ChatterboxApiProperties.class,
        ChatterboxDeliveryTeamsProperties.class,
        ChatterboxDestinationTeamsProperties.class,
        ChatterboxDeliveryDiscordProperties.class,
        ChatterboxSecurityWebhookGithubProperties.class,
        ChatterboxSecurityApiGithubProperties.class,
        ChatterboxSourceGithubPayloadProperties.class,
        ChatterboxSourceGithubRepositoryProperties.class,
})
public class ApplicationConfig {

    private final ChatterboxApiProperties chatterboxApiProperties;

    private final ChatterboxSecurityApiGithubProperties apiGithubProperties;

    public ApplicationConfig(ChatterboxApiProperties chatterboxApiProperties, ChatterboxSecurityApiGithubProperties apiGithubProperties) {
        this.chatterboxApiProperties = chatterboxApiProperties;
        this.apiGithubProperties = apiGithubProperties;
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

    @Bean
    public WebClient githubClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5 MB
                .build();

        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiGithubProperties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "chatterbox")
                .exchangeStrategies(strategies)
                .build();
    }


}
