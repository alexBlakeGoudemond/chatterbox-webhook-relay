package za.co.psybergate.chatterbox.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import za.co.psybergate.chatterbox.common.config.properties.*;
import za.co.psybergate.chatterbox.infrastructure.common.config.properties.*;

@Configuration
@EnableConfigurationProperties({
        ChatterboxApiProperties.class,
        ChatterboxDeliveryTeamsProperties.class,
        ChatterboxDestinationTeamsProperties.class,
        ChatterboxDestinationDiscordProperties.class,
        ChatterboxDeliveryDiscordProperties.class,
        ChatterboxSecurityWebhookGithubProperties.class,
        ChatterboxSecurityApiGithubProperties.class,
        ChatterboxSourceGithubPayloadProperties.class,
        ChatterboxSourceGithubRepositoryProperties.class,
})
@RequiredArgsConstructor
public class InfrastructurePropertiesConfig {

    private final ChatterboxSecurityApiGithubProperties apiGithubProperties;

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
