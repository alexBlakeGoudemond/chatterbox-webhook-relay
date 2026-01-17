package za.co.psybergate.chatterbox.domain.discord.model;

import lombok.Data;

@Data
public class DiscordAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
