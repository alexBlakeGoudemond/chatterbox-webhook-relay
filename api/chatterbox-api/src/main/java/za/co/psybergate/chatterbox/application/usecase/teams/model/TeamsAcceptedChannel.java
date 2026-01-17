package za.co.psybergate.chatterbox.application.usecase.teams.model;

import lombok.Data;

@Data
public class TeamsAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
