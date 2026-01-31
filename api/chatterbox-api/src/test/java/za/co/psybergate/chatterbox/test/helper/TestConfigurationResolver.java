package za.co.psybergate.chatterbox.test.helper;

import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;

@Component
public class TestConfigurationResolver {

    private final WebhookConfigurationResolverPort configurationResolver;

    public TestConfigurationResolver(WebhookConfigurationResolverPort configurationResolver) {
        this.configurationResolver = configurationResolver;
    }

    public String getTeamsDestinationUrl(GithubEventDto eventDto) {
        return configurationResolver.getTeamsDestinationUrl(eventDto.repositoryName());
    }

    public String getDiscordDestinationUrl(GithubEventDto eventDto) {
        return configurationResolver.getDiscordDestinationUrl(eventDto.repositoryName());
    }

}
