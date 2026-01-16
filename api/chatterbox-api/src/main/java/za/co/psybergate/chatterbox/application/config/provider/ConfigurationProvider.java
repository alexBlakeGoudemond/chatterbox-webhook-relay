package za.co.psybergate.chatterbox.application.config.provider;

import za.co.psybergate.chatterbox.application.github.model.GithubDestinationMapping;

import java.util.List;

public interface ConfigurationProvider {

    List<GithubDestinationMapping> getDestinationMapping();

    String getTeamsUrl(String teamsDestinationChannel);

    String getDiscordUrl(String discordDestinationChannel);

}
