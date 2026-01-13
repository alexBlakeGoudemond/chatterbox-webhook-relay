package za.co.psybergate.chatterbox.application.provider;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;

import java.util.List;

public interface ConfigurationProvider {

    List<ChatterboxSourceGithubRepositoryProperties.DestinationMapping> getDestinationMapping();

    String getTeamsUrl(String teamsDestinationChannel);

    String getDiscordUrl(String discordDestinationChannel);

}
