package za.co.psybergate.chatterbox.domain.github;

import lombok.Data;

@Data
public class GithubDestinationMapping {

    private String name;

    private String teamsDestinationChannel;

    private String discordDestinationChannel;

}
