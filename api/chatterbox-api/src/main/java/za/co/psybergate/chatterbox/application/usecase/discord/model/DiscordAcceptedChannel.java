package za.co.psybergate.chatterbox.application.usecase.discord.model;

import lombok.Data;

@Data
public class DiscordAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
