package za.co.psybergate.chatterbox.application.domain.github.model;

import lombok.Data;

@Data
public class GithubDestinationMapping {

    private String name;

    private String teamsDestinationChannel;

    private String discordDestinationChannel;

}
