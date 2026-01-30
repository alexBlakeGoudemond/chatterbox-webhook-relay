package za.co.psybergate.chatterbox.application.domain.teams.model;

import lombok.Data;

// TODO BlakeGoudemond 2026/01/30 | should this be in infra? teams could we swapped for Slack
@Data
public class TeamsAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
