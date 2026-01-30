package za.co.psybergate.chatterbox.application.domain.github.model;

import lombok.Data;

// TODO BlakeGoudemond 2026/01/30 | should this be in infra? github could we swapped for gitlab
@Data
public class GithubDestinationMapping {

    private String name;

    private String teamsDestinationChannel;

    private String discordDestinationChannel;

}
