package za.co.psybergate.chatterbox.test.helper;

import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

@Component
public class TestConfigurationResolver {

    private final WebhookConfigurationResolver configurationResolver;

    public TestConfigurationResolver(WebhookConfigurationResolver configurationResolver) {
        this.configurationResolver = configurationResolver;
    }

    public String getTeamsDestinationUrl(GithubEventDto eventDto) {
        return configurationResolver.getTeamsDestinationUrl(eventDto.repositoryName());
    }

    public String getDiscordDestinationUrl(GithubEventDto eventDto) {
        return configurationResolver.getDiscordDestinationUrl(eventDto.repositoryName());
    }

}
