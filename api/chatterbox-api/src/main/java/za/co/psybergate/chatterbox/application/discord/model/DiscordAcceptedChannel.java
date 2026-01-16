package za.co.psybergate.chatterbox.application.discord.model;

import lombok.Data;

@Data
public class DiscordAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
