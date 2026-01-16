package za.co.psybergate.chatterbox.infrastructure.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.provider.ConfigurationProvider;
import za.co.psybergate.chatterbox.domain.github.GithubDestinationMapping;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfigurationProviderImpl implements ConfigurationProvider {

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    private final ChatterboxDestinationDiscordProperties destinationDiscordProperties;

    @Override
    public List<GithubDestinationMapping> getDestinationMapping() {
        return repositoryProperties.getDestinationMapping();
    }

    @Override
    public String getTeamsUrl(String teamsDestinationChannel) {
        return destinationTeamsProperties.getUrl(teamsDestinationChannel);
    }

    @Override
    public String getDiscordUrl(String discordDestinationChannel) {
        return destinationDiscordProperties.getUrl(discordDestinationChannel);
    }

}
